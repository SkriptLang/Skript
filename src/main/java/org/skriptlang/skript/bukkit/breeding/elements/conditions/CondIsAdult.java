package org.skriptlang.skript.bukkit.breeding.elements.conditions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Adult")
@Description("Checks whether or not a living entity is an adult.")
@Example("""
	on drink:
		event-entity is not an adult
		kill event-entity
	""")
@Since("2.10")
public class CondIsAdult extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(
				CondIsAdult.class,
				PropertyType.BE,
				"[an] adult",
				"livingentities"
			)
				.supplier(CondIsAdult::new)
				.build()
		);
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Ageable ageable && ageable.isAdult();
	}

	@Override
	public boolean acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET;
	}

	@Override
	protected void change(LivingEntity entity, boolean adult, ChangeMode mode) {
		if (entity instanceof Ageable ageable) {
			if (adult) {
				ageable.setAdult();
			} else {
				ageable.setBaby();
			}
		}
	}

	@Override
	protected String getPropertyName() {
		return "an adult";
	}

}
