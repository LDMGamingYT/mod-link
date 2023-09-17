package net.ldm.mod_link.networking.packet;

import java.util.ArrayList;

public class ModFilePacketParser {
	private final ArrayList<Byte> bytes;

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
