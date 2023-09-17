package net.ldm.mod_link;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.ldm.mod_link.networking.packet.ModFilePacketParser;
import net.ldm.mod_link.networking.packet.PacketChannels;
import net.ldm.mod_link.screen.PromptScreen;
import net.ldm.mod_link.screen.PromptScreens;
import net.ldm.mod_link.screen.YesNoScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;
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
	ArrayList<Byte> allReceivedBytes = new ArrayList<>();

	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> client.setScreen(new DownloadModsPromptScreen(client)));

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
			disconnect(client, new PromptScreens.RestartPromptScreen());
			try {
				writeFiles(parser.getFiles());
			} catch (IOException e) {
				disconnect(client, new PromptScreen("Failed to write mod files: " + e.getMessage(), new MultiplayerScreen(new TitleScreen())));
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
	private static void disconnect(@NotNull MinecraftClient client, Screen screen) {
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

	public final class DownloadModsPromptScreen extends YesNoScreen implements YesNoScreen.Listener {
		private final MinecraftClient client;
		public DownloadModsPromptScreen(MinecraftClient client) {
			super("This will delete ALL existing mods in this installation?", new MultiplayerScreen(new TitleScreen()));
			setListener(this);
			this.client = client;
		}

		@Override
		public void onYesButtonPressed() {
			if (askingServerForMods) {
				try {
					showMessage(client, "Deleting existing mods...");
					FileUtils.cleanDirectory(ModLinkServer.MODS_DIR.toFile());
					allReceivedBytes.clear();
					showMessage(client, "Asking server for mods...");
					ClientPlayNetworking.send(PacketChannels.ASK_SERVER_FOR_MODS, PacketByteBufs.empty());
					askingServerForMods = false;
					showMessage(client, "Handshake completed!");
				} catch (IOException e) {
					disconnect(client, new PromptScreen("Failed to delete existing mods: " + e.getMessage(), new MultiplayerScreen(new TitleScreen())));
				}
			}
		}

		@Override
		public void onNoButtonPressed() {
			disconnect(client, new MultiplayerScreen(new TitleScreen()));
		}

		@Override
		public void render(DrawContext context, int mouseX, int mouseY, float delta) {
			super.render(context, mouseX, mouseY, delta);
			context.drawCenteredTextWithShadow(textRenderer, Text.of("Are you sure you want to continue?"), this.width / 2, 80, 0xFFFFFF);
		}
	}
}