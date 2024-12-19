package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Item;

@Name("Will Despawn")
@Description("Checks if the dropped item will be despawned naturally through Minecraft's timer.")
@Examples({
	"if all dropped items can despawn naturally:",
		"\tprevent all dropped items from naturally despawning"
})
@Since("INSERT VERSION")
public class CondItemLifetime extends PropertyCondition<Item> {

	static {
		PropertyCondition.register(CondItemLifetime.class, PropertyType.CAN, "(despawn naturally|naturally despawn)", "itementities");
	}

	private Expression<Item> entities;
	private boolean canDespawn;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		canDespawn = matchedPattern == 0;
		//noinspection unchecked
		entities = (Expression<Item>) exprs[0];
		return true;
	}

	@Override
	public boolean check(Item item) {
		return item.isUnlimitedLifetime();
	}

	@Override
	protected String getPropertyName() {
		return "naturally despawn";
	}

}
