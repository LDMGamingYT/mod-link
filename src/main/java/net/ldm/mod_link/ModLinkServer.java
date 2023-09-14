package net.ldm.mod_link;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.ldm.mod_link.ftp.ModLinkFtpServer;

import java.nio.file.Paths;

public class ModLinkServer implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		new ModLinkFtpServer(2221, Paths.get(System.getProperty("user.dir")).resolve("mods").toString()).start();
	}
}