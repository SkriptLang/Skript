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

@Name("Make Breedable")
@Description("Sets whether or not entities will be able to breed. Only works on animals.")
@Examples({
	"on spawn of animal:",
		"\tmake entity unbreedable"
})
@Since("2.10")
public class EffBreedable extends Effect {

	static {
		Skript.registerEffect(EffBreedable.class,
			"make %livingentities% breedable",
			"unsterilize %livingentities%",
			"make %livingentities% (not |non(-| )|un)breedable",
			"sterilize %livingentities%");
	}

	private boolean sterilize;
	private Expression<LivingEntity> entities;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		sterilize = matchedPattern > 1;
		//noinspection unchecked
		entities = (Expression<LivingEntity>) expressions[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (!(entity instanceof Breedable breedable))
				continue;

			breedable.setBreed(!sterilize);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + entities.toString(event, debug) + (sterilize ? " non-" : " ") + "breedable";
	}

}
