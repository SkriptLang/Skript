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
import org.bukkit.entity.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Make Adult/Baby")
@Description("Force a animal to become an adult or baby.")
@Examples({
	"on spawn of mob:",
	"\tentity is not an adult",
	"\tmake entity an adult",
})
@Since("2.10")
public class EffMakeAdultOrBaby extends Effect {

	static {
		Skript.registerEffect(EffMakeAdultOrBaby.class,
			"make %livingentities% [a[n]] (adult|:baby)",
			"force %livingentities% to be[come] a[n] (adult|:baby)");
	}

	private boolean baby;
	private Expression<LivingEntity> entities;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		baby = parseResult.hasTag("baby");
		//noinspection unchecked
		entities = (Expression<LivingEntity>) expressions[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (!(entity instanceof Ageable ageable))
				continue;

			if (baby) {
				ageable.setBaby();
			} else {
				ageable.setAdult();
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + entities + (baby ? " a baby" : " an adult");
	}

}
