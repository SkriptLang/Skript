package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.event.entity.EntityLungeEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Lunge Power")
@Description({"The power of lunge attack.",
			"Can be set to modify the distance of the lunge attack.",
			"Initially, the lunge power is determined by the enchantment level of the lunge enchantment" +
				"of the weapon used to perform the lunge attack (e.g. a spear)."})
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
@Since("INSERT VERSION")
public class ExprLungePower extends WrapperExpression<Integer> implements EventRestrictedSyntax {

	static {
		// Since paper 26.1.2
		if (Skript.classExists("io.papermc.paper.event.entity.EntityLungeEvent")) {
			Skript.registerExpression(ExprLungePower.class, Integer.class, ExpressionType.SIMPLE, "[the] [event-]lunge power");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (exprs.length != 0) {
			return false;
		}

		setExpr(new EventValueExpression<>(Integer.class));
		return ((EventValueExpression<Integer>) getExpr()).init();
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(EntityLungeEvent.class);
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
