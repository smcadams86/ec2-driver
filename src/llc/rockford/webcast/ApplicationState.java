package llc.rockford.webcast;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ApplicationState {

	static private ApplicationState app_state_instance;
	static private String instance_id;
	static private String ip_id;

	public static String getIp_id() {
		return ip_id;
	}

	public static void setIp_id(String ip_id) {
		EC2Logger.log("setIp_id(" + ip_id + ")");
		ApplicationState.ip_id = ip_id;
	}

	public static String getInstance_id() {
		return instance_id;
	}

	public static void setInstance_id(String instance_id) {
		EC2Logger.log("setInstance_id(" + instance_id + ")");
		ApplicationState.instance_id = instance_id;
	}

	public static enum EC2_State {
		INITIALIZING, TERMINATED, BOOTING, ASSOCIATING_IP, RUNNING, SHUTTING_DOWN
	}

	static EC2_State currentState = EC2_State.INITIALIZING;

	private static final Map<String, EC2_State> amazon_to_state;
	static {
		HashMap<String, EC2_State> aMap = new HashMap<String, EC2_State>();
		aMap.put("pending", EC2_State.BOOTING);
		aMap.put("shutting-down", EC2_State.SHUTTING_DOWN);
		aMap.put("running", EC2_State.RUNNING);
		aMap.put("terminated", EC2_State.TERMINATED);
		amazon_to_state = Collections.unmodifiableMap(aMap);
	}

	public static ApplicationState getInstance() {
		if (app_state_instance == null) {
			app_state_instance = new ApplicationState();
		}
		return app_state_instance;
	}

	public static synchronized void setState(String amazon_state) {
		if (currentState != amazon_to_state.get(amazon_state)) {
			EC2Logger.log("setState(" + amazon_state + ")");
			currentState = amazon_to_state.get(amazon_state);
		}
	}
	
	public static EC2_State getState() {
		return currentState;
	}

}
