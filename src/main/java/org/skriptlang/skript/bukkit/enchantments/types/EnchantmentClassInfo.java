package org.skriptlang.skript.bukkit.enchantments.types;

import ch.njol.skript.classes.registry.RegistryClassInfo;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class EnchantmentClassInfo extends RegistryClassInfo<Enchantment> {

	public EnchantmentClassInfo() {
		super(Enchantment.class, RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT),
			"enchantment", "enchantments");

		user("enchantments?")
			.name("Enchantment")
			.description("An enchantment, e.g. 'sharpness' or 'fortune'. Unlike <a href='#enchantmenttype'>enchantment type</a> " +
					"this type has no level, but you usually don't need to use this type anyway.",
					"NOTE: Minecraft namespaces are supported, ex: 'minecraft:basalt_deltas'.",
					"This also supports custom enchantments using namespaces, ex: 'myenchants:explosive'.")
			.since("1.4.6")
			.before("enchantmenttype");
	}

}
