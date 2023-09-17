package net.ldm.mod_link.networking.packet;

import org.jetbrains.annotations.Contract;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class ModFilePacketParser {
	private final ArrayList<Byte> packet;
	private final int totalSize;
	public static final byte[] START_OF_HEADER = {0x04, 0x02, 0x01};
	public static final byte[] START_OF_FILE = {0x04, 0x02, 0x02};
	public static final byte[] START_OF_SIZE_HEADER = {0x04, 0x02, 0x03};
	public static final byte[] END_OF_FILE = {0x04, 0x03, 0x02};
	public static final int HEADER_SIZE = 3;
	public static final int SIZE_HEADER_SIZE = HEADER_SIZE + Integer.BYTES + HEADER_SIZE;

	public ModFilePacketParser(ArrayList<Byte> packet, int totalSize) {
		this.packet = packet;
		this.totalSize = totalSize;
	}

	@Contract(pure = true)
	public static boolean doesPacketDefineSize(byte[] packet) {
		return 	packet[0] == START_OF_SIZE_HEADER[0] &&
				packet[1] == START_OF_SIZE_HEADER[1] &&
				packet[2] == START_OF_SIZE_HEADER[2]	;
	}

	public static int getSizeFromPacket(byte[] packet) {
		byte[] sizeBytes = new byte[Integer.BYTES];
		System.out.println("SBytes Buffer: "+Arrays.toString(sizeBytes));
        System.arraycopy(packet, 3, sizeBytes, 0, Integer.BYTES);
		System.out.println("Packet: "+Arrays.toString(packet));
		System.out.println("SBytes: "+Arrays.toString(sizeBytes));
		System.out.println("SBytes as int: "+ByteBuffer.wrap(sizeBytes).getInt());
		return ByteBuffer.wrap(sizeBytes).getInt();
	}

	public boolean checksumSize(int currentSize) {
		System.out.printf("Got %s bytes, expecting %s bytes. Checksum passed? %s%n", currentSize, totalSize, currentSize == totalSize);
		return totalSize == currentSize;
	}

	public String getFileName() {
		return "";
	}

	public byte[] getFileContents() {
		return new byte[0];
	}

	@Override
	public String toString() {
		return packet.size() + " bytes large stitched packet";
	}
}
