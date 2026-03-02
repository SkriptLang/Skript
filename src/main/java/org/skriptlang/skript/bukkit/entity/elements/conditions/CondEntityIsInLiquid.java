package org.skriptlang.skript.bukkit.entity.elements.conditions;

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

	private enum LiquidType {
		WATER, LAVA, BUBBLE_COLUMN, RAIN
	}

	private LiquidType liquidType;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		liquidType = LiquidType.values()[parseResult.mark - 1];
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}
	
	@Override
	public boolean check(Entity entity) {
		return switch (liquidType) {
			case WATER -> entity.isInWater();
			case LAVA -> entity.isInLava();
			case BUBBLE_COLUMN -> entity.isInBubbleColumn();
			case RAIN -> entity.isInRain();
		};
	}

	@Override
	protected String getPropertyName() {
		return switch (liquidType) {
			case WATER -> "in water";
			case LAVA -> "in lava";
			case BUBBLE_COLUMN -> "in bubble column";
			case RAIN -> "in rain";
		};
	}

}
