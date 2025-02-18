package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

@Name("Applied Beacon Effect")
@Description("The type of effect applied by a beacon.")
@Examples({
	"on beacon effect:",
		"\tif the applied effect is primary beacon effect:",
			"\t\tbroadcast \"Is Primary\"",
		"\telse if applied effect = secondary effect:",
			"\t\tbroadcast \"Is Secondary\""
})
@Events("Beacon Effect")
@RequiredPlugins("Paper")
@Since("2.10")
public class ExprAppliedEffect extends SimpleExpression<PotionEffectType> {

	static {
		if (Skript.classExists("com.destroystokyo.paper.event.block.BeaconEffectEvent")) {
			Skript.registerExpression(ExprAppliedEffect.class, PotionEffectType.class, ExpressionType.SIMPLE, "[the] applied [beacon] effect");
		}
	}


	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(BeaconEffectEvent.class)) {
			Skript.error("You can only use 'applied effect' in a beacon effect event.");
			return false;
		}
		return true;
	}

	@Override
	protected PotionEffectType @Nullable [] get(Event event) {
		if (!(event instanceof BeaconEffectEvent effectEvent))
			return null;
		return new PotionEffectType[]{effectEvent.getEffect().getType()};
	}

	@Override
	public Class<PotionEffectType> getReturnType() {
		return PotionEffectType.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "applied effect";
	}

}
