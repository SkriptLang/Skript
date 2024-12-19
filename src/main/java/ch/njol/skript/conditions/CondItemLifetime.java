package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Item Has Unlimited Lifetime")
@Description("Checks if the dropped item has unlimited lifetime enabled or disabled.")
@Examples({
	"if all dropped items have unlimited lifetime disabled:",
		"\tenable unlimited lifetime for all dropped items"
})
@Since("INSERT VERSION")
public class CondItemLifetime extends Condition {

	static {
		Skript.registerCondition(CondItemLifetime.class, ConditionType.PROPERTY,
			"[the] %itementities% (has|have) (unlimited|infinite) life(time|span) [enabled]",
			"(unlimited|infinite) life(time|span) (is|are) enabled for %itementities%",
			"[the] %itementities% (has|have) (unlimited|infinite) life(time|span) disabled",
			"(unlimited|infinite) life(time|span) (is|are) disabled for %itementities%",
			"[the] %itementities% (don't|do not|doesn't|does not) have (unlimited|infinite) life(time|span)");
	}

	private Expression<Item> entities;
	private boolean enabled;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		enabled = matchedPattern <= 1;
		//noinspection unchecked
		entities = (Expression<Item>) exprs[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		return entities.check(event, Item::isUnlimitedLifetime, !enabled);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + entities.toString(event, debug) + " have unlimited lifetime " + (enabled ? "enabled" : "disabled");
	}

}
