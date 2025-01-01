package ch.njol.skript.conditions;

import ch.njol.skript.ScriptLoader;
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
import org.skriptlang.skript.lang.script.Script;

@Name("Is Loaded")
@Description({
		"Checks whether a world, chunk or script is loaded.",
		"'chunk at 1, 1' uses chunk coordinates, which are location coords divided by 16."
})
@Examples({"if chunk at {home::%player's uuid%} is loaded:",
		"if chunk 1, 10 in world \"world\" is loaded:",
		"if world(\"lobby\") is loaded:",
		"if script named \"MyScript.sk\" is loaded:"
})
@Since("2.3, 2.5 (revamp with chunk at location/coords), INSERT VERSION (Scripts)")
@SuppressWarnings("unchecked")
public class CondIsLoaded extends Condition {

	static {
		Skript.registerCondition(CondIsLoaded.class,
			"chunk[s] %directions% [%locations%] (is|are)[(1¦(n't| not))] loaded",
			"chunk [at] %number%, %number% (in|of) [world] %world% is[(1¦(n't| not))] loaded",
			"%scripts/worlds% (is|are)[1:(n't| not)] loaded",
			"script[s] %scripts% (is|are)[1:(n't| not)] loaded",
			"world[s] %worlds% (is|are)[1:(n't| not)] loaded");
	}

	private Expression<Location> locations;
	private Expression<Number> x, z;
	private Expression<?> objects;
	private int pattern;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		pattern = matchedPattern;
		switch (pattern) {
			case 0:
				locations = Direction.combine((Expression<? extends Direction>) exprs[0], (Expression<? extends Location>) exprs[1]);
				break;
			case 1:
				x = (Expression<Number>) exprs[0];
				z = (Expression<Number>) exprs[1];
				objects = exprs[2];
				break;
			case 2:
			case 3:
			case 4:
				objects = exprs[0];
		}
		setNegated(parseResult.mark == 1);
		return true;
	}

	@Override
	@SuppressWarnings("null")
	public boolean check(Event e) {
		return switch (pattern) {
			case 0 -> locations.check(e, location -> {
				World world = location.getWorld();
				if (world != null)
					return world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
				return false;
			}, isNegated());
			case 1 -> objects.check(e, object -> {
				if (!(object instanceof World world))
					return false;
				Number x = this.x.getSingle(e);
				Number z = this.z.getSingle(e);
				if (x == null || z == null)
					return false;
				return world.isChunkLoaded(x.intValue(), z.intValue());
			}, isNegated());
			case 2, 4 -> objects.check(e, object -> {
				if (object instanceof World world) {
					return Bukkit.getWorld(world.getName()) != null;
				} else if (object instanceof Script script) {
					return ScriptLoader.getLoadedScripts().contains(script);
				}
				return false;
			}, isNegated());
			case 3 -> objects.check(e, ScriptLoader.getLoadedScripts()::contains, isNegated());
			default -> false;
		};
	}

	@Override
	@SuppressWarnings("null")
	public String toString(@Nullable Event e, boolean d) {
		String neg = isNegated() ? " not " : " ";
		return switch (pattern) {
			case 0 ->
				"chunk[s] at " + locations.toString(e, d) + (locations.isSingle() ? " is" : " are") + neg + "loaded";
			case 1 ->
				"chunk at " + x.toString(e, d) + ", " + z.toString(e, d) + " in " + objects.toString(e, d) + ") is" + neg + "loaded";
			case 3 ->
				"scripts " + this.objects.toString(e, d) + (this.objects.isSingle() ? " is" : " are") + neg + "loaded";
			case 4 ->
				"worlds " + this.objects.toString(e, d) + (this.objects.isSingle() ? " is" : " are") + neg + "loaded";
			default -> this.objects.toString(e, d) + (this.objects.isSingle() ? " is" : " are") + neg + "loaded";
		};
	}

}
