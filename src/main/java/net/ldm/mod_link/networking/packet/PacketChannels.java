package net.ldm.mod_link.networking.packet;

import net.ldm.mod_link.ModLink;
import net.minecraft.util.Identifier;

public class PacketChannels {
	public static final Identifier CLIENT_LOGIN_HANDLER = new Identifier(ModLink.MODID, "client_login_handler");
	public static final Identifier ASK_SERVER_FOR_MODS = new Identifier(ModLink.MODID, "ask_server_for_mods");
	public static final Identifier MOD_FILE = new Identifier(ModLink.MODID, "mod_file");
}
