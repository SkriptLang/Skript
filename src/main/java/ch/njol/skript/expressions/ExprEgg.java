package ch.njol.skript.expressions;

import ch.njol.skript.doc.*;
import org.bukkit.entity.Egg;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.expressions.base.EventValueExpression;

@Name("The Egg")
@Description("The egg thrown in a Player Egg Throw event.")
@Examples("spawn an egg at the egg")
@AvailableEvent(PlayerEggThrowEvent.class)
@Events("Egg Throw")
@Since("2.7")
public class ExprEgg extends EventValueExpression<Egg> {

	static {
		register(ExprEgg.class, Egg.class, "[thrown] egg");
	}

	public ExprEgg() {
		super(Egg.class, true);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the egg";
	}

}
