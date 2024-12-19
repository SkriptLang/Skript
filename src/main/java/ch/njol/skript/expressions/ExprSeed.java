package ch.njol.skript.expressions;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;

@Name("World Seed")
@Description("The seed of given world. Note that it will be returned as Minecraft internally treats seeds, not as you specified it in world configuration.")
@Examples("broadcast \"Seed: %seed of player's world%\"")
@Since("2.2-dev35")
public class ExprSeed extends PropertyExpression<World, Long> {

	static {
		Skript.registerExpression(ExprSeed.class, Long.class, ExpressionType.PROPERTY, "[the] seed[s] (from|of) %worlds%", "%worlds%'[s] seed[s]");
	}

	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		setExpr((Expression<World>) exprs[0]);
		return true;
	}
	
	@Override
	protected Long[] get(final Event event, final World[] source) {
		return get(source, new Getter<Long, World>() {
			@Override
			public Long get(final World world) {
				return world.getSeed();
			}
		});
	}
	
	@Override
	public Class<Long> getReturnType() {
		return Long.class;
	}
	
	@Override
	public String toString(final @Nullable Event event, final boolean debug) {
		if (event == null)
			return "the seed of " + getExpr().toString(event, debug);
		return Classes.getDebugMessage(getAll(event));
	}
	
}
