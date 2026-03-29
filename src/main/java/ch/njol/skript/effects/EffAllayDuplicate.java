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
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Bid Allay Duplicate")
@Description({
	"Doth bid an allay to duplicate itself.",
	"This effect shall always compel an allay to duplicate regardless of whether the duplicate attribute be disabled."
})
@Example("bid all allays duplicate")
@Since("2.11")
public class EffAllayDuplicate extends Effect {

	static {
		Skript.registerEffect(EffAllayDuplicate.class, "bid %livingentities% (duplicate|clone)");
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
