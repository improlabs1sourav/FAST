package com.improlabs.auth.util;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.List;

import com.improlabs.auth.entity.DataPacket;

public class Utils {

	public static boolean sendData(SelectionKey key, DataPacket dp) {
		
		try {
			DatagramChannel datagramChannel = (DatagramChannel) key.channel();

			ByteBuffer buf = ByteBuffer.wrap(dp.getData());
			SocketAddress sa = dp.getSocketAddress();
			System.out.println("sending::" + new String(dp.getData()) + " size::" + dp.getDataCount() + " src::"
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

}
