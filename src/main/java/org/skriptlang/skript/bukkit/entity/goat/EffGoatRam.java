package org.skriptlang.skript.bukkit.entity.goat;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Make Goat Ram")
@Description({
	"Make a goat ram an entity.",
	"Ramming does have a cooldown and currently no way to change it."
})
@Example("make all goats ram player")
@Since("2.11")
public class EffGoatRam extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffGoatRam.class)
				.addPatterns(
					"make %livingentities% ram %livingentity%",
					"force %livingentities% to ram %livingentity%"
				).supplier(EffGoatRam::new)
				.build()
		);
	}

	private Expression<LivingEntity> entities;
	private Expression<LivingEntity> target;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		//noinspection unchecked
		target = (Expression<LivingEntity>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		LivingEntity target = this.target.getSingle(event);
		if (target == null)
			return;
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Goat goat) {
				goat.ram(target);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + entities.toString(event, debug) + " ram " + target.toString(event, debug);
	}

}
