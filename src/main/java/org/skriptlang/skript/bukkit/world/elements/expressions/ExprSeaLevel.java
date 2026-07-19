package org.skriptlang.skript.bukkit.world.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.World;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Sea Level")
@Description("Gets the sea level of a world.")
@Example("send \"The sea level in your world is %sea level in player's world%\"")
@Since("2.5.1")
public class ExprSeaLevel extends SimplePropertyExpression<World, Long> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprSeaLevel.class,
				Long.class,
				"sea level",
				"worlds",
				false
			)
				.supplier(ExprSeaLevel::new)
				.build()
		);
	}

	@Override
	public Long convert(World world) {
		return (long) world.getSeaLevel();
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	protected String getPropertyName() {
		return "sea level";
	}

}
