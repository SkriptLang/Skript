package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Item Despawn")
@Description("Prevent a dropped item from naturally despawning through Minecraft's timer.")
@Examples({
	"prevent all dropped items from naturally despawning",
	"allow all dropped items to naturally despawn"
})
@Since("INSERT VERSION")
public class EffItemLifetime extends Effect {

	static {
		Skript.registerEffect(EffItemLifetime.class,
			"(prevent|disallow) %itementities% from (naturally despawning|despawning naturally)",
			"allow %itementities% to (naturally despawn|despawn naturally)");
	}

	private Expression<Item> entities;
	private boolean prevent;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		prevent = matchedPattern == 0;
		//noinspection unchecked
		entities = (Expression<Item>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Item item : entities.getArray(event)) {
			item.setUnlimitedLifetime(prevent);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (prevent) {
			builder.append("prevent", entities, "from naturally despawning");
		} else {
			builder.append("allow", entities, "to naturally despawn");
		}
		return builder.toString();
	}

}
