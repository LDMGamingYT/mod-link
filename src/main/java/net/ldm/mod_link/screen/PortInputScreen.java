package net.ldm.mod_link.screen;

public class PortInputScreen extends PromptScreen {
	private ScreenClosedListener listener;

	public PortInputScreen() {
		super("An error has occurred, please manually specify Mod Link port.", PromptScreen.Type.INPUT_FIELD);
	}

	public int getPort() {
		return Integer.parseInt(super.getText() != null ? super.getText() : "-1");
	}

	@Override
	public void close() {
		super.close();

		if (listener != null) listener.onClose(getPort());
	}

	public void setListener(ScreenClosedListener listener) {
		this.listener = listener;
	}
}
