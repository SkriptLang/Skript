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

	// The minimum size of a slime is one, whereas phantoms' are 0
	// Setting a slime size to 1 is the same as 0, however 2 is not the same as 1
	private static final int MINIMUM_SLIME_SIZE = 1;
	private static final int MINIMUM_PHANTOM_SIZE = 0;

	static {
		register(ExprEntitySize.class, Integer.class, "entity size", "livingentities");
	}

	@Override
	public @Nullable Integer convert(LivingEntity from) {
		if (from instanceof Phantom phantom) {
			return phantom.getSize();
		} else if (from instanceof Slime slime) {
			return slime.getSize();
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

		int deltaValue = delta != null ? ((Number) delta[0]).intValue() : -1;
		if (mode == ChangeMode.REMOVE)
			deltaValue = -deltaValue;

		switch (mode) {
			case ADD, REMOVE -> {
				for (LivingEntity entity : getExpr().getArray(event)) {
					if (entity instanceof Phantom phantom) {
						int newSize = Math2.fit(MINIMUM_PHANTOM_SIZE, (phantom.getSize() + deltaValue), Integer.MAX_VALUE);
						phantom.setSize(newSize);
					} else if (entity instanceof Slime slime) {
						int newSize = Math2.fit(MINIMUM_SLIME_SIZE, (slime.getSize() + deltaValue), Integer.MAX_VALUE);
						slime.setSize(newSize);
					}
				}
			}
			case SET -> {
				for (LivingEntity entity : getExpr().getArray(event)) {
					if (entity instanceof Phantom phantom) {
						phantom.setSize(Math2.fit(MINIMUM_PHANTOM_SIZE, deltaValue, Integer.MAX_VALUE));
					} else if (entity instanceof Slime slime) {
						slime.setSize(Math2.fit(MINIMUM_SLIME_SIZE, deltaValue, Integer.MAX_VALUE));
					}
				}
			}
			case RESET -> {
				for (LivingEntity entity : getExpr().getArray(event)) {
					if (entity instanceof Phantom phantom) {
						phantom.setSize(MINIMUM_PHANTOM_SIZE);
					} else if (entity instanceof Slime slime) {
						slime.setSize(MINIMUM_SLIME_SIZE);
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
