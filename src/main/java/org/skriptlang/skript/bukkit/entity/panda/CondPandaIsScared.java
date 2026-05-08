package org.skriptlang.skript.bukkit.entity.panda;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Panda Is Scared")
@Description("Whether a panda is scared.")
@Example("if last spawned panda is scared:")
@Since("2.11")
public class CondPandaIsScared extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondPandaIsScared.class, PropertyType.BE, "scared", "livingentities")
				.supplier(CondPandaIsScared::new)
				.build()
		);
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Panda panda && panda.isScared();
	}

	@Override
	protected String getPropertyName() {
		return "scared";
	}

}
