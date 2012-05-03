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
