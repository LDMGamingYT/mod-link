package net.ldm.mod_link.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class PromptScreen extends Screen {
	private final Type type;
	private TextFieldWidget textField;
	private @Nullable String text;
	private ScreenClosedListener listener;

	public PromptScreen(String message, Type type) {
		super(Text.of(message));
		this.type = type;
	}

	public enum Type { MESSAGE, INPUT_FIELD }

	@Override
	public void tick() {
		if (type == Type.INPUT_FIELD) this.textField.tick();
	}

	@Override
	protected void init() {
		if (type == Type.INPUT_FIELD) {
			this.textField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 106, 200, 20, this.title);
			this.addSelectableChild(this.textField);
		}

		this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), button -> close())
				.dimensions(this.width / 2 - 100, 156, 200, 20).build());
	}

	@Override
	public void close() {
		text = textField.getText();
		super.close();

		if (listener != null) listener.onClose();
	}

	/**
	 * This will only work if this prompt screen is of {@link Type} INPUT_FIELD
	 */
	public @Nullable String getText() {
		return text;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.renderBackground(context);
		context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, 70, 0xFFFFFF);
		if (type == Type.INPUT_FIELD) this.textField.render(context, mouseX, mouseY, delta);
		super.render(context, mouseX, mouseY, delta);
	}

	public void setListener(ScreenClosedListener listener) {
		this.listener = listener;
	}
}
