package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Eating")
@Description("Whether a panda or horse type (horse, camel, donkey, llama, mule) is eating.")
@Example("""
	if last spawned panda is eating:
		force last spawned panda to stop eating
	""")
@Since("2.11")
public class CondIsEating extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsEating.class, PropertyType.BE, "eating", "livingentities")
				.supplier(CondIsEating::new)
				.build()
		);
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Panda panda) {
			return panda.isEating();
		} else if (entity instanceof AbstractHorse horse) {
			return horse.isEating();
		}
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "eating";
	}

}
