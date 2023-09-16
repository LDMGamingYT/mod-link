package net.ldm.mod_link.screen;

public class PortInputScreen extends PromptScreen {
	private PortInputScreenListener listener;

	public PortInputScreen() {
		super("Couldn't find Mod Link port automatically, please manually specify it.", PromptScreen.Type.INPUT_FIELD);
	}

	public int getPort() {
		return Integer.parseInt(super.getText() != null ? super.getText() : "-1");
	}

	@Override
	public void close() {
		super.close();

		if (listener != null) listener.onClose(getPort());
	}

	public void setListener(PortInputScreenListener listener) {
		this.listener = listener;
	}
}
