package llc.rockford.webcast.worker;

import java.io.IOException;

import llc.rockford.webcast.AmazonProperties;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;

public class StreamBroadcaster {
	
	private static final int MAX_STREAM_TIME = 4 * 60 * 60 * 1000; // 4 hours

	private String ffmpeg_command;
	private ExecuteWatchdog watchdog;
	private CommandLine cmdLine;
	private Executor executor;
	private StreamResultHandler resultHandler;
	
	public StreamBroadcaster(AmazonProperties amazonProperties) {
		ffmpeg_command = amazonProperties.getFfmpeg_command() + " rtmp://"
				+ amazonProperties.getEc2_elastic_ip() + "/live/stream";

		String fullCommand = ""
				+ amazonProperties.getFfmpeg_location() + "  " + ffmpeg_command;

		cmdLine = CommandLine.parse(fullCommand);
		
		watchdog = new ExecuteWatchdog(MAX_STREAM_TIME);
		resultHandler = new StreamResultHandler(watchdog);
		executor = new DefaultExecutor();
		executor.setExitValue(1);
		executor.setWatchdog(watchdog);

	}

	public void start() throws ExecuteException, IOException, InterruptedException {
		resultHandler.setRunning(true);
		executor.execute(cmdLine, resultHandler);
	}

	public void stop() {
		watchdog.destroyProcess();
	}

	public boolean isRunning() {
		return resultHandler.isRunning();
	}

}
