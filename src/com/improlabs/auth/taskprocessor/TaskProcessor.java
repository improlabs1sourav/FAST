package com.improlabs.auth.taskprocessor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.improlabs.auth.ResponseBuilder.ResponseBuilder;
import com.improlabs.auth.entity.ServerDataEvent;
import com.improlabs.auth.queue.TaskQueue;
import com.improlabs.auth.util.LoggerUtil;

public class TaskProcessor extends Thread {
	
	public static Logger LOGGER = LoggerUtil.getLogger(TaskProcessor.class);
	//private BlockingQueue<ServerDataEvent> taskBlockingQueue   = new LinkedBlockingQueue<ServerDataEvent>();
	
	private boolean runFlag=true;
	
	public void processData() {
		
		//requestBlockingQueue.offer(new ServerDataEvent(server, datagramChannel, dataPacket));
		//TaskQueue.getInstance().pushData(serverDataEvent);
	}
	
	public void run() {
		
		while(isRunFlag()) {
		
			
//			try {
//				dataEvent=(ServerDataEvent)requestBlockingQueue.take();
//				
//				dataEvent.server.send(dataEvent.datagramChannel,dataEvent.dataPacket);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			// Return to sender
			
		}
	}
	
	
	public void stopThread()
	{
		this.setRunFlag(false);
//		try {
//			Thread.sleep(1);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		this.interrupt();
	}

	private boolean isRunFlag() {
		return runFlag;
	}

	private void setRunFlag(boolean runFlag) {
		this.runFlag = runFlag;
	}
}
