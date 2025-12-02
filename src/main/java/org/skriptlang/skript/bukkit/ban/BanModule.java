package org.skriptlang.skript.bukkit.ban;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.ban.elements.*;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;
import java.util.function.Consumer;

public class BanModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		register(addon.syntaxRegistry(),
			EffBan::register,
			ExprBanDate::register,
			ExprAllBannedEntries::register,
			ExprBanExpiration::register,
			ExprBanReason::register,
			ExprBanSource::register,
			CondIsBanned::register
		);
	}

	private void register(SyntaxRegistry registry, Consumer<SyntaxRegistry>... consumers) {
		Arrays.stream(consumers).forEach(consumer -> consumer.accept(registry));
	}

}
