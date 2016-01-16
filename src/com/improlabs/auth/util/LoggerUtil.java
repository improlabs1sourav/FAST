package com.improlabs.auth.util;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class LoggerUtil {

	
	public static Logger getLogger(Class classes)
	{
		Logger logger = Logger.getLogger(classes);
		BasicConfigurator.configure();
		
		return logger;
	}
}
