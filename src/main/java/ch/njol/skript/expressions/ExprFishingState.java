package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ExpressionType;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.jetbrains.annotations.Nullable;

@Name("Fishing State")
@Description("The <a href='/classes.html#fishingstate'>fishing state</a> of a fishing event.")
@Examples("fishing state is failed or in ground")
@Since("INSERT VERSION")
public class ExprFishingState extends EventValueExpression<State> {
	
	static {
		Skript.registerExpression(ExprFishingState.class, State.class, ExpressionType.SIMPLE, "[the] [event-]fish[ing]( |-)state");
	}
	
	public ExprFishingState() {
		super(State.class);
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "fishing state";
	}
	
}
