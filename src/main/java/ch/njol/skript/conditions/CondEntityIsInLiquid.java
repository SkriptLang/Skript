package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;

@Name("Entity is in Liquid")
@Description("Checks whether an entity is in rain, lava, water or a bubble column.")
@Example("if player is in rain:")
@Example("if player is in water:")
@Example("player is in lava:")
@Example("player is in bubble column")
@Since("2.6.1")
public class CondEntityIsInLiquid extends PropertyCondition<Entity> {
	
	static {
		StringBuilder patterns = new StringBuilder();
		patterns.append("1¦water");
		if (Skript.methodExists(Entity.class, "isInLava")) // TODO - remove this when Spigot support is dropped
			patterns.append("|2¦lava|3¦[a] bubble[ ]column|4¦rain");
		register(CondEntityIsInLiquid.class, "in (" + patterns + ")", "entities");
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
