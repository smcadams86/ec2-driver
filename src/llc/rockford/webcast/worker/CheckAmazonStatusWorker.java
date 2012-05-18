package llc.rockford.webcast.worker;

import javax.swing.SwingWorker;

import llc.rockford.webcast.AmazonProperties;
import llc.rockford.webcast.ApplicationState;
import llc.rockford.webcast.EC2Logger;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;

public class CheckAmazonStatusWorker extends SwingWorker<WorkerResult, Integer> {

	AmazonEC2 amazonHandle;
	ApplicationState applicationState;
	AmazonProperties amazonProperties;
	
	public CheckAmazonStatusWorker(AmazonEC2 amazonHandle, ApplicationState applicationState, AmazonProperties amazonProperties) {
		this.amazonHandle = amazonHandle;
		this.applicationState = applicationState;
		this.amazonProperties = amazonProperties;
	}

	@Override
	protected WorkerResult doInBackground() throws Exception {
		DescribeInstancesResult  describeInstancesResult = amazonHandle.describeInstances();
		for (Reservation r : describeInstancesResult.getReservations() ) {
			for (Instance i : r.getInstances()) {
				if (i.getInstanceId().equals(applicationState.getInstance_id())) {
					return new WorkerResult(i.getState().getName(), i.getInstanceId());
				}
			}
		}
		return null;
	}
	
	protected void done() {
		try {
			WorkerResult result = get();
			
			EC2Logger.getInstance();
			EC2Logger.log("applicationState.getState() : " + applicationState.getState());
			if (result != null) {
				EC2Logger.log("result : " + result.getStatus());	
			}
			
			
			if (result != null) {
				switch (applicationState.getState()) {
					case INITIALIZING:
					case ASSOCIATING_IP:
						break;
					case BOOTING :
						if ("running".equals(result.getStatus())) {
							new AssociateInstanceWorker(amazonHandle, applicationState, amazonProperties).execute();
						}
						break;
					default :
						applicationState.setState(result.getStatus());
				}	
			}
			// no instance found.
			else {
				switch (applicationState.getState()) {
					case SHUTTING_DOWN :
					case INITIALIZING :
						applicationState.setState("terminated");
				}	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
