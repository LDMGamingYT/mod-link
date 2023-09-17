package net.ldm.mod_link;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.ldm.mod_link.networking.packet.ModFilePacketParser;
import net.ldm.mod_link.networking.packet.PacketChannels;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

@Environment(EnvType.CLIENT)
public class ModLinkClient implements ClientModInitializer {
	public static final Logger LOG = LogManager.getLogger(ModLinkClient.class);
	public static boolean askingServerForMods = false;

	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (askingServerForMods) {
				LOG.info("Asking server for mods");
				ClientPlayNetworking.send(PacketChannels.ASK_SERVER_FOR_MODS, PacketByteBufs.empty());
				askingServerForMods = false;
			}
		});

		ArrayList<Byte> allReceivedBytes = new ArrayList<>();
		ClientPlayNetworking.registerGlobalReceiver(PacketChannels.MOD_FILE, (client, handler, buf, responseSender) -> {
			byte[] receivedBytes = buf.readByteArray();

			LOG.info("Received " + receivedBytes.length + " bytes");
			for (byte b: receivedBytes) allReceivedBytes.add(b);

			ModFilePacketParser parser = new ModFilePacketParser(allReceivedBytes);
			if (!parser.checksumSize(allReceivedBytes.size())) return;
			LOG.info("Stitched together " + parser);
		});
	}
}