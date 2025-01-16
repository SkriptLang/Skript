package org.skriptlang.skript.bukkit.breeding.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Allow Aging")
@Description("Sets whether or not living entities will be able to age.")
@Examples({
	"on spawn of animal:",
		"\tallow aging of entity"
})
@Since("2.10")
public class EffAllowAging extends Effect {

	static {
		Skript.registerEffect(EffAllowAging.class,
			"lock age of %livingentities%",
			"prevent aging of %livingentities%",
			"prevent %livingentities% from aging",
			"unlock age of %livingentities%",
			"allow aging of %livingentities%",
			"allow %livingentities% to age");
	}

	private boolean unlock;
	private Expression<LivingEntity> entities;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) expressions[0];
		unlock = matchedPattern > 2;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity livingEntity : entities.getArray(event)) {
			if (!(livingEntity instanceof Breedable breedable))
				continue;

			breedable.setAgeLock(!unlock);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (unlock ? "allow" : "prevent") + " aging of " + entities.toString(event,debug);
	}

}
