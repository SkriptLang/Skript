package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import io.papermc.paper.world.MoonPhase;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

@Name("Moon Phase")
@Description("The current moon phase of a world.")
@Example("""
	if moon phase of player's world is full moon:
		send "Watch for the wolves!"
	""")
@Since("2.7")
public class ExprMoonPhase extends SimplePropertyExpression<World, MoonPhase> {

	static {
		// TODO - remove this when Spigot support is dropped
		if (Skript.classExists("io.papermc.paper.world.MoonPhase"))
			register(ExprMoonPhase.class, MoonPhase.class, "(lunar|moon) phase[s]", "worlds");
	}

	@Override
	@Nullable
	public MoonPhase convert(World world) {
		return world.getMoonPhase();
	}

	@Override
	public Class<? extends MoonPhase> getReturnType() {
		return MoonPhase.class;
	}

	@Override
	protected String getPropertyName() {
		return "moon phase";
	}

}
