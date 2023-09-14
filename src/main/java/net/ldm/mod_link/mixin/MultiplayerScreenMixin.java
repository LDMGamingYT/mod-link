package net.ldm.mod_link.mixin;

import net.ldm.mod_link.ftp.ModLinkFtpClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Paths;

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
	@Shadow
	private ServerInfo selectedEntry;
	@Shadow
	protected MultiplayerServerListWidget serverListWidget;

	protected MultiplayerScreenMixin(Text title) {
		super(title);
	}

	@Inject(at = @At("HEAD"), method="init")
	private void addDownloadModsButton(CallbackInfo ci) {
		// TODO: 2023-09-13 Make the button gray when server not selected (like edit, delete, join server buttons)
		this.addDrawableChild(ButtonWidget.builder(Text.literal("Download Mods"), button -> {
					MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
					if (entry instanceof MultiplayerServerListWidget.ServerEntry) {
						ServerInfo serverInfo = ((MultiplayerServerListWidget.ServerEntry)entry).getServer();
						this.selectedEntry = new ServerInfo(serverInfo.name, serverInfo.address, false);
						this.selectedEntry.copyWithSettingsFrom(serverInfo);
						// TODO: 2023-09-13 Add downloading mods screen
						// TODO: 2023-09-13 This is temp code, replace with call to ModLinkFtpClient#fromIp
						ModLinkFtpClient client = new ModLinkFtpClient(selectedEntry.address, 2221);
						System.out.println(client);
						try {
							// TODO: 2023-09-13 Make this the mods folder (Paths.get(System.getProperty("user.dir")).resolve("mods"))
							client.download(Paths.get("E:\\Logan\\Downloads"));
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				})
				.dimensions(5, 5, 100, 20)
				.build());
	}
}