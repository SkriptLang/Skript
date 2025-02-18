package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Since;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import io.papermc.paper.world.MoonPhase;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

@Name("Moon Phase")
@Description("The current moon phase of a world.")
@Examples({
	"if moon phase of player's world is full moon:",
		"\tsend \"Watch for the wolves!\""
})
@Since("2.7")
@RequiredPlugins("Paper 1.16+")
public class ExprMoonPhase extends SimplePropertyExpression<World, MoonPhase> {

	static {
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
