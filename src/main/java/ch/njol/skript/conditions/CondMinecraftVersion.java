package ch.njol.skript.conditions;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Version;
import ch.njol.util.Kleenean;

@Name("Running Minecraft")
@Description("Checks if current Minecraft version is given version or newer.")
@Examples("running minecraft \"1.14\"")
@Since("2.5")
public class CondMinecraftVersion extends Condition {
	
	static {
		Skript.registerCondition(CondMinecraftVersion.class, "running [(1¦below)] minecraft %string%");
	}

	@SuppressWarnings("null")
	private Expression<String> version;
	
	@SuppressWarnings({"null", "unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		version = (Expression<String>) exprs[0];
		setNegated(parseResult.mark == 1);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		String ver = version.getSingle(e);
		return ver != null ? Skript.isRunningMinecraft(new Version(ver)) ^ isNegated() : false;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "is running minecraft " + version.toString(e, debug);
	}
	
}
