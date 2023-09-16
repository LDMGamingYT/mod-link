package net.ldm.mod_link.screen;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class YesNoScreen extends PromptScreen {
	private Listener listener;

	public YesNoScreen(String message) {
		super(message);
	}

	@Override
	protected void init() {
		this.addDrawableChild(ButtonWidget.builder(Text.literal("Yes"), button -> listener.onYesButtonPressed())
				.dimensions(this.width / 2 - 100 - 5, 156, 100, 20).build());
		this.addDrawableChild(ButtonWidget.builder(Text.literal("No"), button -> listener.onNoButtonPressed())
				.dimensions(this.width / 2 + 5, 156, 100, 20).build());
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	public interface Listener {
		void onYesButtonPressed();
		void onNoButtonPressed();
	}
}
