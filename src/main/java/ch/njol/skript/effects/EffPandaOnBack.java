package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Panda;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Force Panda On Back")
@Description("Make a panda get on/off its back.")
@Example("""
	if last spawned panda is on its back:
		make last spawned panda get off its back
	""")
@Since("2.11")
public class EffPandaOnBack extends Effect {

	static {
		Skript.registerEffect(EffPandaOnBack.class,
			"make %livingentities% get (:on|off) (its|their) back[s]",
			"force %livingentities% to get (:on|off) (its|their) back[s]");
	}

	private Expression<LivingEntity> entities;
	private boolean getOn;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		getOn = parseResult.hasTag("on");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Panda panda) {
				panda.setOnBack(getOn);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("make", entities, "get");
		if (getOn) {
			builder.append("on");
		} else {
			builder.append("off");
		}
		builder.append("their backs");
		return builder.toString();
	}

}
