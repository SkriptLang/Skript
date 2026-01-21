package org.skriptlang.skript.bukkit.misc.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.server.ServerListPingEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Message of the Day")
@Description({
	"The message of the day in the server list.",
	"This can be changed in a <a href='#server_list_ping'>server list ping</a> event only.",
	"Use 'default MOTD' to obtain the default MOTD set through the server configuration. This cannot be changed."
})
@Example("""
	on server ling ping:
		set the motd to "<red>Join our server today!"
	""")
@Since("2.3")
@Keywords("MOTD")
@Events("server list ping")
public class ExprMOTD extends SimpleExpression<Component> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprMOTD.class, Component.class)
			.supplier(ExprMOTD::new)
			.priority(SyntaxInfo.SIMPLE)
			.addPattern("[the] [1:default|2:shown|2:displayed] (MOTD|message of [the] day)")
			.build());
	}

	private boolean isDefault;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		boolean isServerPingEvent = getParser().isCurrentEvent(PaperServerListPingEvent.class);
		if (parseResult.mark == 2 && !isServerPingEvent) {
			Skript.error("The 'shown' MOTD expression can't be used outside of a server list ping event");
			return false;
		}
		isDefault = (!isServerPingEvent && parseResult.mark == 0) || parseResult.mark == 1;
		return true;
	}

	@Override
	public Component[] get(Event event) {
		if (isDefault) {
			return new Component[]{Bukkit.motd()};
		}
		if (event instanceof ServerListPingEvent pingEvent) {
			return new Component[]{pingEvent.motd()};
		}
		return new Component[0];
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (isDefault) {
			return null;
		}
		if (getParser().getHasDelayBefore().isTrue()) {
			Skript.error("'" + toString(null, false) + "' cannot be changed after the event has passed");
			return null;
		}
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(Component.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (!(event instanceof ServerListPingEvent pingEvent)) {
			return;
		}
		pingEvent.motd(switch (mode) {
			case SET -> //noinspection DataFlowIssue - delta will not be null for SET
				(Component) delta[0];
			case DELETE -> Component.empty();
			case RESET -> Bukkit.motd();
			default -> throw new IllegalArgumentException();
		});
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Component> getReturnType() {
		return Component.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + (isDefault ? "default " : "") + "MOTD";
	}

}
