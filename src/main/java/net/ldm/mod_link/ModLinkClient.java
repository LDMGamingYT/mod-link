package net.ldm.mod_link;

import net.fabricmc.api.ClientModInitializer;
import net.ldm.mod_link.udp.UDPClient;

public class ModLinkClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		UDPClient.download(2222);
	}
}