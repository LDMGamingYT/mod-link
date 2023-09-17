package net.ldm.mod_link.networking.packet;

import net.ldm.mod_link.ModLinkClient;
import org.jetbrains.annotations.Contract;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

	/**
	 * @return File name, file contents (bytes)
	 */
	public HashMap<String, byte[]> getFiles() {
		HashMap<String, byte[]> out = new HashMap<>();
		ArrayList<Integer> mods = getModIndices();

		for (int startIndex: mods) {
			int headerLength = getLengthOfSection(startIndex, START_OF_FILE);
			int fileLength = getLengthOfSection(startIndex+headerLength, END_OF_FILE)-HEADER_SIZE;

			if (fileLength == -1) continue;

			byte[] contentBytes = new byte[fileLength];
			for (int j = 0; j < fileLength; j++) {
				contentBytes[j] = packet.get((startIndex+headerLength+HEADER_SIZE) + j);
			}

			if (startIndex == -1 || headerLength == -1) out.put("unknown-mod-" + UUID.randomUUID(), contentBytes);

			byte[] nameBytes = new byte[headerLength];
			for (int j = 0; j < headerLength; j++) {
				nameBytes[j] = packet.get(startIndex + j);
			}

			out.put(new String(nameBytes, StandardCharsets.UTF_8), contentBytes);
		}
		return out;
	}

	/**
	 * @return Index of where the data starts. Example: [4, 2, 3, 0, 0, 0, 0], index would be 3.
	 */
	private int getIndexOfSection(int i) {
		while (i + HEADER_SIZE < packet.size()) {
			if (packet.get(i) == ModFilePacketParser.START_OF_HEADER[0] &&
				packet.get(i+1) == ModFilePacketParser.START_OF_HEADER[1] &&
				packet.get(i+2) == ModFilePacketParser.START_OF_HEADER[2])
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

	private ArrayList<Integer> getModIndices() {
		ArrayList<Integer> out = new ArrayList<>();
		int i = 0;
		int j = 0;
		while (j != -1) {
			j = getIndexOfSection(i);
			if (j != -1) {
				i = j;
				out.add(i);
			}
		}
		return out;
	}

	@Override
	public String toString() {
		return packet.size() + " bytes large stitched packet";
	}
}
