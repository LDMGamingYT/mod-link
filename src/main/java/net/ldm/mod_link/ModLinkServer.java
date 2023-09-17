package net.ldm.mod_link;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ldm.mod_link.networking.packet.PacketChannels;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class ModLinkServer implements DedicatedServerModInitializer {
	private static final File MODS_DIR = Paths.get(System.getProperty("user.dir")).resolve("mods").toFile();
	private static final byte[] START_OF_HEADER = { 0x04, 0x02, 0x01 };
	private static final byte[] START_OF_FILE = { 0x04, 0x02, 0x02 };
	private static final byte[] END_OF_FILE = { 0x04, 0x03 };

	@Override
	public void onInitializeServer() {
		ServerPlayNetworking.registerGlobalReceiver(PacketChannels.ASK_SERVER_FOR_MODS, (server, player, handler, buf, responseSender) -> {
			try {
				for (byte[] bytes: readMods()) {
					System.out.printf("Sending %s bytes%n", bytes.length);
					ServerPlayNetworking.send(player, PacketChannels.MOD_FILE, PacketByteBufs.create().writeByteArray(bytes));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private Set<byte[]> readMods() throws IOException {
		Set<byte[]> out = new HashSet<>();
		for (File modFile: Objects.requireNonNull(MODS_DIR.listFiles())) {
			byte[] fileData = FileUtils.readFileToByteArray(modFile);
			byte[] fileNameBytes = modFile.getName().getBytes();

			int bufferSize = START_OF_HEADER.length + fileNameBytes.length + START_OF_FILE.length + fileData.length + END_OF_FILE.length;

			int maxChunks = (bufferSize + 999999) / 1000000;


			for (int chunk = 0; chunk < maxChunks; chunk++) {
				int chunkStart = chunk * 1000000;
				int chunkEnd = Math.min((chunk + 1) * 1000000, bufferSize);

				byte[] result = new byte[bufferSize];
				int index = 0;

				int min = Math.min(START_OF_HEADER.length - chunkStart, result.length);
				if (chunkStart < min) {
					System.arraycopy(START_OF_HEADER, chunkStart, result, index, min);
					index += min;
				} else if ((chunkStart - START_OF_HEADER.length) < fileNameBytes.length) {
					int copyLength = Math.min(fileNameBytes.length - (chunkStart - START_OF_HEADER.length), result.length);
					System.arraycopy(fileNameBytes, (chunkStart - START_OF_HEADER.length), result, index, copyLength);
					index += copyLength;
				} else if ((chunkStart - START_OF_HEADER.length - fileNameBytes.length) < START_OF_FILE.length) {
					int copyLength = Math.min(START_OF_FILE.length - (chunkStart - START_OF_HEADER.length - fileNameBytes.length), result.length);
					System.arraycopy(START_OF_FILE, (chunkStart - START_OF_HEADER.length - fileNameBytes.length), result, index, copyLength);
					index += copyLength;
				} else if ((chunkStart - START_OF_HEADER.length - fileNameBytes.length - START_OF_FILE.length) < fileData.length) {
					int copyLength = Math.min(fileData.length - (chunkStart - START_OF_HEADER.length - fileNameBytes.length - START_OF_FILE.length), result.length);
					System.arraycopy(fileData, (chunkStart - START_OF_HEADER.length - fileNameBytes.length - START_OF_FILE.length), result, index, copyLength);
					index += copyLength;
				} else {
					int i = chunkStart - START_OF_HEADER.length - fileNameBytes.length - START_OF_FILE.length - fileData.length;
					if (i < END_OF_FILE.length) {
						int copyLength = Math.min(END_OF_FILE.length - i, result.length);
						System.arraycopy(END_OF_FILE, i, result, index, copyLength);
						index += copyLength;
					}
				}

				out.add(result);
			}
		}
		return out;
	}
}