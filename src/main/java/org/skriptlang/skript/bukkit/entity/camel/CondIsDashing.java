package org.skriptlang.skript.bukkit.entity.camel;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Camel;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Camel Is Dashing")
@Description("Checks whether a camel is currently using its dash ability.")
@Example("""
	if last spawned camel is dashing:
		kill last spawned camel
	""")
@Since("2.11")
public class CondIsDashing extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsDashing.class, PropertyType.BE, "dashing", "livingentities")
				.supplier(CondIsDashing::new)
				.build()
		);
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Camel camel)
			return camel.isDashing();
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "dashing";
	}

}
