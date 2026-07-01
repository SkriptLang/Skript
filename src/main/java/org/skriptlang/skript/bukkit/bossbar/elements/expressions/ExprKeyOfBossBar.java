package org.skriptlang.skript.bukkit.bossbar.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;


@Name("Key Of Boss Bar")
@Description("""
	Returns the key of a keyed boss bar.
	Does not return anything for normal boss bars.
	""")
@Example("""
	broadcast the boss bar key of {_mybar}
	""")
@Since("INSERT VERSION")
public class ExprKeyOfBossBar extends SimplePropertyExpression<BossBar, String> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprKeyOfBossBar.class,
				String.class,
				"boss[ ]bar (key|id)",
				"bossbars",
				false
			)
				.supplier(ExprKeyOfBossBar::new)
				.build()
		);
	}

	@Override
	public @Nullable String convert(BossBar bar) {
		if (bar instanceof KeyedBossBar keyed) {
			return keyed.getKey().toString();
		}
		return null;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "boss bar key";
	}

}
