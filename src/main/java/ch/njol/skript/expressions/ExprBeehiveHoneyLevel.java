package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Name("Beehive Honey Level")
@Description({
	"The current or max honey level of a beehive.",
	"The max level is 5, which cannot be changed."
})
@Examples("set the honey level of {_beehive} to the max honey level of {_beehive}")
@Since("INSERT VERSION")
public class ExprBeehiveHoneyLevel extends SimplePropertyExpression<Block, Integer> {

	static {
		registerDefault(ExprBeehiveHoneyLevel.class, Integer.class, "[max:max[imum]] honey level", "blocks");
	}

	private boolean isMax;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isMax = parseResult.hasTag("max");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Integer convert(Block block) {
		if (!(block.getBlockData() instanceof Beehive beehive))
			return null;
		if (isMax)
			return beehive.getMaximumHoneyLevel();
		return beehive.getHoneyLevel();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (isMax)
			return null;
		return switch (mode) {
			case SET, ADD, REMOVE -> CollectionUtils.array(Integer.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int value = delta != null ? (int) delta[0] : 0;
		Consumer<Beehive> consumer = switch (mode)  {
			case SET -> beehive -> beehive.setHoneyLevel(Math2.fit(0, value, 5));
			case ADD -> beehive -> {
				int current = beehive.getHoneyLevel();
				beehive.setHoneyLevel(Math2.fit(0, current + value, 5));
			};
			case REMOVE -> beehive -> {
				int current = beehive.getHoneyLevel();
				beehive.setHoneyLevel(Math2.fit(0, current - value, 5));
			};
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		};
		for (Block block : getExpr().getArray(event)) {
			if (!(block.getBlockData() instanceof Beehive beehive))
				continue;
			consumer.accept(beehive);
			block.setBlockData(beehive);
		}
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return (isMax ? "maximum " : "") + "honey level";
	}

}
