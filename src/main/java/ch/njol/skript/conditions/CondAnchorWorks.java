package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Do Respawn Anchors Work")
@Description("Checks whether or not respawn anchors work in a world.")
@Examples("respawn anchors work in world \"world_nether\"")
@RequiredPlugins("Minecraft 1.16+")
@Since("2.7")
public class CondAnchorWorks extends Condition {

	static {
		if (Skript.classExists("org.bukkit.block.data.type.RespawnAnchor"))
			Skript.registerCondition(CondAnchorWorks.class, "respawn anchors [do[1:(n't| not)]] work in %worlds%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<World> worlds;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		worlds = (Expression<World>) exprs[0];
		setNegated(parseResult.mark == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return worlds.check(event, World::isRespawnAnchorWorks, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "respawn anchors " + (isNegated() ? " do" : " don't") + " work in " + worlds.toString(event, debug);
	}

}
