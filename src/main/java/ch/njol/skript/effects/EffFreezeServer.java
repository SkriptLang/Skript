package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.ServerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Freeze/Unfreeze Server")
@Description("Freezes or unfreezes the server.")
@Examples({
	"freeze server",
	"unfreeze server"
})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class EffFreezeServer extends Effect {

	static {
		if (ServerUtils.isServerTickManagerPresent())
			Skript.registerEffect(EffFreezeServer.class,
				"freeze [the] server",
				"unfreeze [the] server");
	}

	private boolean freeze;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		freeze = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		ServerUtils.getServerTickManager().setFrozen(freeze);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return freeze ? "freeze server" : "unfreeze server";
	}

}
