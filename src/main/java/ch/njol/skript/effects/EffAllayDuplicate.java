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
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Make Allay Duplicate")
@Description({
	"Make an allay duplicate itself.",
	"This effect will always make an allay duplicate regardless of whether the duplicate attribute is disabled."
})
@Examples("make all allays duplicate")
@Since("INSERT VERSION")
public class EffAllayDuplicate extends Effect {

	static {
		Skript.registerEffect(EffAllayDuplicate.class, "make %livingentities% (duplicate|clone)");
	}

	private Expression<LivingEntity> entities;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Allay allay)
				allay.duplicateAllay();
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + entities.toString(event, debug) + " duplicate";
	}

}
