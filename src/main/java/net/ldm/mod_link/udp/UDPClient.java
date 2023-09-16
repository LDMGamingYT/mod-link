package net.ldm.mod_link.udp;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

@Environment(EnvType.CLIENT)
public class UDPClient {
	public static void download(int port) {
		try (DatagramSocket socket = new DatagramSocket(port);
			 FileOutputStream outputStream = new FileOutputStream("E:\\udp_test_download.txt")) {
			byte[] data = new byte[1024];

			while (true) {
				DatagramPacket packet = new DatagramPacket(data, data.length);
				socket.receive(packet);

				if (compareByteArrays(data, UDPServer.END_OF_FILE)) break;

				outputStream.write(packet.getData(), 0, packet.getLength());
				outputStream.flush();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean compareByteArrays(byte[] b1, byte[] b2) {
		if (b1.length != b2.length) return false;
		for (int i = 0; i < b1.length; i++) if (b1[i] != b2[i]) return false;
		return true;
	}
}
