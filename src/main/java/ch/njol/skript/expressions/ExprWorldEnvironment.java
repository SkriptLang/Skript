package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.jetbrains.annotations.Nullable;

@Name("World Environment")
@Description("The environment of a world")
@Examples({
	"if environment of player's world is nether:",
	"\tapply fire resistance to player for 10 minutes"
})
@Since("2.7")
public class ExprWorldEnvironment extends SimplePropertyExpression<World, Environment> {

	static {
		register(ExprWorldEnvironment.class, Environment.class, "[world] environment", "worlds");
	}

	@Override
	@Nullable
	public Environment convert(World world) {
		return world.getEnvironment();
	}

	@Override
	public Class<? extends Environment> getReturnType() {
		return Environment.class;
	}

	@Override
	protected String getPropertyName() {
		return "environment";
	}

}
