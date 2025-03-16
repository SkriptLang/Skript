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
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Name("Entity Storage Entity Count")
@Description({
	"The current number of entities stored inside an entity block storage (i.e. beehive).",
	"The maximum amount of entities an entity block storage can hold."
})
@Examples({
	"broadcast the stored entity count of {_beehive}",
	"set the maximum entity count of {_beehive} to 20"
})
@Since("INSERT VERSION")
public class ExprEntityStorageEntityCount extends SimplePropertyExpression<Block, Integer> {

	static {
		registerDefault(ExprEntityStorageEntityCount.class, Integer.class, "[max:max[imum]] [stored] entity count", "blocks");
	}

	private boolean withMax;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		withMax = parseResult.hasTag("max");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable Integer convert(Block block) {
		if (block.getState() instanceof EntityBlockStorage<?> blockStorage) {
			if (withMax)
				return blockStorage.getMaxEntities();
			return blockStorage.getEntityCount();
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (withMax) {
			return switch (mode) {
				case SET, DELETE, RESET, ADD, REMOVE -> CollectionUtils.array(Integer.class);
				default -> null;
			};
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int value = delta != null ? (int) delta[0] : 0;
		Consumer<EntityBlockStorage<?>> consumer = switch (mode) {
			case SET, DELETE -> blockStorage -> blockStorage.setMaxEntities(value);
			case RESET -> blockStorage -> {
				if (blockStorage instanceof Beehive) {
					blockStorage.setMaxEntities(3);
				} else {
					blockStorage.setMaxEntities(1);
				}
			};
			case ADD -> blockStorage -> {
				int current = blockStorage.getMaxEntities();
				blockStorage.setMaxEntities(current + value);
			};
			case REMOVE -> blockStorage -> {
				int current = blockStorage.getMaxEntities();
				blockStorage.setMaxEntities(current - value);
			};
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		};

		for (Block block : getExpr().getArray(event)) {
			if (!(block.getState() instanceof EntityBlockStorage<?> blockStorage))
				continue;
			consumer.accept(blockStorage);
			blockStorage.update(true, false);
		}
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return (withMax ? "maximum " : "") + "stored entity count";
	}

}
