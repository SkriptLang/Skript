package org.skriptlang.skript.bukkit.entity.general.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Entity is in Liquid")
@Description("Checks whether an entity is in rain, lava, water or a bubble column.")
@Example("if player is in rain:")
@Example("if player is in water:")
@Example("player is in lava:")
@Example("player is in bubble column")
@Since("2.6.1")
public class CondEntityIsInLiquid extends PropertyCondition<Entity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(
				CondEntityIsInLiquid.class,
				PropertyType.BE,
				"in (1:water|2:lava|3:[a] bubble[ ]column|4:rain)",
				"entities"
			).supplier(CondEntityIsInLiquid::new)
				.build()
		);
	}

	private static final int IN_WATER = 1, IN_LAVA = 2, IN_BUBBLE_COLUMN = 3, IN_RAIN = 4;

	private int mark;

	@Override
	@SuppressWarnings({"unchecked"})
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<? extends Entity>) exprs[0]);
		setNegated(matchedPattern == 1);
		mark = parseResult.mark;
		return true;
	}
	
	@Override
	public boolean check(Entity entity) {
		return switch (mark) {
			case IN_WATER -> entity.isInWater();
			case IN_LAVA -> entity.isInLava();
			case IN_BUBBLE_COLUMN -> entity.isInBubbleColumn();
			case IN_RAIN -> entity.isInRain();
			default -> throw new IllegalStateException();
		};
	}

	@Override
	protected String getPropertyName() {
		return switch (mark) {
			case IN_WATER -> "in water";
			case IN_LAVA -> "in lava";
			case IN_BUBBLE_COLUMN -> "in bubble column";
			case IN_RAIN -> "in rain";
			default -> throw new IllegalStateException();
		};
	}

}
