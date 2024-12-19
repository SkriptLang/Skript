package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Unlimited Item Lifetime")
@Description("Makes a dropped item have unlimited lifetime, meaning it won't despawn from vanilla Minecraft timer.")
@Examples({
	"enabled unlimited lifetime of all dropped items"
})
@Since("INSERT VERSION")
public class EffItemLifetime extends Effect {

	static {
		Skript.registerEffect(EffItemLifetime.class,
			"enable (unlimited|infinite) life(time|span) for [the] %itementities%",
			"make [the] life(time|span) (unlimited|infinite) for [the] %itementities%",
			"make %itementities%'[s] life(time|span) (unlimited|infinite)",
			"disable (unlimited|infinite) life(time|span) for [the] %itementities%",
			"make [the] life(time|span) (limited|finite) for [the] %itementities%",
			"make %itementities%'[s] life(time|span) (limited|finite)");
	}

	private Expression<Item> entities;
	private boolean enable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		enable = matchedPattern <= 2;
		//noinspection unchecked
		entities = (Expression<Item>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Item item : entities.getArray(event)) {
			item.setUnlimitedLifetime(enable);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (enable ? "enable" : "disable") + " unlimited lifetime of " + entities.toString(event, debug);
	}

}
