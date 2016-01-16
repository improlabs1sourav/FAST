package com.improlabs.auth.ResponseBuilder;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.improlabs.auth.RequestHandler.RequestHandler;
import com.improlabs.auth.entity.DataPacket;
import com.improlabs.auth.entity.ServerDataEvent;
import com.improlabs.auth.main.MainRunner;
import com.improlabs.auth.queue.RequestQueue;
import com.improlabs.auth.taskprocessor.TaskProcessor;
import com.improlabs.auth.util.LoggerUtil;
import com.improlabs.auth.util.Utils;

public class ResponseBuilder extends Thread {
	
	public static Logger LOGGER = LoggerUtil.getLogger(ResponseBuilder.class);
	// private List queue = new LinkedList();
	//private BlockingQueue<ServerDataEvent> requestBlockingQueue = new LinkedBlockingQueue<ServerDataEvent>();

	private TaskProcessor taskProcessor;
	
	private boolean runFlag=true;

	private Integer counter = 0;
	
	private Integer breakCounter = 0;

	public ResponseBuilder(TaskProcessor taskProcessor) {
		this.taskProcessor = taskProcessor;
	}
	// public void processData(RequestHandler requestHandler,DatagramChannel
	// datagramChannel, DataPacket dataPacket) {
	// byte[] dataCopy = new byte[dataPacket.getDataCount()];
	// System.arraycopy(dataPacket.getData(), 0, dataCopy, 0,
	// dataPacket.getDataCount());
	// dataPacket.setData(dataCopy);
	// System.out.println("receiving::" + new String(dataPacket.getData()) + "
	// size::" + dataPacket.getDataCount() + " src::"
	// + dataPacket.getSocketAddress());
	//
	// requestBlockingQueue.offer(new
	// ServerDataEvent(requestHandler,datagramChannel, dataPacket));
	// }
	//

	public void processData(SelectionKey key, DataPacket dataPacket) {
		byte[] dataCopy = new byte[dataPacket.getDataCount()];
		System.arraycopy(dataPacket.getData(), 0, dataCopy, 0, dataPacket.getDataCount());
		dataPacket.setData(dataCopy);
		LOGGER.debug("receiving::" + new String(dataPacket.getData()) + " size::" + dataPacket.getDataCount()
				+ " src::" + dataPacket.getSocketAddress());

		//requestBlockingQueue.offer(new ServerDataEvent(key, dataPacket));
		
		RequestQueue.getInstance().pushData(new ServerDataEvent(key, dataPacket));
	}

	public void run() {
		ServerDataEvent dataEvent;

		while (isRunFlag()) {
			// Wait for data to become available
			// synchronized(queue) {
			// while(queue.isEmpty()) {
			// try {
			// queue.wait();
			// } catch (InterruptedException e) {
			// }
			// }
			// dataEvent = (ServerDataEvent) queue.remove(0);
			// }

			try {
				dataEvent = (ServerDataEvent) RequestQueue.getInstance().popData();
				
				// dataEvent.requestHandler.send(dataEvent.datagramChannel,dataEvent.dataPacket);
				if (!sendData(dataEvent.key, dataEvent.dataPacket)) {
					//requestBlockingQueue.offer(dataEvent);
					RequestQueue.getInstance().pushData(dataEvent);
					synchronized (breakCounter) {
						breakCounter++;
					}
				}
				
				else
				{
					synchronized (counter) {
						counter++;
					}
				}
				
				LOGGER.debug("failed packet count::"+ breakCounter + "success packet count::"+ counter);

				// send to task processor queue
				// taskProcessor.processData();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Return to sender

		}
		
		
	}

	public boolean sendData(SelectionKey key, DataPacket dp) {

		try {
			DatagramChannel datagramChannel = (DatagramChannel) key.channel();

			ByteBuffer buf = ByteBuffer.wrap(dp.getData());
			SocketAddress sa = dp.getSocketAddress();
			LOGGER.debug("sending::" + new String(dp.getData()) + " size::" + dp.getDataCount() + " src::"
					+ dp.getSocketAddress());
			int bytesSent = datagramChannel.send(buf, sa);

			if (buf.remaining() > 0) {
				// ... or the socket's buffer fills up
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
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