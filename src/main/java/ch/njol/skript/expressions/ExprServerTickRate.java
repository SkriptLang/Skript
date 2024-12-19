package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.ServerUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Server Tick Rate")
@Description({
	"Gets or sets the current tick rate of the server. The tick rate is the number of game ticks that occur in a second. Higher values mean the game runs faster.",
	"The server's default tick rate is 20."
})
@Examples({
	"send \"%server's tick rate%\" to player",
	"set server's tick rate to 20 # This is the default tick rate.",
	"add 5 to server's tick rate",
	"remove 2 from server's tick rate"
})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class ExprServerTickRate extends SimpleExpression<Float> {

	static {
		if (ServerUtils.isServerTickManagerPresent())
			Skript.registerExpression(ExprServerTickRate.class, Float.class, ExpressionType.SIMPLE, "[the] server['s] tick rate");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Nullable
	@Override
	protected Float[] get(Event event) {
		return new Float[] {ServerUtils.getServerTickManager().getTickRate()};
	}

	public Class<?>[] acceptChange(ChangeMode mode) {
		switch (mode) {
			case SET, ADD, REMOVE, RESET -> return CollectionUtils.array(Number.class);
			default -> return null;
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		float tickRate = ServerUtils.getServerTickManager().getTickRate();
		float change = delta != null ? ((Number) delta[0]).floatValue() : 0;
		float newTickRate = tickRate;

		switch (mode) {
			case SET:
				newTickRate = change;
				break;
			case ADD:
				newTickRate = tickRate + change;
				break;
			case REMOVE:
				newTickRate = tickRate - change;
				break;
			case RESET:
				newTickRate = 20;
				break;
		}

		newTickRate = Math2.fit(newTickRate, 1.0f, 10000f);

		ServerUtils.getServerTickManager().setTickRate(newTickRate);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the server's tick rate";
	}

}
