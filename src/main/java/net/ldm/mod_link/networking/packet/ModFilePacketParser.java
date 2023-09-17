package net.ldm.mod_link.networking.packet;

import java.util.ArrayList;

public class ModFilePacketParser {
	private final ArrayList<Byte> bytes;
	public static final byte[] START_OF_HEADER = {0x04, 0x02, 0x01};
	public static final byte[] START_OF_FILE = {0x04, 0x02, 0x02};
	public static final byte[] START_OF_SIZE_HEADER = {0x04, 0x02, 0x03};
	public static final byte[] END_OF_FILE = {0x04, 0x03, 0x02};
	public static final byte[] END_OF_SIZE_HEADER = {0x04, 0x03, 0x03};
	public static final int HEADER_SIZE = 3;
	public static final int SIZE_HEADER_SIZE = HEADER_SIZE + Integer.BYTES + HEADER_SIZE;

	public ModFilePacketParser(ArrayList<Byte> bytes) {
		this.bytes = bytes;
	}

	public int getTotalSize() {
		return 0;
	}

	public boolean checksumSize(int currentSize) {
		return getTotalSize() == currentSize;
	}

	public String getFileName() {
		return "";
	}

	public byte[] getFileContents() {
		return new byte[0];
	}

	@Override
	public String toString() {
		return bytes.size() + " bytes large stitched packet";
	}
}
