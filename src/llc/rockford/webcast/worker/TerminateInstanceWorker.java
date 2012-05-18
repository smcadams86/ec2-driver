package llc.rockford.webcast.worker;

import java.util.ArrayList;

import javax.swing.SwingWorker;

import llc.rockford.webcast.AmazonProperties;
import llc.rockford.webcast.ApplicationState;
import llc.rockford.webcast.EC2Logger;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DisassociateAddressRequest;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

public class TerminateInstanceWorker extends SwingWorker<WorkerResult, Integer> {

	AmazonEC2 amazonHandle;
	ApplicationState applicationState;
	AmazonProperties amazonProperties;
	
	public TerminateInstanceWorker(AmazonEC2 amazonHandle, ApplicationState applicationState, AmazonProperties amazonProperties) {
		this.amazonHandle = amazonHandle;
		this.applicationState = applicationState;
		this.amazonProperties = amazonProperties;
	}
	
	@Override
	protected WorkerResult doInBackground() throws Exception {
		EC2Logger.getInstance();
		EC2Logger.log("Terminating Instance...");
		
		ArrayList<String> instanceIds = new ArrayList<String>();
		instanceIds.add(applicationState.getInstance_id());
		TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest(instanceIds);
		TerminateInstancesResult terminateInstancesResult = amazonHandle.terminateInstances(terminateInstancesRequest);
		
		java.util.List<InstanceStateChange> instanceStateChangeList = terminateInstancesResult.getTerminatingInstances();
		for (InstanceStateChange isc : instanceStateChangeList) {
			EC2Logger.log("Instance ID : " + isc.getInstanceId());
			EC2Logger.log("Instance State : " + isc.getCurrentState().getName());
		}
		
		// disassociate IP
		DisassociateAddressRequest disassociateAddressRequest = new DisassociateAddressRequest();
		disassociateAddressRequest.setAssociationId(applicationState.getIp_id());
		disassociateAddressRequest.setPublicIp(amazonProperties.getEc2_elastic_ip());
		amazonHandle.disassociateAddress(disassociateAddressRequest);
		
		return new WorkerResult("shutting-down", "");
	}

	protected void done() {
		try {
			WorkerResult result = get();
			if (result != null) {
				applicationState.setState(result.getStatus());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
