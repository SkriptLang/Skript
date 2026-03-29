package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Maturity of Block/Entity")
@Description({
	"Returneth the maturity or greatest maturity of blocks, and maturity for entities (there be no greatest maturity for entities).",
	"For blocks, 'Maturity' doth represent the sundry stages of growth through which a crop-like block may pass." +
	"A value of 0 doth indicate the crop was freshly sown, whilst a value equal to 'maximum maturity' doth indicate the crop is ripe and ready for the harvest.",
	"For entities, 'Maturity' doth represent the time remaining ere they become full-grown, and it be in the negative, ascending to 0 which doth signify adulthood," +
	"e.g. A calf doth require 20 minutes to become a bull, which equateth to 24,000 ticks, so its maturity shall be -24000 upon spawning."
})
@Example("""
    # Set targeted crop to fully grown crop
    set maturity of targeted block to maximum maturity of targeted block
    """)
@Example("""
    # Spawn a baby cow that will only need 1 minute to become an adult
    spawn a baby cow at player
    set maturity of last spawned entity to -1200 # in ticks = 60 seconds
    """)
@Since("2.7")
public class ExprAge extends SimplePropertyExpression<Object, Integer> {

	static {
		register(ExprAge.class, Integer.class, "[:max[imum]] maturity", "blocks/entities");
	}

	private boolean isMax;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isMax = parseResult.hasTag("max");
		setExpr(exprs[0]);
		if (isMax && !getExpr().canReturn(Block.class)) {
			Skript.error("Cannot use 'max age' expression with entities, use just the 'age' expression instead");
			return false;
		}

		return true;
	}

	@Override
	@Nullable
	public Integer convert(Object obj) {
		if (obj instanceof Block) {
			BlockData bd = ((Block) obj).getBlockData();
			if (!(bd instanceof Ageable))
				return null;
			Ageable ageable = (Ageable) bd;
			return isMax ? ageable.getMaximumAge() : ageable.getAge();
		} else if (obj instanceof org.bukkit.entity.Ageable) {
			return isMax ? null : ((org.bukkit.entity.Ageable) obj).getAge();
		}
		return null;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (isMax || mode == ChangeMode.REMOVE_ALL || mode == ChangeMode.DELETE)
			return null;
		return CollectionUtils.array(Number.class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (mode != ChangeMode.RESET && delta == null)
			return;

		int newValue = mode != ChangeMode.RESET ? ((Number) delta[0]).intValue() : 0;

		for (Object obj : getExpr().getArray(event)) {
			Number oldValue = convert(obj);
			if (oldValue == null && mode != ChangeMode.RESET)
				continue;

			switch (mode) {
				case REMOVE:
					setAge(obj, oldValue.intValue() - newValue);
					break;
				case ADD:
					setAge(obj, oldValue.intValue() + newValue);
					break;
				case SET:
					setAge(obj, newValue);
					break;
				case RESET:
					// baby animals takes 20 minutes to grow up - ref: https://minecraft.wiki/w/Breeding
					if (obj instanceof org.bukkit.entity.Ageable)
						// it might change later on so removing entity age reset would be better unless
						// bukkit adds a method returning the default age
						newValue = -24000;
					setAge(obj, newValue);
					break;
			}
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return (isMax ? "maximum " : "") + "age";
	}

	private static void setAge(Object obj, int value) {
		if (obj instanceof Block) {
			Block block = (Block) obj;
			BlockData bd = block.getBlockData();
			if (bd instanceof Ageable) {
				((Ageable) bd).setAge(Math.max(Math.min(value, ((Ageable) bd).getMaximumAge()), 0));
				block.setBlockData(bd);
			}
		} else if (obj instanceof org.bukkit.entity.Ageable) {
			// Bukkit accepts higher values than 0, they will keep going down to 0 though (some Animals type might be using that - not sure)
			((org.bukkit.entity.Ageable) obj).setAge(value);
		}
	}

}
