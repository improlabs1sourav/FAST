package com.improlabs.auth.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.improlabs.auth.entity.ServerDataEvent;

public class RequestQueue extends BaseQueue {

	private BlockingQueue<ServerDataEvent> requestBlockingQueue;

	private static RequestQueue requestQueue;

	private RequestQueue(BlockingQueue<ServerDataEvent> requestBlockingQueue) {
		this.requestBlockingQueue = requestBlockingQueue;

	}

	public static RequestQueue getInstance() {
		if (requestQueue == null) {
			BlockingQueue<ServerDataEvent> requestBlockingQueue = new LinkedBlockingQueue<ServerDataEvent>();
			requestQueue = new RequestQueue(requestBlockingQueue);
		}
		return requestQueue;
	}

	public void pushData(ServerDataEvent serverDataEvent) {
		requestBlockingQueue.offer(serverDataEvent);
	}

	public ServerDataEvent popData() {
		try {
			return requestBlockingQueue.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
