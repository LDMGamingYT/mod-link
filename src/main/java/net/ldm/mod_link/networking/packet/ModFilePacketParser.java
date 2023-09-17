package net.ldm.mod_link.networking.packet;

import net.ldm.mod_link.ModLinkClient;
import org.jetbrains.annotations.Contract;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class ModFilePacketParser {
	private final ArrayList<Byte> packet;
	private final int totalSize;
	public static final byte[] START_OF_HEADER = {0x04, 0x02, 0x01};
	public static final byte[] START_OF_FILE = {0x04, 0x02, 0x02};
	public static final byte[] START_OF_SIZE_HEADER = {0x04, 0x02, 0x03};
	public static final byte[] END_OF_FILE = {0x04, 0x03, 0x02};
	public static final int HEADER_SIZE = 3;
	public static final int SIZE_HEADER_SIZE = HEADER_SIZE + Integer.BYTES;

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
        System.arraycopy(packet, HEADER_SIZE, sizeBytes, 0, Integer.BYTES);
		return ByteBuffer.wrap(sizeBytes).order(ByteOrder.BIG_ENDIAN).getInt();
	}

	public boolean checksumSize(int currentSize) {
		ModLinkClient.LOG.info("Got {} bytes, expecting {} bytes. Checksum passed? {}", currentSize, totalSize, currentSize == totalSize);
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
