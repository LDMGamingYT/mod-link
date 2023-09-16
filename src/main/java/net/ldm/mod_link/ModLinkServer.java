package net.ldm.mod_link;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.ldm.mod_link.udp.UDPServer;

public class ModLinkServer implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		new Thread(() -> UDPServer.start(2222), "Mod Link UDP").start();
	}
}