package llc.rockford.webcast;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class EC2Logger {
	
	private static EC2Logger _EC2Instance;
	private static Logger logger = Logger.getLogger("EC2Logger");

	public static EC2Logger getInstance() {
		if (_EC2Instance == null) {
			_EC2Instance = new EC2Logger();
			
			logger.setUseParentHandlers(false);
		    Handler conHdlr = new ConsoleHandler();
		    conHdlr.setFormatter(new Formatter() {
		      public String format(LogRecord record) {
		        return record.getLevel() + "  :  "
		            + record.getSourceClassName() + " -:- "
		            + record.getSourceMethodName() + " -:- "
		            + record.getMessage() + "\n";
		      }
		    });
		    logger.addHandler(conHdlr);
		}
		return _EC2Instance;
	}
	
	public static void log(String message) {
		logger.info(message);
	}
}
