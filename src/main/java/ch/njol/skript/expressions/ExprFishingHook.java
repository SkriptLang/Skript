package ch.njol.skript.expressions;

import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.bukkit.entity.FishHook;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Fishing Hook")
@Description("The <a href='classes.html#entity'>fishing hook</a> in a fishing event.")
@Examples({
	"on fishing:",
	"\twait a second",
	"\tteleport player to fishing hook"
})
@Events("Fishing")
@Since("INSERT VERSION")
public class ExprFishingHook extends EventValueExpression<FishHook> {

	static {
		register(ExprFishingHook.class, FishHook.class, "fish[ing](-| )hook");
	}

	public ExprFishingHook() {
		super(FishHook.class);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the fishing hook";
	}

}
