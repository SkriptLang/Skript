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
import org.jetbrains.annotations.Nullable;

@Name("Can See Entity")
@Description("Checks whether the given players can see the provided entities.")
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
					"entit(y|ies) %entities% (is|are) [(1¦in)]visible for %players%",
					"%players% can see entit(y|ies) %entities%",
					"entit(y|ies) %entities% (is|are)(n't| not) [(1¦in)]visible for %players%",
					"%players% can('t| not) see entit(y|ies) %entities%");
	}

	@SuppressWarnings("null")
	private Expression<Player> players;
	@SuppressWarnings("null")
	private Expression<Entity> entities;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern,
						Kleenean isDelayed, ParseResult result) {
		if (matchedPattern == 1 || matchedPattern == 3) {
			players = (Expression<Player>) exprs[0];
			entities = (Expression<Entity>) exprs[1];
		} else {
			entities = (Expression<Entity>) exprs[0];
			players = (Expression<Player>) exprs[1];
		}
		setNegated(matchedPattern > 1 ^ result.mark == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return players.check(event,
				player -> entities.check(event,
						player::canSee
				), isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.CAN, event, debug, players,
				"see entities" + entities.toString(event, debug));
	}
}
