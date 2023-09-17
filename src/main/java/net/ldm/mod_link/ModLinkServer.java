package net.ldm.mod_link;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ldm.mod_link.networking.packet.PacketChannels;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class ModLinkServer implements DedicatedServerModInitializer {
	public static final Logger LOG = LogManager.getLogger(ModLinkServer.class);
	private static final File MODS_DIR = Paths.get(System.getProperty("user.dir")).resolve("mods").toFile();
	private static final byte[] START_OF_HEADER = { 0x04, 0x02, 0x01 };
	private static final byte[] START_OF_FILE = { 0x04, 0x02, 0x02 };
	private static final byte[] END_OF_FILE = { 0x04, 0x03 };

	@Override
	public void onInitializeServer() {
		ServerPlayNetworking.registerGlobalReceiver(PacketChannels.ASK_SERVER_FOR_MODS, (server, player, handler, buf, responseSender) -> {
			try {
				for (byte[] bytes: readMods()) {
					LOG.info("Sending {} bytes", bytes.length);
					ServerPlayNetworking.send(player, PacketChannels.MOD_FILE, PacketByteBufs.create().writeByteArray(bytes));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private @NotNull Set<byte[]> readMods() throws IOException {
		Set<byte[]> out = new HashSet<>();
		for (File modFile: Objects.requireNonNull(MODS_DIR.listFiles())) {
			byte[] fileData = FileUtils.readFileToByteArray(modFile);
			byte[] fileNameBytes = modFile.getName().getBytes();

			byte[] result = new byte[START_OF_HEADER.length + fileNameBytes.length + START_OF_FILE.length + fileData.length + END_OF_FILE.length];

			int index = 0;

			System.arraycopy(START_OF_HEADER, 0, result, index, START_OF_HEADER.length);
			index += START_OF_HEADER.length;

			System.arraycopy(fileNameBytes, 0, result, index, fileNameBytes.length);
			index += fileNameBytes.length;

			System.arraycopy(START_OF_FILE, 0, result, index, START_OF_FILE.length);
			index += START_OF_FILE.length;

			System.arraycopy(fileData, 0, result, index, fileData.length);
			index += fileData.length;

			System.arraycopy(END_OF_FILE, 0, result, index, END_OF_FILE.length);

			int splitLength = 1000000;
			int numOfChunks = (int)Math.ceil((double)result.length / splitLength);
			for (int i = 0; i < numOfChunks; i++) {
				int start = i * splitLength;
				int length = Math.min(result.length - start, splitLength);

				byte[] part = new byte[length];
				System.arraycopy(result, start, part, 0, length);

				out.add(part);
			}
		}
		return out;
	}
}