package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Make Invisible")
@Description("Makes a living entity visible/invisible.")
@Examples("make target entity invisible")
@Since("INSERT VERSION")
public class EffInvisible extends Effect {

	static {
		if (Skript.methodExists(LivingEntity.class, "isInvisible"))
			Skript.registerEffect(EffInvisible.class,
				"make %livingentities% (invisible|not visible)",
				"make %livingentities% (visible|not invisible)");
	}

	private Expression<LivingEntity> livingEntities;
	private boolean invisible;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		livingEntities = (Expression<LivingEntity>) exprs[0];
		invisible = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event e) {
		for (LivingEntity entity : livingEntities.getArray(e))
			entity.setInvisible(invisible);
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "make " + livingEntities.toString(e, debug) + " " + (invisible ? "in" : "") + "visible";
	}

}
