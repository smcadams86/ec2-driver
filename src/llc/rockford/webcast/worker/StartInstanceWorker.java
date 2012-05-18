package llc.rockford.webcast.worker;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.swing.SwingWorker;

import llc.rockford.webcast.AmazonProperties;
import llc.rockford.webcast.ApplicationState;
import llc.rockford.webcast.EC2Logger;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;

public class StartInstanceWorker extends SwingWorker<WorkerResult, Integer> {

	AmazonEC2 amazonHandle;
	ApplicationState applicationState;
	AmazonProperties amazonProperties;
	
	public StartInstanceWorker(AmazonEC2 amazonHandle, ApplicationState applicationState, AmazonProperties amazonProperties) {
		this.amazonHandle = amazonHandle;
		this.applicationState = applicationState;
		this.amazonProperties = amazonProperties;
	}
	
	@Override
	protected WorkerResult doInBackground() throws Exception {
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
			e.printStackTrace();
		}
		    
		
		RunInstancesResult runInstances = amazonHandle.runInstances(runInstancesRequest);
		Reservation reservation = runInstances.getReservation();
		List<Instance> instanceList = reservation.getInstances();
		// extract the first instance
		if (instanceList.size() > 0) {
			Instance instance = instanceList.get(0);
			EC2Logger.log("Instance ID : " + instance.getInstanceId());
			EC2Logger.log("Instance State : " + instance.getState().getName());
			return new WorkerResult(instance.getState().getName(), instance.getInstanceId());
		}
		return null;
	}

	protected void done() {
		try {
			WorkerResult result = get();
			if (result != null) {
				applicationState.setState(result.getStatus());
				applicationState.setInstance_id(result.getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
