package org.skriptlang.skript.bukkit.entity.general.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Handedness")
@Description("Make mobs left or right-handed. This does not affect players.")
@Example("""
	spawn skeleton at spawn of world "world":
		make entity left handed
	""")
@Example("make all zombies in radius 10 of player right handed")
@Since("2.8.0")
public class EffHandedness extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffHandedness.class)
				.addPattern("make %livingentities% (:left|right)( |-)handed")
				.supplier(EffHandedness::new)
				.build()
		);
	}

	private boolean leftHanded;
	private Expression<LivingEntity> livingEntities;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		leftHanded = parseResult.hasTag("left");
		//noinspection unchecked
		livingEntities = (Expression<LivingEntity>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity livingEntity : livingEntities.getArray(event)) {
			if (livingEntity instanceof Mob mob) {
				mob.setLeftHanded(leftHanded);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + livingEntities.toString(event, debug) + " " + (leftHanded ? "left" : "right") + " handed";
	}

}
