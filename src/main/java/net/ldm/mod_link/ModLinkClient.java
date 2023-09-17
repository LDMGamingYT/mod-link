package net.ldm.mod_link;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.ldm.mod_link.networking.packet.ModFilePacketParser;
import net.ldm.mod_link.networking.packet.PacketChannels;
import net.ldm.mod_link.screen.PromptScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Environment(EnvType.CLIENT)
public class ModLinkClient implements ClientModInitializer {
	public static final Logger LOG = LogManager.getLogger(ModLinkClient.class);
	public static boolean askingServerForMods = false;

	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (askingServerForMods) {
				showMessage(client, "Asking server for mods...");
				ClientPlayNetworking.send(PacketChannels.ASK_SERVER_FOR_MODS, PacketByteBufs.empty());
				askingServerForMods = false;
				showMessage(client, "Handshake completed!");
			}
		});

		ArrayList<Byte> allReceivedBytes = new ArrayList<>();
		int[] checksumSize = {-1};
		ClientPlayNetworking.registerGlobalReceiver(PacketChannels.MOD_FILE, (client, handler, buf, responseSender) -> {
			if (buf.readableBytes() == 0) {
				disconnect(client, "Server has no mods! Nothing downloaded.");
				return;
			}

			byte[] receivedBytes = buf.readByteArray();

			if (ModFilePacketParser.doesPacketDefineSize(receivedBytes)) {
				checksumSize[0] = ModFilePacketParser.getSizeFromPacket(receivedBytes);
			}

			showMessage(client, "Received " + receivedBytes.length + " bytes");
			for (byte b: receivedBytes) allReceivedBytes.add(b);

			showMessage(client, "Parsing mod files...");
			ModFilePacketParser parser = new ModFilePacketParser(allReceivedBytes, checksumSize[0]);
			if (!parser.checksumSize(allReceivedBytes.size())) return;
			disconnect(client, "Downloaded all mods!");
		});
	}

	/**
	 * This ONLY works if connected to a server, it will CRASH the game if used otherwise.
	 */
	private void disconnect(@NotNull MinecraftClient client, String message) {
        assert client.world != null;
        client.execute(() -> {
			client.world.disconnect();
			client.disconnect();
			client.setScreen(new PromptScreen(message, new MultiplayerScreen(new TitleScreen())));
		});
	}

	private void showMessage(@NotNull MinecraftClient client, String message) {
		LOG.info(message);
		client.execute(() -> client.setScreen(new MessageScreen(Text.literal(message))));
	}
}