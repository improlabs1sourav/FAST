package com.improlabs.auth.entity;

import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import com.improlabs.auth.RequestHandler.RequestHandler;

public class ServerDataEvent {
	
	//public RequestHandler requestHandler;
	//public DatagramChannel datagramChannel;
	public DataPacket dataPacket;
	public SelectionKey key;
	
	
	public ServerDataEvent(SelectionKey key, DataPacket dataPacket) {
		this.key=key;
		this.dataPacket=dataPacket;
	}
}