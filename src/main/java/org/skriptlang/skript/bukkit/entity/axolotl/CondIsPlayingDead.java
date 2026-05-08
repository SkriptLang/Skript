package org.skriptlang.skript.bukkit.entity.axolotl;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Playing Dead")
@Description("Checks to see if an axolotl is playing dead.")
@Example("""
	if last spawned axolotl is playing dead:
		make last spawned axolotl stop playing dead
	""")
@Since("2.11")
public class CondIsPlayingDead extends PropertyCondition<LivingEntity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(CondIsPlayingDead.class, PropertyType.BE, "playing dead", "livingentities")
				.supplier(CondIsPlayingDead::new)
				.build()
		);
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Axolotl axolotl && axolotl.isPlayingDead();
	}

	@Override
	protected String getPropertyName() {
		return "playing dead";
	}

}
