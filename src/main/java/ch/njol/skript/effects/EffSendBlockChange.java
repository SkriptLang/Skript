package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Send Block Change")
@Description("Makes a player see a block as something else or as the original.")
@Example("make player see block at player as dirt")
@Example("make player see player's target block as campfire[facing=south]")
@Example("""
	make all players see (blocks in radius 5 of location(0, 0, 0)) as bedrock
	make all players see (blocks in radius 5 of location(0, 0, 0)) as original
	""")
@Since("2.2-dev37c, 2.5.1 (block data support), 2.12 (as original)")
public class EffSendBlockChange extends Effect {

	static {
		Skript.registerEffect(EffSendBlockChange.class,
			"make %players% see %locations% as %itemtype/blockdata%",
			"make %players% see %locations% as [the|its] (original|normal|actual) [block]"
		);
	}

	private Expression<Player> players;
	private Expression<Location> locations;
	private @Nullable Expression<Object> type;
	private boolean asOriginal;
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) exprs[0];
		locations = (Expression<Location>) exprs[1];
		asOriginal = matchedPattern == 1;
		if (!asOriginal)
			type = (Expression<Object>) exprs[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (asOriginal) {
			Player[] players = this.players.getArray(event);
			for (Location location : locations.getArray(event)) {
				for (Player player : players)
					player.sendBlockChange(location, location.getBlock().getBlockData());
			}
			return;
		}
		assert type != null;
		Object type = this.type.getSingle(event);
		if (type == null)
			return;
		Player[] players = this.players.getArray(event);
		if (type instanceof ItemType itemType) {
			for (Location location : locations.getArray(event))  {
				for (Player player : players)
					itemType.sendBlockChange(player, location);
			}
		} else if (type instanceof BlockData blockData) {
			for (Location location : locations.getArray(event)) {
				for (Player player : players)
					player.sendBlockChange(location, blockData);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("make", players, "see", locations, "as");
		if (asOriginal) {
			builder.append("original");
		} else {
			assert type != null;
			builder.append(type);
		}
		return builder.toString();
	}

}
