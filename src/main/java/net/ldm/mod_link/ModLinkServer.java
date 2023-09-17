package net.ldm.mod_link;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.ldm.mod_link.networking.packet.PacketChannels;

public class ModLinkServer implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		ServerPlayNetworking.registerGlobalReceiver(PacketChannels.ASK_SERVER_FOR_MODS, (server, player, handler, buf, responseSender) -> {
			System.out.printf("%s, %s, %s, %s, %s", server, player, handler, buf, responseSender);
		});
	}
}