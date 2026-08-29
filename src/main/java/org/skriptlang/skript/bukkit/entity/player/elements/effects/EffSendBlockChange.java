package org.skriptlang.skript.bukkit.entity.player.elements.effects;

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
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.HashMap;
import java.util.Map;

@Name("Send Block Change")
@Description("""
	 Makes a player see a block as something else or as the original.
	 This will not actually change the world in any way.
	 """)
@Example("make player see block at player as dirt")
@Example("make player see player's target block as campfire[facing=south]")
@Example("""
	make all players see (blocks in radius 5 of location(0, 0, 0)) as bedrock
	make all players see (blocks in radius 5 of location(0, 0, 0)) as original
	""")
@Since("2.2-dev37c, 2.5.1 (block data support), 2.12 (as original)")
@SuppressWarnings("UnstableApiUsage")
public class EffSendBlockChange extends Effect {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffSendBlockChange.class)
			.supplier(EffSendBlockChange::new)
			.addPatterns("make %players% see %locations% as %itemtype/blockdata%",
				"make %players% see %locations% as [the|its] (original|normal|actual) [block]")
			.build());
	}

	private Expression<Player> players;
	private Expression<Location> locations;
	private @Nullable Expression<Object> type;
	private boolean asOriginal;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) expressions[0];
		locations = (Expression<Location>) expressions[1];
		asOriginal = matchedPattern == 1;
		if (!asOriginal)
			type = (Expression<Object>) expressions[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Player[] players = this.players.getArray(event);
		Location[] locations = this.locations.getArray(event);

		if (asOriginal) {
			Map<Location, BlockData> changes = new HashMap<>();
			for (Location location : locations)
				changes.put(location, location.getBlock().getBlockData());
			for (Player player : players)
				player.sendMultiBlockChange(changes);
			return;
		}

		if (this.type == null)
			return;
		Object type = this.type.getSingle(event);
		if (type == null)
			return;
		Map<Location, BlockData> changes = new HashMap<>();

		if (type instanceof ItemType itemType) {
			for (Location location : locations)
				changes.put(location, itemType.getMaterial().createBlockData());
		} else if (type instanceof BlockData blockData) {
			for (Location location : locations)
				changes.put(location, blockData);
		}
		for (Player player : players)
			player.sendMultiBlockChange(changes);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("make", players, "see", locations, "as")
			.appendIf(asOriginal, "original")
			.appendIf(!asOriginal, type)
			.toString();
	}

}
