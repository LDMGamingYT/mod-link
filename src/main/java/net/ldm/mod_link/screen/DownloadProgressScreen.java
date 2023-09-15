package net.ldm.mod_link.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class DownloadProgressScreen extends Screen {
	public DownloadProgressScreen() {
		super(Text.of("Download Mods"));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context);
		context.drawCenteredTextWithShadow(textRenderer, "Downloading mods...", this.width / 2, this.height / 2, 0xFFFFFF);
		super.render(context, mouseX, mouseY, delta);
	}
}
