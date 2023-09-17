package net.ldm.mod_link.udp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServer {
	public static final Logger LOG = LogManager.getLogger("Mod Link UDP Server");
	public static final byte[] END_OF_FILE = { 0x04, 0x03 };
	public static final byte[] END_OF_TRANSMISSION = { 0x04, 0x04 };

	public static void start(int port) {
		try (DatagramSocket socket = new DatagramSocket(port);
			 FileInputStream inputStream = new FileInputStream("E:\\udp_test_upload.txt")) {
			byte[] sendBuffer = new byte[512];

			InetAddress serverAddress = InetAddress.getLocalHost();

			int bytesRead;
			while ((bytesRead = inputStream.read(sendBuffer)) != -1) {
				DatagramPacket packet = new DatagramPacket(sendBuffer, bytesRead, serverAddress, port);
				socket.send(packet);
				LOG.info("Sent packet {} ({} bytes) to {}:{}", sendBuffer, sendBuffer.length, serverAddress, port);
			}

			DatagramPacket packet = new DatagramPacket(END_OF_FILE, END_OF_FILE.length, serverAddress, port);
			socket.send(packet);
			LOG.info("Sent packet {} ({} bytes) to {}:{}", END_OF_FILE, END_OF_FILE.length, serverAddress, port);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
