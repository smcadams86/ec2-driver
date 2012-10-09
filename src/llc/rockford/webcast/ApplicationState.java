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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ApplicationState {

	private static final long secondInMillis = 1000;
	private static final long SECONDS_TO_INSTALL_WOWZA = 120; // 2 min

	private String instance_id;
	private String ip_id;
	private EC2Driver guiHandle;
	private boolean wowza_installed;
	private long startTime = 0;

	public static enum EC2_State {
		INITIALIZING, TERMINATED, BOOTING, ASSOCIATING_IP, INSTALLING_WOWZA, RUNNING, SHUTTING_DOWN
	}

	private Map<String, EC2_State> amazon_to_state;
	private EC2_State currentState = EC2_State.INITIALIZING;

	public ApplicationState(EC2Driver guiHandle) {
		HashMap<String, EC2_State> aMap = new HashMap<String, EC2_State>();
		aMap.put("pending", EC2_State.BOOTING);
		aMap.put("shutting-down", EC2_State.SHUTTING_DOWN);
		aMap.put("running", EC2_State.RUNNING);
		aMap.put("terminated", EC2_State.TERMINATED);
		amazon_to_state = Collections.unmodifiableMap(aMap);
		wowza_installed = false;
		this.guiHandle = guiHandle;
	}

	public String getIp_id() {
		return ip_id;
	}

	public void setIp_id(String ip_id) {
		EC2Logger.log("setIp_id(" + ip_id + ")");
		this.ip_id = ip_id;
	}

	public String getInstance_id() {
		return instance_id;
	}

	public void setInstance_id(String instance_id) {
		EC2Logger.log("setInstance_id(" + instance_id + ")");
		this.instance_id = instance_id;
	}

	public void setState(String amazon_state) {
		if (startTime != 0 && !wowza_installed) {
			checkTimer();
		}

		if (currentState != amazon_to_state.get(amazon_state)) {
			EC2Logger.log("setState(" + amazon_state + ")");
			currentState = amazon_to_state.get(amazon_state);

			switch (currentState) {
			case INITIALIZING:
				guiHandle.startButton.setEnabled(false);
				guiHandle.stopButton.setEnabled(false);
				guiHandle.startStreamButton.setEnabled(false);
				guiHandle.stopStreamButton.setEnabled(false);
				guiHandle.statusLabel.setText("INITIALIZING");
				guiHandle.statusLabel.setBackground(Color.YELLOW);
				break;
			case TERMINATED:
				guiHandle.startButton.setEnabled(true);
				guiHandle.stopButton.setEnabled(false);
				guiHandle.startStreamButton.setEnabled(false);
				guiHandle.stopStreamButton.setEnabled(false);
				guiHandle.statusLabel.setText("TERMINATED");
				guiHandle.statusLabel.setBackground(Color.RED);
				break;
			case BOOTING:
				guiHandle.startButton.setEnabled(false);
				guiHandle.stopButton.setEnabled(false);
				guiHandle.startStreamButton.setEnabled(false);
				guiHandle.stopStreamButton.setEnabled(false);
				guiHandle.statusLabel.setText("BOOTING UP");
				guiHandle.statusLabel.setBackground(Color.ORANGE);
				break;
			case ASSOCIATING_IP:
				guiHandle.startButton.setEnabled(false);
				guiHandle.stopButton.setEnabled(false);
				guiHandle.startStreamButton.setEnabled(false);
				guiHandle.stopStreamButton.setEnabled(false);
				guiHandle.statusLabel.setText("ASSOCIATING IP ADDRESS");
				guiHandle.statusLabel.setBackground(Color.YELLOW);
				break;
			case RUNNING:
				if (startTime == 0) {
					startTime = System.currentTimeMillis();
				}
				// insert artificial delay for wowza install
				if (wowza_installed == false) {
					currentState = EC2_State.INSTALLING_WOWZA;
					guiHandle.statusLabel.setText("INSTALLING SOFTWARE");
					guiHandle.statusLabel.setBackground(Color.YELLOW);
				} else {
					guiHandle.startButton.setEnabled(false);
					guiHandle.stopButton.setEnabled(true);
					guiHandle.statusLabel.setText("RUNNING");
					guiHandle.statusLabel.setBackground(Color.GREEN);
				}
				break;
			case SHUTTING_DOWN:
				guiHandle.startButton.setEnabled(false);
				guiHandle.stopButton.setEnabled(false);
				guiHandle.startStreamButton.setEnabled(false);
				guiHandle.stopStreamButton.setEnabled(false);
				guiHandle.statusLabel.setText("SHUTTING DOWN");
				guiHandle.statusLabel.setBackground(Color.ORANGE);
			}
		}

		if (guiHandle.broadcaster.isRunning()) {
			guiHandle.startStreamButton.setEnabled(false);
			guiHandle.stopStreamButton.setEnabled(true);

			guiHandle.broadcastStatusLabel.setText("BROADCASTING");
			guiHandle.broadcastStatusLabel.setBackground(Color.GREEN);
		} else {
			if (currentState == ApplicationState.EC2_State.RUNNING) {
				guiHandle.startStreamButton.setEnabled(true);
			}
			guiHandle.stopStreamButton.setEnabled(false);

			guiHandle.broadcastStatusLabel.setText("NOT BROADCASTING");
			guiHandle.broadcastStatusLabel.setBackground(Color.RED);
		}
	}

	private void checkTimer() {
		long diff = System.currentTimeMillis() - startTime;
		long elapsedSeconds = diff / secondInMillis;

		EC2Logger.getInstance();
		EC2Logger.log("elapsedSeconds : " + elapsedSeconds);
		EC2Logger.log("(elapsedSeconds > SECONDS_TO_INSTALL_WOWZA) : "
				+ (elapsedSeconds > SECONDS_TO_INSTALL_WOWZA));
		if (elapsedSeconds > SECONDS_TO_INSTALL_WOWZA) {
			wowza_installed = true;
		}
	}

	public EC2_State getState() {
		return currentState;
	}

}
