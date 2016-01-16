package com.improlabs.auth.main;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.improlabs.auth.RequestHandler.RequestHandler;
import com.improlabs.auth.ResponseBuilder.ResponseBuilder;
import com.improlabs.auth.taskprocessor.TaskProcessor;
import com.improlabs.auth.util.LoggerUtil;
import com.improlabs.auth.util.PropertyUtil;

public class MainRunner {

	public static Logger LOGGER = LoggerUtil.getLogger(MainRunner.class);

	public static void main(String[] args) {
		
		
		int numberOfWorkerThreads = 0;
		int[] ports = null;
		String hostIp = "";

		try {
			numberOfWorkerThreads = Integer.parseInt(PropertyUtil.getInstance().getProperty("no_of_worker_thread"));

			String[] portstr = PropertyUtil.getInstance().getProperty("host_ports").split(",");
			ports = new int[portstr.length];

			for (int i = 0; i < ports.length; i++) {
				ports[i] = Integer.parseInt(portstr[i]);
			}

			hostIp = PropertyUtil.getInstance().getProperty("host_ip");

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		TaskProcessor processor = new TaskProcessor();

		// creating task thread
		Thread taskThread= new Thread(processor);
		taskThread.start();

		LOGGER.debug("Task thread started");
		
		List<Thread> responseBuilderThreadList = new ArrayList<Thread>();
		ResponseBuilder worker = new ResponseBuilder(processor);
		// creating worker threads
		for (int i = 0; i < numberOfWorkerThreads; i++) {
			Thread responseBuilderThread = new Thread(worker);
			responseBuilderThread.start();
			responseBuilderThreadList.add(responseBuilderThread);
		}
		LOGGER.debug("No of worker Thread started::" + numberOfWorkerThreads);
		
		RequestHandler requestHandler = null;
		try {
			InetAddress addr = InetAddress.getByName(hostIp);

			// start requesthandler
			requestHandler=new RequestHandler(addr, ports, worker);
			Thread requestHandlerThread = new Thread(requestHandler);
			requestHandlerThread.start();

			LOGGER.debug("Request handler thread started");

		} catch (IOException e) {
			LOGGER.debug(e.getMessage());
			
			worker.stopThread();
//			for (Thread thread : responseBuilderThreadList) {
//				thread.interrupt();
//			}
			
			processor.stopThread();
			
			LOGGER.debug("all thread stopped");
			System.exit(1);
		}
		
		
	}
	
	
	
	
	
	
	
	
	

}
