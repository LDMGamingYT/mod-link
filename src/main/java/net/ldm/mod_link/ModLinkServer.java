package net.ldm.mod_link;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ldm.mod_link.networking.packet.PacketChannels;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static net.ldm.mod_link.networking.packet.ModFilePacketParser.*;

public class ModLinkServer implements DedicatedServerModInitializer {
	public static final Logger LOG = LogManager.getLogger(ModLinkServer.class);
	private static final File MODS_DIR = Paths.get(System.getProperty("user.dir")).resolve("mods").toFile();

	@Override
	public void onInitializeServer() {
		ServerPlayNetworking.registerGlobalReceiver(PacketChannels.ASK_SERVER_FOR_MODS, (server, player, handler, buf, responseSender) -> {
			try {
				Set<byte[]> packet = readMods();
				if (packet == null) {
					ServerPlayNetworking.send(player, PacketChannels.MOD_FILE, PacketByteBufs.empty());
					return;
				}

				for (byte[] bytes: packet) {
					LOG.info("Sending {} bytes", bytes.length);
					ServerPlayNetworking.send(player, PacketChannels.MOD_FILE, PacketByteBufs.create().writeByteArray(bytes));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private @Nullable Set<byte[]> readMods() throws IOException {
		Set<byte[]> out = new HashSet<>();
		int totalSize = 0;
		File[] mods = Objects.requireNonNull(MODS_DIR.listFiles());

		if (mods.length == 0) return null;

		for (File mod: mods) {
			byte[] fileData = FileUtils.readFileToByteArray(mod);
			byte[] fileNameBytes = mod.getName().getBytes();

			byte[] result = new byte[HEADER_SIZE + fileNameBytes.length + HEADER_SIZE + fileData.length + HEADER_SIZE];

			int index = 0;

			System.arraycopy(START_OF_HEADER, 0, result, index, HEADER_SIZE);
			index += HEADER_SIZE;

			System.arraycopy(fileNameBytes, 0, result, index, fileNameBytes.length);
			index += fileNameBytes.length;

			System.arraycopy(START_OF_FILE, 0, result, index, HEADER_SIZE);
			index += HEADER_SIZE;

			System.arraycopy(fileData, 0, result, index, fileData.length);
			index += fileData.length;

			System.arraycopy(END_OF_FILE, 0, result, index, HEADER_SIZE);

			int splitLength = 1000000;
			int numOfChunks = (int) Math.ceil((double) result.length / splitLength);
			for (int i = 0; i < numOfChunks; i++) {
				int start = i * splitLength;
				int length = Math.min(result.length - start, splitLength);

				byte[] part = new byte[length];
				System.arraycopy(result, start, part, 0, length);

				out.add(part);
				totalSize += length + SIZE_HEADER_SIZE;
			}
		}

		byte[] sizeHeader = new byte[SIZE_HEADER_SIZE];

		System.arraycopy(START_OF_SIZE_HEADER, 0, sizeHeader, 0, HEADER_SIZE);
		System.arraycopy(ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.BIG_ENDIAN)
				.putInt(totalSize).array(), 0, sizeHeader, HEADER_SIZE, Integer.BYTES);

		out.add(sizeHeader);
		System.out.println(Arrays.toString(sizeHeader));

		return out;
	}
}