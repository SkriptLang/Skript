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

@Name("World from Name")
@Description("Returns the world from a string.")
@Example("world named {game::world-name}")
@Example("the world \"world\"")
@Since("2.6.1")
public class ExprWorldFromName extends SimpleExpression<World> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(
			SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprWorldFromName.class, World.class)
				.addPatterns("[the] world [(named|with name)] %string%")
				.supplier(ExprWorldFromName::new)
				.build()
		);
	}

	private Expression<String> worldName;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		worldName = (Expression<String>) expressions[0];
		return true;
	}

	@Override
	protected World @Nullable [] get(Event event) {
		String worldName = this.worldName.getSingle(event);
		if (worldName == null)
			return new World[0];
		World world = Bukkit.getWorld(worldName);
		if (world == null)
			return new World[0];

		return new World[]{world};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<World> getReturnType() {
		return World.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the world with name " + worldName.toString(event, debug);
	}

}
