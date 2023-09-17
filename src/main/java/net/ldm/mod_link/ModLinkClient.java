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
import net.ldm.mod_link.screen.YesNoScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ModLinkClient implements ClientModInitializer {
	public static final Logger LOG = LogManager.getLogger(ModLinkClient.class);
	public static boolean askingServerForMods = false;

	@Override
	public void onInitializeClient() {
		ArrayList<Byte> allReceivedBytes = new ArrayList<>();

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (askingServerForMods) {
				allReceivedBytes.clear();
				showMessage(client, "Asking server for mods...");
				ClientPlayNetworking.send(PacketChannels.ASK_SERVER_FOR_MODS, PacketByteBufs.empty());
				askingServerForMods = false;
				showMessage(client, "Handshake completed!");
			}
		});

		int[] checksumSize = {-1};
		ClientPlayNetworking.registerGlobalReceiver(PacketChannels.MOD_FILE, (client, handler, buf, responseSender) -> {
			if (buf.readableBytes() == 0) {
				disconnect(client, new PromptScreen("Server has no mods! Nothing downloaded.", new MultiplayerScreen(new TitleScreen())));
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
			disconnect(client, new RestartPromptScreen());
			try {
				writeFiles(parser.getFiles());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private void writeFiles(HashMap<String, byte[]> map) throws IOException {
		for (Map.Entry<String, byte[]> entry: map.entrySet()) {
			Files.write(ModLinkServer.MODS_DIR.resolve(entry.getKey()), entry.getValue());
		}
	}

	/**
	 * This ONLY works if connected to a server, it will CRASH the game if used otherwise.
	 */
	private void disconnect(@NotNull MinecraftClient client, Screen screen) {
        assert client.world != null;
        client.execute(() -> {
			client.world.disconnect();
			client.disconnect();
			client.setScreen(screen);
		});
	}

	private void showMessage(@NotNull MinecraftClient client, String message) {
		LOG.info(message);
		client.execute(() -> client.setScreen(new MessageScreen(Text.literal(message))));
	}

	private static final class RestartPromptScreen extends YesNoScreen implements YesNoScreen.Listener {
		public RestartPromptScreen() {
			super("Downloaded all mods! Quit game?", new MultiplayerScreen(new TitleScreen()));
			setListener(this);
		}

		@Override
		public void onYesButtonPressed() {
			System.exit(0);
		}

		@Override
		public void onNoButtonPressed() {
			close();
		}
	}
}