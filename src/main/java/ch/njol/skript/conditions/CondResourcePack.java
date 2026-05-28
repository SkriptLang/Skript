package ch.njol.skript.conditions;

import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Resource Pack")
@Description("Checks state of the resource pack in a <a href='#resource_pack_request_action'>resource pack request response</a> event.")
@Example("""
	on resource pack response:
		if the resource pack wasn't accepted:
			kick the player due to "You have to install the resource pack to play in this server!"
	""")
@Since("2.4")
@Events("resource pack request response")
public class CondResourcePack extends Condition implements EventRestrictedSyntax {

	static {
		Skript.registerCondition(CondResourcePack.class,
				"[the] resource pack (was|is|has) [been] %resourcepackstate%",
				"[the] resource pack (was|is|has)(n't| not) [been] %resourcepackstate%");
	}

	@SuppressWarnings("null")
	private Expression<Status> states;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		states = (Expression<Status>) exprs[0];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(PlayerResourcePackStatusEvent.class);
	}
	
	@Override
	public boolean check(Event e) {
		if (!(e instanceof PlayerResourcePackStatusEvent))
			return isNegated();

		Status state = ((PlayerResourcePackStatusEvent) e).getStatus();
		return states.check(e, state::equals, isNegated());
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "resource pack was " + (isNegated() ? "not " : "") + states.toString(e, debug);
	}
	
}
