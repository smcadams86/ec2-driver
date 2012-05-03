package llc.rockford.webcast;

/**
 * Copyright 2012 Steven McAdams
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import java.awt.Color;

import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;

/**
 * 
 * @author Steven McAdams
 * 
 *         This class is in charge of monitoring the Amazon EC2 Status and
 *         updating the UI appropriately
 * 
 */
public class EC2StatusMonitor implements Runnable {

	final int SLEEP_DURATION = 1000 * 1;
	EC2Driver ec2Driver;
	EC2Handle ec2Handle;
	EC2TaskExecutor ec2TaskExecutor;

	public EC2StatusMonitor(EC2Driver ec2Driver, EC2Handle ec2Handle, EC2TaskExecutor ec2TaskExecutor) {
		this.ec2Driver = ec2Driver;
		this.ec2Handle = ec2Handle;
		this.ec2TaskExecutor = ec2TaskExecutor;
		new Thread(this).start();
	}
	
	private void checkEC2Status() {
		DescribeInstancesResult  describeInstancesResult = ec2Handle.getEc2Handle().describeInstances();
		for (Reservation r : describeInstancesResult.getReservations() ) {
			for (Instance i : r.getInstances()) {
				if (i.getInstanceId().equals(ApplicationState.getInstance_id())) {
					ApplicationState.getInstance();
					switch (ApplicationState.getState()) {
						case INITIALIZING:
							break;
						case BOOTING :
							if ("running".equals(i.getState().getName())) {
								ec2TaskExecutor.associateInstance();
							}
							break;
						case ASSOCIATING_IP:
							break;
						default :
							ApplicationState.setState(i.getState().getName());
					}
				}
			}
		}
	}

	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(SLEEP_DURATION);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	
			checkEC2Status();
			
			ApplicationState.getInstance();
			switch (ApplicationState.getState()) {
			case INITIALIZING:
				ec2Driver.startButton.setEnabled(false);
				ec2Driver.stopButton.setEnabled(false);
				ec2Driver.statusLabel.setText("INITIALIZING");
				ec2Driver.statusLabel.setBackground(Color.YELLOW);
				break;
			case TERMINATED:
				ec2Driver.startButton.setEnabled(true);
				ec2Driver.stopButton.setEnabled(false);
				ec2Driver.statusLabel.setText("TERMINATED");
				ec2Driver.statusLabel.setBackground(Color.RED);
				break;
			case BOOTING:
				ec2Driver.startButton.setEnabled(false);
				ec2Driver.stopButton.setEnabled(false);
				ec2Driver.statusLabel.setText("BOOTING UP");
				ec2Driver.statusLabel.setBackground(Color.ORANGE);
				break;
			case ASSOCIATING_IP:
				ec2Driver.startButton.setEnabled(false);
				ec2Driver.stopButton.setEnabled(false);
				ec2Driver.statusLabel.setText("ASSOCIATING IP ADDRESS");
				ec2Driver.statusLabel.setBackground(Color.YELLOW);
				break;
			case RUNNING:
				ec2Driver.startButton.setEnabled(false);
				ec2Driver.stopButton.setEnabled(true);
				ec2Driver.statusLabel.setText("RUNNING");
				ec2Driver.statusLabel.setBackground(Color.GREEN);
				break;
			case SHUTTING_DOWN:
				ec2Driver.startButton.setEnabled(false);
				ec2Driver.stopButton.setEnabled(false);
				ec2Driver.statusLabel.setText("SHUTTING DOWN");
				ec2Driver.statusLabel.setBackground(Color.ORANGE);
			}
		}
	}
	

}
