package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.destroystokyo.paper.event.block.BeaconEffectEvent;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

@Name("Beacon Effect")
@Description("Called when a player gets an effect from a beacon.")
@Examples({
	"on beacon effect:",
		"\tbroadcast applied effect",
		"\tbroadcast event-player",
		"\tbroadcast event-block",
	"on primary beacon effect apply of haste:",
	"on application of secondary beacon effect:",
	"on beacon effect of speed:"
})
@RequiredPlugins("Paper")
@Since("INSERT VERSION")
public class EvtBeaconEffect extends SkriptEvent {

	static {
		if (Skript.classExists("com.destroystokyo.paper.event.block.BeaconEffectEvent"))
			Skript.registerEvent("Beacon Effect", EvtBeaconEffect.class, BeaconEffectEvent.class,
				"[:primary|:secondary] beacon effect [of %-potioneffecttypes%]",
				"application of [:primary|:secondary] beacon effect [of %-potioneffecttypes%]",
				"[:primary|:secondary] beacon effect apply [of %-potioneffecttypes%]");

	}

	private @Nullable Literal<PotionEffectType> potionTypes;
	private @Nullable Boolean primaryCheck;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		potionTypes = (Literal<PotionEffectType>) exprs[0];
		if (parseResult.hasTag("primary")) {
			primaryCheck = true;
		} else if (parseResult.hasTag("secondary")) {
			primaryCheck = false;
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof BeaconEffectEvent effectEvent))
			return false;
		if (primaryCheck != null && effectEvent.isPrimary() != primaryCheck)
			return false;
		if (potionTypes != null)
			return potionTypes.check(event, type -> effectEvent.getEffect().getType() == type);
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (primaryCheck == null ? "" : primaryCheck ? "primary " : "secondary ") +
			"beacon effect" + (potionTypes == null ? "" : " of " + potionTypes.toString(event, debug));
	}

}
