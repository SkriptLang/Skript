package org.skriptlang.skript.bukkit.entity.panda;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Panda Is On Its Back")
@Description("Whether a panda is on its back.")
@Example("""
	if last spawned panda is on its back:
		make last spawned panda get off its back
	""")
@Since("2.11")
public class CondPandaIsOnBack extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondPandaIsOnBack.class, PropertyType.BE, "on (its|their) back[s]", "livingentities")
				.supplier(CondPandaIsOnBack::new)
				.build()
		);
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Panda panda && panda.isOnBack();
	}

	@Override
	protected String getPropertyName() {
		return "on their back";
	}

}
