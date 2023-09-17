package net.ldm.mod_link.networking.packet;

import net.ldm.mod_link.ModLinkClient;
import org.jetbrains.annotations.Contract;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

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
		int startIndex = getIndexOfSection(START_OF_HEADER);
		int length = getLengthOfSection(startIndex, START_OF_FILE);

		if (startIndex == -1 || length == -1) return "unknown-mod-" + UUID.randomUUID();

		byte[] bytes = new byte[length];
		for (int i = 0; i < length; i++) {
			bytes[i] = packet.get(startIndex + i);
		}

		return new String(bytes, StandardCharsets.UTF_8);
	}

	public byte[] getFileContents() {
		return new byte[0];
	}

	/**
	 * @return Index of where the data starts. Example: [4, 2, 3, 0, 0, 0, 0], index would be 3.
	 */
	private int getIndexOfSection(byte[] header) {
		int i = 0;
		while (i + HEADER_SIZE < packet.size()) {
			if (packet.get(i) == header[0] &&
				packet.get(i+1) == header[1] &&
				packet.get(i+2) == header[2])
				return i+3;
			i++;
		}
		return -1;
	}

	private int getLengthOfSection(int startIndex, byte[] endHeader) {
		int i = startIndex;

		while (i + HEADER_SIZE < packet.size()) {
			if (packet.get(i) == endHeader[0] &&
					packet.get(i+1) == endHeader[1] &&
					packet.get(i+2) == endHeader[2])
				break;
			i++;
		}

		return i - startIndex;
	}

	@Override
	public String toString() {
		return packet.size() + " bytes large stitched packet";
	}
}
