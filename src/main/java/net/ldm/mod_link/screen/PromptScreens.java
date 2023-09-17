package net.ldm.mod_link.screen;

import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;

public class PromptScreens {
	public static final class RestartPromptScreen extends YesNoScreen implements YesNoScreen.Listener {
		public RestartPromptScreen() {
			super("Downloaded all mods! Quit game?", new MultiplayerScreen(new TitleScreen()));
			setListener(this);
		}

		@Override
		public void onYesButtonPressed() {
			System.exit(0);
		}

		@Override
		public void onNoButtonPressed() {
			close();
		}
	}
}
