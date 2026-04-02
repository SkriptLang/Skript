package ch.njol.skript.expressions;

import org.bukkit.entity.Egg;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;

@Name("The Egg")
@Description("The egg hurled forth in a Player Egg Throw event.")
@Example("spawn an egg at the hurled egg")

@Since("2.7")
public class ExprEgg extends EventValueExpression<Egg> {

	static {
		register(ExprEgg.class, Egg.class, "[hurled] egg");
	}

	public ExprEgg() {
		super(Egg.class, true);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the egg";
	}

}
