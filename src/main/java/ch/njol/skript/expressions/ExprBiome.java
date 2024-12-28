package ch.njol.skript.expressions;

import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Biome")
@Description({"The biome at a certain location. Please note that biomes are only defined for x/z-columns",
	"(i.e. the <a href='#ExprAltitude'>altitude</a> (y-coordinate) doesn't matter), up until Minecraft 1.15.x.",
	"As of Minecraft 1.16, biomes are now 3D (per block vs column)."})
@Examples({"# damage player in deserts constantly",
		"every real minute:",
		"	loop all players:",
		"		biome at loop-player is desert",
		"		damage the loop-player by 1"})
@Since("1.4.4, 2.6.1 (3D biomes)")
public class ExprBiome extends PropertyExpression<Location, Biome> {

	static {
		Skript.registerExpression(ExprBiome.class, Biome.class, ExpressionType.PROPERTY, "[the] biome [(of|%direction%) %locations%]", "%locations%'[s] biome");
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(matchedPattern == 1 ? (Expression<? extends Location>) exprs[0] : Direction.combine((Expression<? extends Direction>) exprs[0], (Expression<? extends Location>) exprs[1]));
		return true;
	}

	@Override
	protected Biome[] get(Event event, Location[] source) {
		return get(source, location -> location.getBlock().getBiome());
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return new Class[] {Biome.class};
		return super.acceptChange(mode);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (mode != ChangeMode.SET) {
			super.change(event, delta, mode);
			return;
		}
		assert delta != null;
		Biome biome = (Biome) delta[0];
		for (Location location : getExpr().getArray(event))
			location.getBlock().setBiome(biome);
	}

	@Override
	public Class<? extends Biome> getReturnType() {
		return Biome.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the biome at " + getExpr().toString(event, debug);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean setTime(int time) {
		super.setTime(time, getExpr());
		return true;
	}

}
