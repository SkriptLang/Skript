package org.skriptlang.skript.bukkit.bossbar.elements.expressions;

import ch.njol.skript.bukkitutil.NamespacedUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.KeyedBossBar;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Boss Bar From Key")
@Description("Obtains a keyed boss bar from the specified key.")
@Example("""
	set {_bar} to a keyed red boss bar with key "example":
	    set title of event-bossbar to "My Test Title"
	    set color of event-bossbar to red
	    set progress of event-bossbar to 86%
	    set style of event-bossbar to 6 notches
	
	// some structure later on and you need to access it
	set the title of the boss bar with the key "example" to "My Updated Test Title"
	""")
@Since("2.16")
public class ExprBossBarFromKey extends SimpleExpression<KeyedBossBar> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprBossBarFromKey.class, KeyedBossBar.class)
				.addPatterns("[the] boss[ ]bar[s] (from|with) [the] (id|key)[s] %strings%")
				.supplier(ExprBossBarFromKey::new)
				.build()
		);
	}

	private Expression<String> keys;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		keys = (Expression<String>) exprs[0];
		return true;
	}

	@Override
	protected KeyedBossBar @Nullable [] get(Event event) {
		List<KeyedBossBar> bars = new ArrayList<>();
		for (String string : keys.getArray(event)) {
			NamespacedKey key = NamespacedUtils.checkValidationAndSend(string, this);
			if (key == null)
				continue;
			KeyedBossBar bar = Bukkit.getBossBar(key);
			if (bar != null)
				bars.add(bar);
		}
		return bars.toArray(KeyedBossBar[]::new);
	}

	@Override
	public boolean isSingle() {
		return keys.isSingle();
	}

	@Override
	public Class<? extends KeyedBossBar> getReturnType() {
		return KeyedBossBar.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
		    .append("boss bar from keys", keys)
			.toString();
	}

}
