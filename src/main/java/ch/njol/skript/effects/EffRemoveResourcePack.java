package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.UUID;

@Name("Remove Resource Pack")
@Description({
	"Removes all server resource packs, or a single server resource pack specified by UUID, from the given players.",
	"You can only choose to remove all packs or by UUID, not both",
	"",
	"UUIDs must be valid using the following format: ",
	"\"00000000-0000-0000-0000-000000000000\"",
})
@Examples({
	"remove all resource packs from all players",
	"remove resource pack with the uuid \"00000000-0000-0000-0000-0000000000001\" from all players"
})
@Since("INSERT VERSION")
public class EffRemoveResourcePack extends Effect {

	static {
		Skript.registerEffect(EffRemoveResourcePack.class,
			"remove all resource pack[s] from %players%",
			"remove [a|the] resource pack with [the] [uu]id %string% from %players%"
		);
	}

	private int pattern;
	@UnknownNullability
	private Expression<String> id;
	@UnknownNullability
	private Expression<Player> recipients;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		pattern = matchedPattern;
		if (matchedPattern == 1) {
			id = (Expression<String>) exprs[0];
			recipients = (Expression<Player>) exprs[1];
		} else {
			recipients = (Expression<Player>) exprs[0];
		}

		return true;
	}

	@Override
	protected void execute(Event event) {
		UUID uuid = null;
		if (pattern == 1) {
			try {
				uuid = UUID.fromString(id.getSingle(event));
			} catch (IllegalArgumentException exception) {
				Skript.error("Remove Resource Pack UUID failed: " + exception.getLocalizedMessage());
			}
			if (uuid == null) {
				return;
			}
		}
		if (uuid == null) {
			for (Player player : recipients.getArray(event)) {
				player.removeResourcePacks();
			}
		} else {
			for (Player player : recipients.getArray(event)) {
				player.removeResourcePack(uuid);
			}
		}
	}



	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (pattern == 0)
			return "remove all resource packs from " + recipients.toString(event, debug);
		return "remove resource pack with the uuid " + id.toString(event, debug) + " from " + recipients.toString(event, debug);
	}

}
