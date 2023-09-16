package net.ldm.mod_link.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ldm.mod_link.ftp.ModLinkFtpClient;
import net.ldm.mod_link.ftp.ModLinkFtpServer;
import net.ldm.mod_link.screen.PortInputScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Paths;

@Environment(EnvType.CLIENT)
@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen implements PortInputScreen.Listener {
	@Shadow
	private ServerInfo selectedEntry;
	@Shadow
	protected MultiplayerServerListWidget serverListWidget;
	private ButtonWidget downloadButton;

	protected MultiplayerScreenMixin(Text title) {
		super(title);
	}

	@Inject(at = @At("HEAD"), method="init")
	private void addDownloadModsButton(CallbackInfo ci) {
		// TODO: 2023-09-13 Make the button gray when server not selected (like edit, delete, join server buttons)
		downloadButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("Download Mods"),
						button -> downloadFromPort(ModLinkFtpServer.PORT))
				.dimensions(5, 5, 100, 20)
				.build());
	}

	@Inject(at = @At("RETURN"), method = "updateButtonActivationStates")
	protected void updateButtonActivationStates(CallbackInfo ci) {
		this.downloadButton.active = false;
		MultiplayerServerListWidget.Entry serverListEntry = this.serverListWidget.getSelectedOrNull();
		if (serverListEntry != null && !(serverListEntry instanceof MultiplayerServerListWidget.ScanningEntry))
			this.downloadButton.active = true;
	}


	public void downloadFromPort(int port) {
		assert this.client != null;
		MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
		if (entry instanceof MultiplayerServerListWidget.ServerEntry) {
			ServerInfo serverInfo = ((MultiplayerServerListWidget.ServerEntry)entry).getServer();

			this.selectedEntry = new ServerInfo(serverInfo.name, serverInfo.address, false);
			this.selectedEntry.copyWithSettingsFrom(serverInfo);

			ModLinkFtpClient client = ModLinkFtpClient.fromServerAddress(serverInfo.address, port);

			try {
				client.download(Paths.get(System.getProperty("user.dir")).resolve("mods"), this.client);
			} catch (IOException e) {
				PortInputScreen prompt = new PortInputScreen();
				prompt.setListener(this);
				this.client.setScreen(prompt);
				ModLinkFtpClient.LOG.error(e);
			}
		}
	}

	@Override
	public void onClose(@Nullable Integer callbackInfo) {
		if (callbackInfo != null) downloadFromPort(callbackInfo);
		else {
			PortInputScreen prompt = new PortInputScreen();
			prompt.setListener(this);
			assert this.client != null;
			this.client.setScreen(prompt);
			ModLinkFtpClient.LOG.error("Screen callback info (port) is null, re-prompting user");
		}
	}
}