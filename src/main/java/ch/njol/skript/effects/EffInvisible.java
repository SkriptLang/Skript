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
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Make Invisible")
@Description({
	"Makes a living entity visible/invisible. This is not a potion and therefore does not have features such as a time limit or particles.",
	"When setting an entity to invisible while using an invisibility potion on it, the potion will be overridden and when it runs out the entity keeps its invisibility."
})
@Examples("make target entity invisible")
@Since("2.7")
public class EffInvisible extends Effect {

	static {
		if (Skript.methodExists(LivingEntity.class, "isInvisible"))
			Skript.registerEffect(EffInvisible.class,
				"make %livingentities% (invisible|not visible)",
				"make %livingentities% (visible|not invisible)");
	}

	private boolean invisible;
	private Expression<LivingEntity> livingEntities;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		livingEntities = (Expression<LivingEntity>) exprs[0];
		invisible = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : livingEntities.getArray(event))
			entity.setInvisible(invisible);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + livingEntities.toString(event, debug) + " " + (invisible ? "in" : "") + "visible";
	}

}
