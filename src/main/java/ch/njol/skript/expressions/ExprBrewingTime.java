package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Name("Brewing Time")
@Description("The remaining brewing time of the brewing stand.")
@Examples({
	"set the brewing time of {_block} to 10",
	"clear the remaining brewing time of {_block}"
})
@Since("INSERT VERSION")
public class ExprBrewingTime extends SimplePropertyExpression<Block, Integer> {

	static {
		registerDefault(ExprBrewingTime.class, Integer.class, "[current|remaining] brewing time", "blocks");
	}

	@Override
	public @Nullable Integer convert(Block block) {
		if (!(block.getState() instanceof BrewingStand brewingStand))
			return null;
		return brewingStand.getBrewingTime();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE, SET, DELETE -> CollectionUtils.array(Integer.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int providedValue = delta != null ? (Integer) delta[0] : 0;
		Consumer<BrewingStand> consumer = switch (mode) {
			case ADD -> brewingStand -> {
				int current = brewingStand.getBrewingTime();
				int newValue = Math2.fit(0, current + providedValue, Integer.MAX_VALUE);
				brewingStand.setBrewingTime(newValue);
			};
			case REMOVE -> brewingStand -> {
				int current = brewingStand.getBrewingTime();
				int newValue = Math2.fit(0, current - providedValue, Integer.MAX_VALUE);
				brewingStand.setBrewingTime(newValue);
			};
			case SET -> {
				int newValue = Math2.fit(0, providedValue, Integer.MAX_VALUE);
				yield brewingStand -> {
					brewingStand.setBrewingTime(newValue);
				};
			}
			case DELETE -> brewingStand -> {
				brewingStand.setBrewingTime(0);
			};
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		};

		for (Block block : getExpr().getArray(event)) {
			if (!(block.getState() instanceof BrewingStand brewingStand))
				continue;
			consumer.accept(brewingStand);
		}
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "brewing time";
	}

}
