package org.skriptlang.skript.bukkit.entity.panda;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Panda Is Sneezing")
@Description("Whether a panda is sneezing.")
@Example("""
	if last spawned panda is sneezing:
		make last spawned panda stop sneezing
	""")
@Since("2.11")
public class CondPandaIsSneezing extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondPandaIsSneezing.class, PropertyType.BE, "sneezing", "livingentities")
				.supplier(CondPandaIsSneezing::new)
				.build()
		);
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Panda panda && panda.isSneezing();
	}

	@Override
	protected String getPropertyName() {
		return "sneezing";
	}

}
