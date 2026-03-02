package org.skriptlang.skript.bukkit.entity.allay;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Allay Can Duplicate")
@Description("Checks to see if an allay is able to duplicate naturally.")
@Example("""
	if last spawned allay can duplicate:
		disallow last spawned to duplicate
	""")
@Since("2.11")
public class CondAllayCanDuplicate extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondAllayCanDuplicate.class, PropertyType.CAN, "(duplicate|clone)", "livingentities")
				.supplier(CondAllayCanDuplicate::new)
				.build()
		);
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Allay allay && allay.canDuplicate();
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.CAN;
	}

	@Override
	protected String getPropertyName() {
		return "duplicate";
	}

}
