package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Slime;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Entity Size")
@Description({
	"Changes the entity size of slimes and phantoms. This is not the same as changing the scale attribute of an entity.",
	"When changing the size of a slime, its health is fully resorted and will have changes done to its max health, movement speed and attack damage.",
	"The default minecraft size of a slime is anywhere between 0 and 2, with a maximum of 126.",
	"The default minecraft size of a phantom is 0 with a maximum size of 64."
})
@Example("""
	spawn a slime at player:
		set entity size of event-entity to 5
		set name of event-entity to "King Slime Jorg"
	""")
@Since("INSERT VERSION")
public class ExprEntitySize extends SimplePropertyExpression<LivingEntity, Integer> {

	private static final int MAXIMUM_SLIME_SIZE = 127;
	private static final int MAXIMUM_PHANTOM_SIZE = 64;

	static {
		register(ExprEntitySize.class, Integer.class, "entity size", "livingentities");
	}

	@Override
	public @Nullable Integer convert(LivingEntity from) {
		if (from instanceof Phantom phantom) {
			return phantom.getSize();
		} else if (from instanceof Slime slime) {
			// Skript follows the nbt format of 0-126 for slimes, as bukkit uses a 1-127 value
			return slime.getSize()-1;
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE, SET -> CollectionUtils.array(Number.class);
			case RESET -> CollectionUtils.array();
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta == null && mode != ChangeMode.RESET)
			return;

		double deltaSizeDouble = delta != null ? ((Number) delta[0]).doubleValue() : 0;
		if (Double.isNaN(deltaSizeDouble) || Double.isInfinite(deltaSizeDouble))
			return;
		// Due to how double to int conversions happen, we are required to reassign from delta to prevent integer overflows
		int sizeDelta = delta != null ? ((Number) delta[0]).intValue() : 0;
		if (mode == ChangeMode.REMOVE)
			sizeDelta = -sizeDelta;

		switch (mode) {
			case ADD, REMOVE -> {
				for (LivingEntity entity : getExpr().getArray(event)) {
					if (entity instanceof Phantom phantom) {
						int newSize = Math2.fit(0, (phantom.getSize() + sizeDelta), MAXIMUM_PHANTOM_SIZE);
						phantom.setSize(newSize);
					} else if (entity instanceof Slime slime) {
						int newSize = Math2.fit(1, (slime.getSize() + sizeDelta), MAXIMUM_SLIME_SIZE);
						slime.setSize(newSize);
					}
				}
			}
			case SET, RESET -> {
				for (LivingEntity entity : getExpr().getArray(event)) {
					if (entity instanceof Phantom phantom) {
						phantom.setSize(Math2.fit(0, sizeDelta, MAXIMUM_PHANTOM_SIZE));
					} else if (entity instanceof Slime slime) {
						// Skript follows the nbt format of 0-126 for slimes, as bukkit uses a 1-127 value
						slime.setSize(Math2.fit(1, sizeDelta+1, MAXIMUM_SLIME_SIZE));
					}
				}
			}
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "entity size";
	}

}
