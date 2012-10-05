package llc.rockford.webcast.worker;

import llc.rockford.webcast.EC2Logger;

import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;

public class StreamResultHandler extends DefaultExecuteResultHandler {
	private ExecuteWatchdog watchdog;
	private boolean isRunning = false;

	public StreamResultHandler(ExecuteWatchdog watchdog) {
		this.watchdog = watchdog;
	}

	public StreamResultHandler(int exitValue) {
		super.onProcessComplete(exitValue);
	}

	public void onProcessComplete(int exitValue) {
		super.onProcessComplete(exitValue);
		setRunning(false);
		EC2Logger.getInstance();
		EC2Logger.log("[resultHandler] The stream was successfully completed...");
	}

	public void onProcessFailed(ExecuteException e) {
		super.onProcessFailed(e);
		setRunning(false);
		EC2Logger.getInstance();
		if (watchdog != null && watchdog.killedProcess()) {
			EC2Logger.log("[resultHandler] The stream process timed out");
		} else {
			EC2Logger.log("[resultHandler] The stream process failed to do : " + e.getMessage());
		}
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
}
