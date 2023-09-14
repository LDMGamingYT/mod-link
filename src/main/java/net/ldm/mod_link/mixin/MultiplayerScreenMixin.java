package net.ldm.mod_link.mixin;

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

@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
	@Shadow
	private ServerInfo selectedEntry;
	@Shadow
	protected MultiplayerServerListWidget serverListWidget;

	@Shadow
	protected abstract void editEntry(boolean confirmedAction);

	protected MultiplayerScreenMixin(Text title) {
		super(title);
	}

	@Inject(at = @At("HEAD"), method="init")
	private void addDownloadModsButton(CallbackInfo ci) {
		this.addDrawableChild(ButtonWidget.builder(Text.literal("Download Mods"), button -> {
					MultiplayerServerListWidget.Entry entry = this.serverListWidget.getSelectedOrNull();
					if (entry instanceof MultiplayerServerListWidget.ServerEntry) {
						ServerInfo serverInfo = ((MultiplayerServerListWidget.ServerEntry)entry).getServer();
						this.selectedEntry = new ServerInfo(serverInfo.name, serverInfo.address, false);
						this.selectedEntry.copyWithSettingsFrom(serverInfo);
						// TODO: 2023-09-13 Add downloading mods screen

					}
				})
				.dimensions(5, 5, 100, 20)
				.build());
	}
}