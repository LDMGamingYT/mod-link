package net.ldm.mod_link.networking.packet;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ModFilePacketParser {
	private final ArrayList<Byte> packet;
	public static final byte[] START_OF_HEADER = {0x04, 0x02, 0x01};
	public static final byte[] START_OF_FILE = {0x04, 0x02, 0x02};
	public static final byte[] START_OF_SIZE_HEADER = {0x04, 0x02, 0x03};
	public static final byte[] END_OF_FILE = {0x04, 0x03, 0x02};
	public static final byte[] END_OF_SIZE_HEADER = {0x04, 0x03, 0x03};
	public static final int HEADER_SIZE = 3;
	public static final int SIZE_HEADER_SIZE = HEADER_SIZE + Integer.BYTES + HEADER_SIZE;

	public ModFilePacketParser(ArrayList<Byte> packet) {
		this.packet = packet;
	}

	/* TODO #8: 2023-09-16 This does NOT work, it gets the wrong size.
	 * For reference, call to checksum:
	 * Got 1000077 bytes, expecting 1482777970 bytes. Checksum passed? false
	 */
	public int getTotalSize() {
		int i = 0;
		while (i + HEADER_SIZE < packet.size()) {
			if (packet.get(i) == START_OF_HEADER[0] &&
					packet.get(i + 1) == START_OF_HEADER[1] &&
					packet.get(i + 2) == START_OF_HEADER[2])
				break;
			i++;
		}

		if (i + HEADER_SIZE >= packet.size()) return -1;

		i += HEADER_SIZE;

		if (i + Integer.BYTES <= packet.size()) {
			byte[] totalSizeBytes = new byte[Integer.BYTES];
			for (int j = 0; j < Integer.BYTES; j++) totalSizeBytes[j] = packet.get(i + j);
			return ByteBuffer.wrap(totalSizeBytes).getInt();
		}
		return -1;
	}

	public boolean checksumSize(int currentSize) {
		int totalSize = getTotalSize();
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
