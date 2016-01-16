package com.improlabs.auth.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.improlabs.auth.entity.ServerDataEvent;

public class TaskQueue extends BaseQueue{
	
	private BlockingQueue<ServerDataEvent> taskBlockingQueue;

	private static TaskQueue taskQueue;

	private TaskQueue(BlockingQueue<ServerDataEvent> taskBlockingQueue) {
		this.taskBlockingQueue = taskBlockingQueue;

	}

	public static TaskQueue getInstance() {
		if (taskQueue == null) {
			BlockingQueue<ServerDataEvent> taskBlockingQueue = new LinkedBlockingQueue<ServerDataEvent>();
			taskQueue = new TaskQueue(taskBlockingQueue);
		}
		return taskQueue;
	}

	public void pushData(ServerDataEvent serverDataEvent) {
		taskBlockingQueue.offer(serverDataEvent);
	}

	public ServerDataEvent popData() {
		try {
			return taskBlockingQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
