package org.skriptlang.skript.bukkit.breeding.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is In Love")
@Description("Checks whether or not a living entity is in love.")
@Example("""
	on spawn of living entity:
		if entity is in love:
			broadcast "That was quick!"
	""")
@Since("2.10")
public class CondIsInLove extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(
				CondIsInLove.class,
				PropertyType.BE,
				"in lov(e|ing) [state|mode]",
				"livingentities"
			)
				.supplier(CondIsInLove::new)
				.build()
		);
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Animals animals)
			return animals.isLoveMode();

		return false;
	}

	@Override
	protected String getPropertyName() {
		return "in love";
	}

}
