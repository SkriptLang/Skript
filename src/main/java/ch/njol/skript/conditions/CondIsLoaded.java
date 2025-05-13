package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Is Loaded")
@Description("Checks whether or not a chunk/world is loaded. 'chunk at 1, 1' uses chunk coords, which are location coords divided by 16.")
@Examples({"if chunk at {home::%player's uuid%} is loaded:",
		"if chunk 1, 10 in world \"world\" is loaded:",
		"if world(\"lobby\") is loaded:"})
@Since("2.3, 2.5 (revamp with chunk at location/coords)")
public class CondIsLoaded extends Condition {
	
	static {
		Skript.registerCondition(CondIsLoaded.class,
			"chunk[s] %directions% [%locations%] (is|are)[(1¦(n't| not))] loaded",
			"chunk [at] %number%, %number% (in|of) [world] %world% is[(1¦(n't| not))] loaded",
			"[world[s]] %worlds% (is|are)[(1¦(n't| not))] loaded");
	}
	
	@Nullable
	private Expression<Location> locations;
	@Nullable
	private Expression<Number> x,z;
	@Nullable
	private Expression<World> world;
	private int pattern;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		pattern = matchedPattern;
		locations = pattern == 0 ? Direction.combine((Expression<? extends Direction>) exprs[0], (Expression<? extends Location>) exprs[1]) : null;
		x = pattern == 1 ? (Expression<Number>) exprs[0] : null;
		z = pattern == 1 ? (Expression<Number>) exprs[1] : null;
		world = pattern == 1 ? (Expression<World>) exprs[2] : pattern == 2 ? (Expression<World>) exprs[0] : null;
		setNegated(parseResult.mark == 1);
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	public boolean check(Event e) {
		switch (pattern) {
			case 0:
				return locations.check(e, location -> {
					World world = location.getWorld();
					if (world != null)
						return world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
					return false;
				}, isNegated());
			case 1:
				return world.check(e, world -> {
					Number x = this.x.getSingle(e);
					Number z = this.z.getSingle(e);
					if (x == null || z == null)
						return false;
					return world.isChunkLoaded(x.intValue(), z.intValue());
				}, isNegated());
			case 2:
				return world.check(e, world -> Bukkit.getWorld(world.getName()) != null, isNegated());
		}
		return false;
	}
	
	@SuppressWarnings("null")
	@Override
	public String toString(@Nullable Event e, boolean d) {
		String neg = isNegated() ? " not " : " ";
		String chunk = pattern == 0 ? "chunk[s] at " + locations.toString(e, d) + (locations.isSingle() ? " is" : " are") + neg + "loaded" : "";
		String chunkC = pattern == 1 ? "chunk (x:" + x.toString(e, d) + ",z:" + z.toString(e, d) + ",w:" + world.toString(e,d) + ") is" + neg + "loaded" : "";
		String world = pattern == 2 ? "world[s] " + this.world.toString(e, d) + (this.world.isSingle() ? " is" : " are") + neg + "loaded" : "";
		return chunk + chunkC + world;
	}
	
}
