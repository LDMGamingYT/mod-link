package net.ldm.mod_link.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class PromptScreen extends Screen {
	public PromptScreen(String message) {
		super(Text.of(message));
	}

	@Override
	protected void init() {
		this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
				.dimensions(this.width / 2 - 100, 156, 200, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context);
		context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, 70, 0xFFFFFF);
		super.render(context, mouseX, mouseY, delta);
	}
}
