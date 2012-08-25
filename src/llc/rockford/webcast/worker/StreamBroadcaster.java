package llc.rockford.webcast.worker;

import java.io.IOException;
import java.io.InputStream;

import llc.rockford.webcast.AmazonProperties;
import llc.rockford.webcast.ApplicationState;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;

public class StreamBroadcaster {
	
	private String ffmpeg_command;
	
	private ExecuteWatchdog watchdog;
	private CommandLine cmdLine;
	private Executor executor;
	private DefaultExecuteResultHandler resultHandler;
	
	public StreamBroadcaster(AmazonProperties amazonProperties) {
		ffmpeg_command = amazonProperties.getFfmpeg_command() + " rtmp://" + amazonProperties.getEc2_elastic_ip() + "/live/stream";
		
		String fullCommand = "cmd /c start " + amazonProperties.getFfmpeg_location() + "  " + ffmpeg_command;
		cmdLine = CommandLine.parse(fullCommand);

		resultHandler = new DefaultExecuteResultHandler();
		watchdog = new ExecuteWatchdog(60*1000);
		executor = new DefaultExecutor();
		executor.setExitValue(1);
		executor.setWatchdog(watchdog);

	}
	
	
	public void start() throws ExecuteException, IOException {
		executor.execute(cmdLine, resultHandler);
	}
	
	public void stop() {
		watchdog.destroyProcess();
	}
	
	public boolean isRunning() {
		return watchdog.isWatching();
	}

}
