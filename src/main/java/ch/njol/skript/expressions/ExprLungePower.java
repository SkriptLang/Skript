package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.event.entity.EntityLungeEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Lunge Power")
@Description("""
	The power of lunge attack.
	Can be set to modify the distance of the lunge attack.
	Initially, the lunge power is determined by the enchantment level of the lunge enchantment of the weapon used to perform the lunge attack (e.g. a spear).
	""")
@Example("""
	on skeleton lunge:
		if the lunge power is 1, 2 or 3:
			broadcast "Normal lunge power"
		else if the lunge power is greater than 3:
			broadcast "Overpowered lunge power"
	""")
@Example("""
	on lunge:
		set event-lunge power to 5
	""")
@Example("""
	on player lunge:
		if event-entity has slowness:
			remove 1 from lunge power
			send "Slowed you down a bit"
	""")
@Since("INSERT VERSION")
public class ExprLungePower extends SimpleExpression<Integer> implements EventRestrictedSyntax {

	static {
		// Since paper 26.1.2
		if (Skript.classExists("io.papermc.paper.event.entity.EntityLungeEvent")) {
			Skript.registerExpression(ExprLungePower.class, Integer.class, ExpressionType.SIMPLE, "[the] [event-]lunge power");
		}
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(EntityLungeEvent.class);
	}

	@Override
	public Integer[] get(Event event) {
		if (event instanceof EntityLungeEvent lungeEvent)
		{
			return new Integer[]{lungeEvent.getLungePower()};
		}
		return new Integer[0];
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, ADD, REMOVE -> CollectionUtils.array(Integer.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof EntityLungeEvent lungeEvent)) {
			return;
		}

		int deltaValue = delta == null ? 0 : (int) delta[0];
		int currentValue = lungeEvent.getLungePower();
		int newValue = switch (mode) {
			case SET -> deltaValue;
			case ADD -> currentValue + deltaValue;
			case REMOVE -> currentValue - deltaValue;
			default -> throw new UnsupportedOperationException("Unsupported change mode: " + mode);
		};

		lungeEvent.setLungePower(newValue);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "lunge power";
	}

}
