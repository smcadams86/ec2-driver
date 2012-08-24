package llc.rockford.webcast.worker;

import java.io.IOException;
import java.io.InputStream;

import llc.rockford.webcast.AmazonProperties;
import llc.rockford.webcast.ApplicationState;

public class RLLCBroadcaster {
	
	private AmazonProperties amazonProperties;
	private ApplicationState applicationState;
	private Process broadcastProcess;
	private ProcessWatcher processWatcher;
	private String ffmpeg_command;
	
	public RLLCBroadcaster(AmazonProperties amazonProperties, ApplicationState applicationState) {
		this.amazonProperties = amazonProperties;
		this.applicationState = applicationState;
		ffmpeg_command = amazonProperties.getFfmpeg_command() + " rtmp://" + amazonProperties.getEc2_elastic_ip() + "/live/stream";
	}
	
	
	public void start() {
		try {
			System.out.println("cmd /c start " + amazonProperties.getFfmpeg_location() + "  " + ffmpeg_command);
			broadcastProcess = Runtime.getRuntime().exec("cmd /c start " + amazonProperties.getFfmpeg_location() + "  " + ffmpeg_command);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		processWatcher = new ProcessWatcher(broadcastProcess);
		InputStream output = broadcastProcess.getInputStream();
		while(!processWatcher.isFinished()) {
		    processOutput(output);
		    if(shouldCancel()) broadcastProcess.destroy();
		    try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private boolean shouldCancel() {
		if (applicationState.equals(ApplicationState.EC2_State.SHUTTING_DOWN) ||
				applicationState.equals(ApplicationState.EC2_State.TERMINATED)) {
			return true;
		}
		return false;
	}


	private void processOutput(InputStream output) {
		
	}


	public void stop() {
		broadcastProcess.destroy();
	}
	
	public boolean isRunning() {
		if (processWatcher != null && !processWatcher.isFinished()) {
			return true;
		}
		return false;
	}

}
