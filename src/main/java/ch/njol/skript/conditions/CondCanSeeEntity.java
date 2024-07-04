package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.*;
import ch.njol.skript.effects.EffEntityVisibility;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Can See Entity")
@Description("Checks whether the given players can see other entities.")
@Examples({
	"if the player can't see the last spawned entity:",
		"\tmessage \"who dat?\""
})
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.19+")
public class CondCanSeeEntity extends Condition {

	static {
		if (EffEntityVisibility.SUPPORTS_ENTITY_VISIBILITY)
			Skript.registerCondition(CondCanSeeEntity.class,
					"%entities% (is|are) [(1¦in)]visible for %players%",
					"%players% can see %entities%",
					"%entities% (is|are)(n't| not) [(1¦in)]visible for %players%",
					"%players% can('t| not) see %entities%");
	}

	@SuppressWarnings("null")
	private Expression<Player> players;
	@SuppressWarnings("null")
	private Expression<Entity> entities;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 1 || matchedPattern == 3) {
			players = (Expression<Player>) exprs[0];
			entities = (Expression<Entity>) exprs[1];
		} else {
			entities = (Expression<Entity>) exprs[0];
			players = (Expression<Player>) exprs[1];
		}
		setNegated(matchedPattern > 1 ^ parseResult.mark == 1);
		return true;
	}

	@Override
	public boolean check(Event e) {
		return players.check(e,
				player -> entities.check(e,
						player::canSee
				), isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.CAN, event, debug, players,
				"see" + entities.toString(event, debug));
	}
}
