package net.ldm.mod_link.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ldm.mod_link.ModLinkClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MultiplayerScreen.class)
public abstract class MultiplayerScreenMixin extends Screen {
	@Shadow protected MultiplayerServerListWidget serverListWidget;
	@Shadow public abstract void connect();
	private ButtonWidget downloadButton;

	protected MultiplayerScreenMixin(Text title) {
		super(title);
	}

	@Inject(at = @At("HEAD"), method="init")
	private void addDownloadModsButton(CallbackInfo ci) {
		downloadButton = this.addDrawableChild(ButtonWidget.builder(Text.literal("Download Mods"),
						button -> askServerForMods())
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

	public void askServerForMods() {
		ModLinkClient.askingServerForMods = true;
		connect();
	}
}