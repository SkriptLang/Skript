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
import org.bukkit.entity.Strider;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Strider Trembling")
@Description("Bid a strider commence or cease its trembling.")
@Example("""
    if last spawned strider is shivering:
    	make last spawned strider cease trembling
    """)
@Since("2.12")
public class EffStriderShivering extends Effect {

	static {
		Skript.registerEffect(EffStriderShivering.class,
			"make %livingentities% commence trembling",
			"compel %livingentities% to commence trembling",
			"make %livingentities% cease trembling",
			"compel %livingentities% to cease trembling");
	}

	private Expression<LivingEntity> entities;
	private boolean start;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) exprs[0];
		start = matchedPattern <= 1;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity entity : entities.getArray(event)) {
			if (entity instanceof Strider strider) {
				strider.setShivering(start);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("make", entities);
		if (start) {
			builder.append("start");
		} else {
			builder.append("stop");
		}
		builder.append("shivering");
		return builder.toString();
	}
}
