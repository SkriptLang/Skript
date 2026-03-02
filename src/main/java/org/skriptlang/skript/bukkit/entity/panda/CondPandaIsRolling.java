package org.skriptlang.skript.bukkit.entity.panda;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Panda Is Rolling")
@Description("Whether a panda is rolling.")
@Example("""
	if last spawned panda is rolling:
		make last spawned panda stop rolling
	""")
@Since("2.11")
public class CondPandaIsRolling extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondPandaIsRolling.class, PropertyType.BE, "rolling", "livingentities")
				.supplier(CondPandaIsRolling::new)
				.build()
		);
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Panda panda && panda.isRolling();
	}

	@Override
	protected String getPropertyName() {
		return "rolling";
	}

}
