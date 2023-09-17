package net.ldm.mod_link.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Map;

@Mixin(RegistrySyncManager.class)
public class DisableModCheck {
	private static final Logger LOG = LoggerFactory.getLogger("DisableModCheck@FabricRegistrySync");

	/**
	 * @author Mod Link | LDM
	 * @reason Disable mod check/registry sync, so we can connect to the server without mods
	 */
	@VisibleForTesting
	@Overwrite(remap = false)
	public static void checkRemoteRemap(Map<Identifier, Object2IntMap<Identifier>> map) {
		LOG.warn("Registry will NOT be synced S2C, missing mods may cause game to crash.");
		LOG.warn("You should probably press the download mods button before joining.");
	}
}
