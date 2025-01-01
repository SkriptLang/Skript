package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.EntityUnleashEvent.UnleashReason;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Unleash Reason")
@Description("The unleash reason in an unleash event.")
@Examples({
	"if the unleash reason is distance:",
		"\tbroadcast \"The leash was snapped in half.\""
})
@Events("Unleash")
@Since("INSERT VERSION")
public class ExprUnleashReason extends EventValueExpression<UnleashReason> {

	public ExprUnleashReason() {
		super(UnleashReason.class);
	}

	static {
		Skript.registerExpression(ExprUnleashReason.class, EntityUnleashEvent.UnleashReason.class, ExpressionType.SIMPLE, "[the] unleash[ing] reason");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(EntityUnleashEvent.class)) {
			Skript.error("The 'unleash reason' expression can only be used in an 'unleash' event");
			return false;
		}
		return true;
	}

	@Override
	protected UnleashReason[] get(Event event) {
		if (!(event instanceof EntityUnleashEvent unleashEvent))
			return new UnleashReason[0];
		return new UnleashReason[] {unleashEvent.getReason()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends UnleashReason> getReturnType() {
		return UnleashReason.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the unleash reason";
	}

}
