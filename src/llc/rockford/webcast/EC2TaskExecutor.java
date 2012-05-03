package llc.rockford.webcast;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.amazonaws.services.ec2.model.AssociateAddressRequest;
import com.amazonaws.services.ec2.model.AssociateAddressResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DisassociateAddressRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

public class EC2TaskExecutor {
	
	public static final String ec2_bin = "E:\\dropbox\\Dropbox\\webcast\\command_line\\ec2-api-tools\\bin\\";
	
	private EC2Handle ec2Handle;
	private AmazonProperties amazonProperties;
	
	public EC2TaskExecutor(EC2Handle ec2Handle, AmazonProperties amazonProperties) {
		this.ec2Handle = ec2Handle;
		this.amazonProperties = amazonProperties;
		initializeInstance();
	}
	
	public void initializeInstance() {
		EC2Logger.getInstance();
		EC2Logger.log("Initializing Instance...");
		
		boolean found = false;
		DescribeInstancesResult describeInstancesResult = ec2Handle.getEc2Handle().describeInstances();
		for (Reservation r : describeInstancesResult.getReservations() ) {
			for (Instance i : r.getInstances()) {
				
				EC2Logger.log(i.getInstanceId() + " : " + i.getState().getName());
				if ("running".equals(i.getState().getName())) {
					found = true;
					ApplicationState.setInstance_id(i.getInstanceId());
				}
			}
		}
		if (found) {
			ApplicationState.getInstance();
			ApplicationState.setState("running");
		}
		else {
			ApplicationState.getInstance();
			ApplicationState.setState("terminated");
		}
		
	}

	public void startInstance() {
		EC2Logger.getInstance();
		EC2Logger.log("Starting Instance...");
		
		// CREATE EC2 INSTANCES
		RunInstancesRequest runInstancesRequest = null;
		
			EC2Logger.log("\t.withInstanceType(\"m1.small\")");
			EC2Logger.log("\t.withImageId(" + amazonProperties.getWowza_ami() + ")");
			EC2Logger.log("\t.withKeyName(" + amazonProperties.getEc2_key() + ")");
			EC2Logger.log("\t.withSecurityGroupIds(" + amazonProperties.getEc2_security_group() + ")");
		
			try {
				InputStream user_data_file = new FileInputStream(amazonProperties.getEc2_user_data_file());
				runInstancesRequest = new RunInstancesRequest()
				    .withInstanceType("m1.small")
				    .withImageId(amazonProperties.getWowza_ami())
				    .withMinCount(1)
				    .withMaxCount(1)
				    .withSecurityGroupIds(amazonProperties.getEc2_security_group())
				    .withKeyName(amazonProperties.getEc2_key())
				    .withUserData(Base64.encodeBase64String(IOUtils.toByteArray(user_data_file)));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//				.withUserData(StringUtils.encodeBase64BinaryFile(user_data_file));
		    
		
		RunInstancesResult runInstances = ec2Handle.getEc2Handle().runInstances(runInstancesRequest);
		Reservation reservation = runInstances.getReservation();
		for (Instance i : reservation.getInstances()) {
			EC2Logger.log("Instance ID : " + i.getInstanceId());
			EC2Logger.log("Instance State : " + i.getState().getName());
			ApplicationState.setInstance_id(i.getInstanceId());

			ApplicationState.getInstance();
			ApplicationState.setState(i.getState().getName());
		}
	}
	
	public void associateInstance() {
		EC2Logger.getInstance();
		EC2Logger.log("Associating Instance...");
		ApplicationState.getInstance();
		ApplicationState.currentState = ApplicationState.EC2_State.ASSOCIATING_IP;
		
		EC2Logger.log("\tInstance ID : " + ApplicationState.getInstance_id());
		EC2Logger.log("\tElastic IP : " + amazonProperties.getEc2_elastic_ip());
		
		AssociateAddressRequest associateAddressRequest = new AssociateAddressRequest();
		associateAddressRequest.setInstanceId(ApplicationState.getInstance_id());
		associateAddressRequest.setPublicIp(amazonProperties.getEc2_elastic_ip());
		AssociateAddressResult associateAddressResult = ec2Handle.getEc2Handle().associateAddress(associateAddressRequest);

		ApplicationState.setIp_id(associateAddressResult.getAssociationId());
		ApplicationState.setState("running");
		
	}
	
	public void disassociateInstance() {
		EC2Logger.getInstance();
		EC2Logger.log("Disassociate Instance...");

		DisassociateAddressRequest disassociateAddressRequest = new DisassociateAddressRequest();
		disassociateAddressRequest.setAssociationId(ApplicationState.getIp_id());
		disassociateAddressRequest.setPublicIp(amazonProperties.getEc2_elastic_ip());
		
		ec2Handle.getEc2Handle().disassociateAddress(disassociateAddressRequest);
		
	}
	

	
	public void terminateInstance() {
		EC2Logger.getInstance();
		EC2Logger.log("Terminating Instance...");
		
		ArrayList<String> instanceIds = new ArrayList<String>();
		instanceIds.add(ApplicationState.getInstance_id());
		TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest(instanceIds);
		TerminateInstancesResult terminateInstancesResult = ec2Handle.getEc2Handle().terminateInstances(terminateInstancesRequest);
		
		java.util.List<InstanceStateChange> instanceStateChangeList = terminateInstancesResult.getTerminatingInstances();
		for (InstanceStateChange isc : instanceStateChangeList) {
			EC2Logger.log("Instance ID : " + isc.getInstanceId());
			EC2Logger.log("Instance State : " + isc.getCurrentState().getName());
		}
		
		disassociateInstance();
		
		ApplicationState.getInstance();
		ApplicationState.setState("shutting-down");
		
	}
}
