package net.ldm.mod_link;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.ldm.mod_link.networking.packet.ModFilePacketParser;
import net.ldm.mod_link.networking.packet.PacketChannels;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.text.Text;
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
				// TODO #7: 2023-09-17 Show screen that says downloading mods once connected
				//client.setScreen(new MessageScreen(Text.of("Asking server for mods...")));
				ClientPlayNetworking.send(PacketChannels.ASK_SERVER_FOR_MODS, PacketByteBufs.empty());
				askingServerForMods = false;
			}
		});

		ArrayList<Byte> allReceivedBytes = new ArrayList<>();
		ClientPlayNetworking.registerGlobalReceiver(PacketChannels.MOD_FILE, (client, handler, buf, responseSender) -> {
			client.setScreen(new MessageScreen(Text.of("Handshake completed!")));
			byte[] receivedBytes = buf.readByteArray();

			//client.setScreen(new MessageScreen(Text.of("Received " + receivedBytes.length + " bytes")));
			LOG.info("Received " + receivedBytes.length + " bytes");
			for (byte b: receivedBytes) allReceivedBytes.add(b);

			//client.setScreen(new MessageScreen(Text.of("Parsing mod files...")));
			ModFilePacketParser parser = new ModFilePacketParser(allReceivedBytes);
			LOG.info("Created " + parser);
			if (!parser.checksumSize(allReceivedBytes.size())) return;
			LOG.info("Checksum passed! Packet complete.");
			//client.setScreen(new MessageScreen(Text.of("Done!")));
			client.disconnect(); // Disconnect once checksum has passed, full packet has been retrieved.
		});
	}
}