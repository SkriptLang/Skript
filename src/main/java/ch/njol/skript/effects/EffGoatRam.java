package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Make Goat Ram")
@Description({
	"Make a goat ram an entity.",
	"Ramming does have a cooldown and currently no way to change it."
})
@Examples("make all goats ram player")
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class EffGoatRam extends Effect {

	static {
		if (Skript.methodExists(Goat.class, "ram", LivingEntity.class))
			Skript.registerEffect(EffGoatRam.class,
				"make %livingentities% ram %livingentity%",
				"force %livingentities% to ram %livingentity%");
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
