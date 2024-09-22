package ch.njol.skript.effects;

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
		"\tlock age of entity"
})
@Since("INSERT VERSION")
public class EffLockAge extends Effect {

	static {
		if (Skript.classExists("org.bukkit.entity.Breedable"))
			Skript.registerEffect(EffLockAge.class,
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
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
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
		return (unlock ? "unlock" : "lock") + " age of " + entities.toString(event,debug);
	}

}
