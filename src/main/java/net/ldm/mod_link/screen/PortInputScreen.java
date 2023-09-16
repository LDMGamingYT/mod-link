package net.ldm.mod_link.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.jetbrains.annotations.Nullable;

public class PortInputScreen extends PromptScreen {
	private Listener listener;
	private TextFieldWidget textField;
	private @Nullable String text;

	public PortInputScreen() {
		super("Couldn't find Mod Link port automatically, please manually specify it.");
	}

	@Override
	protected void init() {
		this.textField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 106, 200, 20, this.title);
		this.addSelectableChild(this.textField);

		super.init();
	}

	@Override
	public void tick() {
		this.textField.tick();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		this.textField.render(context, mouseX, mouseY, delta);
	}

 	public @Nullable Integer getPort() {
		return text != null ? Integer.valueOf(text) : null;
	}

	@Override
	public void close() {
		super.close();
		text = textField.getText();
		if (listener != null) listener.onClose(getPort());
	}


	public void setListener(Listener listener) {
		this.listener = listener;
	}

	public interface Listener {
		void onClose(@Nullable Integer callbackInfo);
	}
}
