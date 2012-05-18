package llc.rockford.webcast.worker;

import javax.swing.SwingWorker;

import llc.rockford.webcast.ApplicationState;
import llc.rockford.webcast.EC2Logger;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;

public class InitializeWorker extends SwingWorker<WorkerResult, Integer> {
	
	AmazonEC2 amazonHandle;
	ApplicationState applicationState;
	public InitializeWorker(AmazonEC2 amazonHandle, ApplicationState applicationState) {
		this.amazonHandle = amazonHandle;
		this.applicationState = applicationState;
	}

	@Override
	protected WorkerResult doInBackground() throws Exception {
		EC2Logger.getInstance();
		EC2Logger.log("Initializing Instance...");
		
		DescribeInstancesResult describeInstancesResult = amazonHandle.describeInstances();
		for (Reservation r : describeInstancesResult.getReservations() ) {
			for (Instance i : r.getInstances()) {
				EC2Logger.log(i.getInstanceId() + " : " + i.getState().getName());
				if ("running".equals(i.getState().getName())) {
					return new WorkerResult(i.getState().getName(), i.getInstanceId());
				}
			}
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
			else {
				applicationState.setState("terminated");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
