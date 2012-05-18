package llc.rockford.webcast.worker;

import javax.swing.SwingWorker;

import llc.rockford.webcast.AmazonProperties;
import llc.rockford.webcast.ApplicationState;
import llc.rockford.webcast.EC2Logger;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AssociateAddressRequest;
import com.amazonaws.services.ec2.model.AssociateAddressResult;

public class AssociateInstanceWorker extends SwingWorker<WorkerResult, Integer> {

	AmazonEC2 amazonHandle;
	ApplicationState applicationState;
	AmazonProperties amazonProperties;
	
	public AssociateInstanceWorker(AmazonEC2 amazonHandle, ApplicationState applicationState, AmazonProperties amazonProperties) {
		this.amazonHandle = amazonHandle;
		this.applicationState = applicationState;
		this.amazonProperties = amazonProperties;
	}
	
	@Override
	protected WorkerResult doInBackground() throws Exception {
		EC2Logger.getInstance();
		EC2Logger.log("Associating Instance...");
		
		EC2Logger.log("\tInstance ID : " + applicationState.getInstance_id());
		EC2Logger.log("\tElastic IP : " + amazonProperties.getEc2_elastic_ip());
		
		AssociateAddressRequest associateAddressRequest = new AssociateAddressRequest();
		associateAddressRequest.setInstanceId(applicationState.getInstance_id());
		associateAddressRequest.setPublicIp(amazonProperties.getEc2_elastic_ip());
		AssociateAddressResult associateAddressResult = amazonHandle.associateAddress(associateAddressRequest);
		
		return new WorkerResult("running", associateAddressResult.getAssociationId());
	}

	protected void done() {
		try {
			WorkerResult result = get();
			if (result != null) {
				applicationState.setState(result.getStatus());
				applicationState.setIp_id(result.getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
