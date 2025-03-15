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
	"Changes the entity size of slimes and phantoms, this is not the same as changing the scale attribute of an entity.",
	"When changing the size of a slime, its health is fully resorted and will have changes done to the MAX_HEALTH, MOVEMENT_SPEED and ATTACK_DAMAGE attributes",
	"The minimum size of a slime is one and the minimum size of a phantom is 0"
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
			// We follow nbt format of 0-126 for slimes, bukkit uses a 1-127 value
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

		double deltaValueDouble = delta != null ? ((Number) delta[0]).doubleValue() : -1;
		if (Double.isNaN(deltaValueDouble) || Double.isInfinite(deltaValueDouble))
			return;
		int deltaValue = (int) deltaValueDouble;
		if (mode == ChangeMode.REMOVE)
			deltaValue = -deltaValue;

		switch (mode) {
			case ADD, REMOVE -> {
				for (LivingEntity entity : getExpr().getArray(event)) {
					if (entity instanceof Phantom phantom) {
						int newSize = Math2.fit(0, (phantom.getSize() + deltaValue), MAXIMUM_PHANTOM_SIZE);
						phantom.setSize(newSize);
					} else if (entity instanceof Slime slime) {
						int newSize = Math2.fit(1, (slime.getSize() + deltaValue), MAXIMUM_SLIME_SIZE);
						slime.setSize(newSize);
					}
				}
			}
			case SET -> {
				for (LivingEntity entity : getExpr().getArray(event)) {
					if (entity instanceof Phantom phantom) {
						phantom.setSize(Math2.fit(0, deltaValue, Integer.MAX_VALUE));
					} else if (entity instanceof Slime slime) {
						// We follow nbt format of 0-126 for slimes, bukkit uses a 1-127 value
						slime.setSize(Math2.fit(1, deltaValue+1, MAXIMUM_SLIME_SIZE));
					}
				}
			}
			case RESET -> {
				for (LivingEntity entity : getExpr().getArray(event)) {
					if (entity instanceof Phantom phantom) {
						phantom.setSize(0);
					} else if (entity instanceof Slime slime) {
						slime.setSize(1);
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
