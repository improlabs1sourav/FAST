package com.improlabs.auth.RequestHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;

import org.apache.log4j.Logger;

import com.improlabs.auth.ChangeRequest;
import com.improlabs.auth.ResponseBuilder.ResponseBuilder;
import com.improlabs.auth.entity.DataPacket;
import com.improlabs.auth.taskprocessor.TaskProcessor;
import com.improlabs.auth.util.LoggerUtil;
import com.improlabs.auth.util.PropertyUtil;

public class RequestHandler extends Thread {
	// The host:port combination to listen on

	public static Logger LOGGER = LoggerUtil.getLogger(RequestHandler.class);

	private InetAddress hostAddress;
	private int[] port;

	// The channel on which we'll accept connections
	// private DatagramChannel serverChannel;

	// The selector we'll be monitoring
	private Selector selector;

	// The buffer into which we'll read data when it's available
	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);

	private ResponseBuilder worker;

	// A list of PendingChange instances
	private List pendingChanges = new LinkedList();

	// Maps a SocketChannel to a list of ByteBuffer instances
	private Map pendingData = new HashMap();
	
	private boolean runFlag=true;
	
	private Integer readCounter=0;

	public RequestHandler(InetAddress hostAddress, int[] port, ResponseBuilder worker) throws IOException {
		this.hostAddress = hostAddress;
		this.port = port;
		this.selector = this.initSelector();
		this.worker = worker;
	}

	public void send(DatagramChannel datagramChannel, DataPacket dataPacket) {
		synchronized (this.pendingChanges) {
			// Indicate we want the interest ops set changed
			this.pendingChanges.add(new ChangeRequest(datagramChannel, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

			// And queue the data we want written
			synchronized (this.pendingData) {
				List queue = (List) this.pendingData.get(datagramChannel);
				if (queue == null) {
					queue = new ArrayList();
					this.pendingData.put(datagramChannel, queue);
				}
				// queue.add(ByteBuffer.wrap(data));
				queue.add(dataPacket);
			}
		}

		// Finally, wake up our selecting thread so it can make the required
		// changes
		this.selector.wakeup();
	}

	public void run() {
		while (runFlag) {
			try {
				// Process any pending changes
				// synchronized (this.pendingChanges) {
				// Iterator changes = this.pendingChanges.iterator();
				// while (changes.hasNext()) {
				// ChangeRequest change = (ChangeRequest) changes.next();
				// switch (change.type) {
				// case ChangeRequest.CHANGEOPS:
				// SelectionKey key = change.socket.keyFor(this.selector);
				// key.interestOps(change.ops);
				// }
				// }
				// this.pendingChanges.clear();
				// }

				// Wait for an event one of the registered channels
				this.selector.select();

				// Iterate over the set of keys for which events are available
				Iterator selectedKeys = this.selector.selectedKeys().iterator();

				// System.out.println("selected key size:: " + this.selector);

				while (selectedKeys.hasNext()) {
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue;
					}

					// Check what event is available and deal with it
					// if (key.isAcceptable()) {
					// this.accept(key);
					// }
					// else
					if (key.isReadable()) {
						this.read(key);
					}

					// else if (key.isWritable()) {
					// this.write(key);
					// }
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void accept(SelectionKey key) throws IOException {
		// For an accept to be pending the channel must be a server socket
		// channel.
		// DatagramChannel datagramChannel = (DatagramChannel) key.channel();
		//
		// // Accept the connection and make it non-blocking
		// SocketChannel socketChannel = serverSocketChannel.accept();
		// Socket socket = socketChannel.socket();
		// socketChannel.configureBlocking(false);
		//
		// // Register the new SocketChannel with our Selector, indicating
		// // we'd like to be notified when there's data waiting to be read
		// socketChannel.register(this.selector, SelectionKey.OP_READ);
	}

	private void read(SelectionKey key) throws IOException {
		DatagramChannel datagramChannel = (DatagramChannel) key.channel();

		SocketAddress sa;
		// Clear out our read buffer so it's ready for new data
		this.readBuffer.clear();

		// Attempt to read off the channel
		int numRead;
		try {

			// numRead = datagramChannel.read(this.readBuffer);
			// sa=datagramChannel.getRemoteAddress();
			
			
			sa = datagramChannel.receive(this.readBuffer);
			this.readBuffer.flip();

			numRead = this.readBuffer.remaining();
			
			synchronized (readCounter) {
				readCounter++;
			}
//			 System.out.println("receiving::" + new
//			 String(this.readBuffer.array()) + " size::" + numRead + " src::"
//			 +sa);

			 System.out.println("data read::" +readCounter);
			 
		} catch (IOException e) {
			// The remote forcibly closed the connection, cancel
			// the selection key and close the channel.
			key.cancel();
			datagramChannel.close();
			return;
		}

		if (numRead == -1) {
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
			key.channel().close();
			key.cancel();
			return;
		}

		// Hand the data off to our worker thread
		DataPacket dataPacket = new DataPacket();
		dataPacket.setData(this.readBuffer.array());
		dataPacket.setDataCount(numRead);
		dataPacket.setSocketAddress(sa);
		// this.worker.processData(this,datagramChannel, dataPacket);
		this.worker.processData(key, dataPacket);

	}

	private void write(SelectionKey key) throws IOException {
		DatagramChannel datagramChannel = (DatagramChannel) key.channel();

		synchronized (this.pendingData) {
			List queue = (List) this.pendingData.get(datagramChannel);

			// Write until there's not more data ...
			while (!queue.isEmpty()) {

				DataPacket dp = (DataPacket) queue.get(0);
				ByteBuffer buf = ByteBuffer.wrap(dp.getData());
				SocketAddress sa = dp.getSocketAddress();
				// datagramChannel.write(buf);
				System.out.println("sending::" + new String(dp.getData()) + "size::" + dp.getDataCount() + " src::"
						+ dp.getSocketAddress());

				datagramChannel.send(buf, sa);

				if (buf.remaining() > 0) {
					// ... or the socket's buffer fills up
					break;
				}
				queue.remove(0);
			}

			if (queue.isEmpty()) {
				// We wrote away all data, so we're no longer interested
				// in writing on this socket. Switch back to waiting for
				// data.
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}

	private Selector initSelector() throws IOException {
		// Create a new selector
		Selector socketSelector = SelectorProvider.provider().openSelector();

		for (int i = 0; i < port.length; i++) {
			// Create a new non-blocking server socket channel
			DatagramChannel serverChannel = DatagramChannel.open();
			serverChannel.configureBlocking(false);

			// Bind the server socket to the specified address and port
			InetSocketAddress isa = new InetSocketAddress(this.hostAddress, port[i]);
			serverChannel.socket().bind(isa);

			LOGGER.debug("Server listing on:" + this.hostAddress.getHostAddress() + " port:" + port[i]);
			// Register the server socket channel, indicating an interest in
			// accepting new connections
			serverChannel.register(socketSelector, SelectionKey.OP_READ);

		}
		return socketSelector;
	}
	
	public void stopThread()
	{
		this.setRunFlag(false);
		//this.interrupt();
	}

	private boolean isRunFlag() {
		return runFlag;
	}

	private void setRunFlag(boolean runFlag) {
		this.runFlag = runFlag;
	}

//	public static void main(String[] args) {
//
//		// System.out.println(PropertyUtil.getInstance().getAllPropertyNames());
//		int numberOfWorkerThreads = 0;
//		int[] ports = null;
//		String hostIp = "";
//
//		try {
//			numberOfWorkerThreads = Integer.parseInt(PropertyUtil.getInstance().getProperty("no_of_worker_thread"));
//
//			String[] portstr = PropertyUtil.getInstance().getProperty("host_ports").split(",");
//			ports = new int[portstr.length];
//
//			for (int i = 0; i < ports.length; i++) {
//				ports[i] = Integer.parseInt(portstr[i]);
//			}
//
//			hostIp = PropertyUtil.getInstance().getProperty("host_ip");
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//
//		try {
//
//			TaskProcessor processor = new TaskProcessor();
//
//			// creating task thread
//			new Thread(processor).start();
//
//			LOGGER.debug("Task thread started");
//
//			ResponseBuilder worker = new ResponseBuilder(processor);
//			// creating worker threads
//			for (int i = 0; i < numberOfWorkerThreads; i++) {
//				new Thread(worker).start();
//			}
//
//			LOGGER.debug("No of worker Thread started::" + numberOfWorkerThreads);
//
//			InetAddress addr = InetAddress.getByName(hostIp);
//
//			// start requesthandler
//			new Thread(new RequestHandler(addr, ports, worker)).start();
//			
//			LOGGER.debug("Request handler started");
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}