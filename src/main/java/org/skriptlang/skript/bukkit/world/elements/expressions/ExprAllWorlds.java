package org.skriptlang.skript.bukkit.world.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Iterator;

@Name("All Worlds")
@Description("All worlds of the server, useful for looping.")
@Example("""
	loop all worlds:
		broadcast "You're in %loop-world%" to loop-world
	""")
@Since("1.0")
public class ExprAllWorlds extends SimpleExpression<World> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprAllWorlds.class, World.class)
				.addPatterns("[(all [[of] the]|the)] worlds")
				.supplier(ExprAllWorlds::new)
				.build()
		);
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected World @Nullable [] get(Event event) {
		return Bukkit.getWorlds().toArray(new World[0]);
	}

	@Override
	public @Nullable Iterator<World> iterator(Event event) {
		return Bukkit.getWorlds().iterator();
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<World> getReturnType() {
		return World.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "all worlds";
	}

}
