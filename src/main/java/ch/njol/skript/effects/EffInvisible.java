package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Render Unseen")
@Description({
	"Rendereth a living entity visible or invisible. This be not a potion and therefore doth not possess features such as a time limit or particles.",
	"When setting an entity to invisible whilst an invisibility potion is upon it, the potion shall be overridden, and when it runneth out the entity keepeth its invisibility."
})
@Example("render target entity invisible")
@Since("2.7")
public class EffInvisible extends Effect {

	static {
		if (Skript.methodExists(LivingEntity.class, "isInvisible") || Skript.methodExists(Entity.class, "isInvisible"))
			Skript.registerEffect(EffInvisible.class,
				"render %livingentities% (invisible|not visible)",
				"render %livingentities% (visible|not invisible)");
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
