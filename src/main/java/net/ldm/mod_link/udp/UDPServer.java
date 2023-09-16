package net.ldm.mod_link.udp;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {
	public static final byte[] END_OF_FILE = { 0x04, 0x03 };
	public static final byte[] END_OF_TRANSMISSION = { 0x04, 0x04 };

	public static void start(int port) {
		try (DatagramSocket socket = new DatagramSocket(port);
			 FileInputStream inputStream = new FileInputStream("E:\\udp_test_upload.txt")) {
			byte[] data = new byte[1024];

			InetAddress serverAddress = InetAddress.getLocalHost();

			int bytesRead;
			while ((bytesRead = inputStream.read(data)) != -1) {
				DatagramPacket packet = new DatagramPacket(data, bytesRead, serverAddress, port);
				socket.send(packet);
			}

			DatagramPacket packet = new DatagramPacket(END_OF_FILE, END_OF_FILE.length, serverAddress, port);
			socket.send(packet);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
