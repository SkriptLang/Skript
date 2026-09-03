package org.skriptlang.skript.bukkit.world.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("World Seed")
@Description("""
	The seed of given world.
	Note that it will be returned as Minecraft internally treats seeds, \
	not as you specified it in world configuration.
	""")
@Example("broadcast \"Seed: %seed of player's world%\"")
@Since({"2.2-dev35", "INSERT VERSION (updated pattern)"})
public class ExprWorldSeed extends PropertyExpression<World, Long> {

	// TODO: turn this into a type property
	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprWorldSeed.class,
				Long.class,
				"[world] seed[s]",
				"worlds",
				false
			)
				.supplier(ExprWorldSeed::new)
				.build()
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<World>) expressions[0]);
		return true;
	}

	@Override
	protected Long[] get(Event event, World[] source) {
		return get(source, WorldInfo::getSeed);
	}

	@Override
	public Class<Long> getReturnType() {
		return Long.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the world seed of" + getExpr().toString(event, debug);
	}

}
