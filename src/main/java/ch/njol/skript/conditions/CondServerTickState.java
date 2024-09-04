package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.ServerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.ServerTickManager;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Locale;

@Name("Server Tick State")
@Description("Represents the ticking state of the server, for example, if the server is frozen, or running normally.")
@Examples({
	"if server's tick state is currently frozen:",
	"if the server is sprinting:"
})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class CondServerTickState extends Condition {

	static {
		if (ServerUtils.isServerTickManagerPresent())
			Skript.registerCondition(CondServerTickState.class,
				"[the] server's tick[ing] state is [currently] (:frozen|:stepping|:sprinting|:normal)",
				"[the] server's tick[ing] state (isn't|is not) [currently] (:frozen|:stepping|:sprinting|:normal)");
	}

	private ServerState state;

	public enum ServerState {
		FROZEN, STEPPING, SPRINTING, NORMAL
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		String tag = parseResult.tags.get(0).toUpperCase(Locale.ENGLISH);
		state = ServerState.valueOf(tag);
		setNegated(matchedPattern == 1);
		return true;
	}
	@Override
	public boolean check(Event event) {
		ServerTickManager serverTickManager = ServerUtils.getServerTickManager();
		boolean result = switch (state) {
			case FROZEN -> serverTickManager.isFrozen();
			case STEPPING -> serverTickManager.isStepping();
			case SPRINTING -> serverTickManager.isSprinting();
			case NORMAL -> serverTickManager.isRunningNormally();
		};
		return isNegated() != result;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String stateStr;
		if (isNegated()) {
			stateStr = "the server's tick state isn't ";
		} else {
			stateStr = "the server's tick state is ";
		}
		return stateStr + state.toString().toLowerCase(Locale.ENGLISH);
	}
}

