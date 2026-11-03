package org.skriptlang.skript.bukkit.bossbar.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Boss;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wither;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Boss Bar From Boss")
@Description("""
	Returns a boss bar from one or more bosses.
	""")
@Example("""
	on spawn of wither:
	    set title of boss bar of event-entity to "<red>Angry Wither"
	""")
@Since("2.16")
public class ExprBossBarFromEntity extends SimplePropertyExpression<Entity, BossBar> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprBossBarFromEntity.class,
				BossBar.class,
				"boss[ ]bar",
				"entities",
				false
			)
				.supplier(ExprBossBarFromEntity::new)
				.build()
		);
	}

	@Override
	public @Nullable BossBar convert(Entity entity) {
		if (entity instanceof Boss boss)
			return boss.getBossBar();
		return null;
	}

	@Override
	public Class<BossBar> getReturnType() {
		return BossBar.class;
	}

	@Override
	protected String getPropertyName() {
		return "boss bar";
	}

}
