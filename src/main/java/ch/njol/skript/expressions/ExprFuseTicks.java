package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Fuse Ticks")
@Description("The fuse ticks of a creeper or primed TNT. This is how many ticks remain before the entity explodes.")
@Example("""
	on spawn of a creeper:
		set the fuse ticks of the event-entity to 100
	""")
@Example("""
	on spawn of primed tnt:
		set fuse ticks of event-entity to 200
	""")
@Since("INSERT VERSION")
public class ExprFuseTicks extends SimplePropertyExpression<Entity, Number> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprFuseTicks.class, Number.class, "fuse tick[s]", "entities", false).build()
		);
	}

	@Override
	public @Nullable Number convert(Entity entity) {
		if (entity instanceof Creeper creeper)
			return creeper.getMaxFuseTicks();
		else if (entity instanceof TNTPrimed tnt)
			return tnt.getFuseTicks();
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, ADD, REMOVE -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int value = delta != null ? Math2.fit(0, ((Number) delta[0]).intValue(), Integer.MAX_VALUE) : 0;
		for (Entity entity : getExpr().getArray(event)) {
			if (entity instanceof Creeper creeper) {
				creeper.setMaxFuseTicks(Math2.fit(0, switch (mode) {
					case SET, DELETE -> value;
					case ADD -> creeper.getMaxFuseTicks() + value;
					case REMOVE -> creeper.getMaxFuseTicks() - value;
					default -> throw new IllegalArgumentException("Unexpected mode: " + mode);
				}, Integer.MAX_VALUE));
			} else if (entity instanceof TNTPrimed tnt) {
				tnt.setFuseTicks(Math2.fit(0, switch (mode) {
					case SET, DELETE -> value;
					case ADD -> tnt.getFuseTicks() + value;
					case REMOVE -> tnt.getFuseTicks() - value;
					default -> throw new IllegalArgumentException("Unexpected mode: " + mode);
				}, Integer.MAX_VALUE));
			}
		}
	}

	@Override
	public Class<Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "fuse ticks";
	}

}