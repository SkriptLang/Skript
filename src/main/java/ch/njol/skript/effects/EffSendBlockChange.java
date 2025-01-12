package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Send Block Change")
@Description("Makes a player see a block as something it really isn't.")
@Examples({
	"make player see block at player as dirt",
	"make player see target block as campfire[facing=south]"
})
@RequiredPlugins("Minecraft 1.13+ for BlockData support")
@Since("2.2-dev37c, 2.5.1 (block data support)")
public class EffSendBlockChange extends Effect {

	private static final boolean BLOCK_DATA_SUPPORT = Skript.classExists("org.bukkit.block.data.BlockData");
	private static final boolean SUPPORTED = Skript.methodExists(Player.class, "sendBlockChange", Location.class, Material.class, byte.class);

	static {
		Skript.registerEffect(EffSendBlockChange.class,
			BLOCK_DATA_SUPPORT ? "make %players% see %blocks% as %itemtype/blockdata%" : "make %players% see %blocks% as %itemtype%"
		);
	}

	private Expression<Player> players;
	private Expression<Block> blocks;
	private Expression<Object> as;
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!SUPPORTED) {
			Skript.error("The send block change effect is not supported on this version. " +
				"If Spigot has added a replacement method without magic values " +
				"please open an issue at https://github.com/SkriptLang/Skript/issues " +
				"and support will be added for it.");
			return false;
		}
		players = (Expression<Player>) exprs[0];
		blocks = (Expression<Block>) exprs[1];
		as = (Expression<Object>) exprs[2];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Object object = this.as.getSingle(event);
		if (object instanceof ItemType itemType) {
			for (Player player : players.getArray(event)) {
				for (Block block : blocks.getArray(event)) {
					itemType.sendBlockChange(player, block.getLocation());
				}
			}
		} else if (BLOCK_DATA_SUPPORT && object instanceof BlockData blockData) {
			for (Player player : players.getArray(event)) {
				for (Block block : blocks.getArray(event)) {
					player.sendBlockChange(block.getLocation(), blockData);
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("make", players, "see", blocks, "as", as)
			.toString();
	}

}
