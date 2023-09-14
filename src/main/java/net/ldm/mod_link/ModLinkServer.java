package net.ldm.mod_link;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.ldm.mod_link.ftp.ModLinkFtpServer;

import java.nio.file.Paths;

public class ModLinkServer implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		// TODO: 2023-09-13 Port should be set in server.properties, not hard-coded to 2221
		new ModLinkFtpServer(2221, Paths.get(System.getProperty("user.dir")).resolve("mods").toString()).start();
	}
}