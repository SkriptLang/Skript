package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Is Spawnable")
@Description("""
	Whether an entity type can be spawned in a world. If no world is provided, will default to the first world.
	Any general types such as 'monster, mob, entity, living entity' etc. will never be spawnable.
	""")
@Example("""
	if a pig is spawnable: # true
	if a monster can be spawned: # false
	""")
@Since("INSERT VERSION")
public class CondIsSpawnable extends Condition {

	static {
		Skript.registerCondition(CondIsSpawnable.class, ConditionType.COMBINED,
			"%entitytypes% is spawnable [in [the [world]] %-world%]",
			"%entitytypes% can be spawned [in [the [world]] %-world%]",
			"%entitytypes% (isn't|is not) spawnable [in [the [world]] %-world%]",
			"%entitytypes% (can't|can not) be spawned [in [the [world]] %-world%]");
	}

	private Expression<?> types;
	private @Nullable Expression<World> world = null;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		types = exprs[0];
		if (exprs[1] != null) {
			//noinspection unchecked
			world = (Expression<World>) exprs[1];
		}
		setNegated(matchedPattern >= 2);
		return true;
	}

	@Override
	public boolean check(Event event) {
		World world = null;
		if (this.world != null) {
			world = this.world.getSingle(event);
		}
		if (world == null)
			world = Bukkit.getWorlds().get(0);

		World finalWorld = world;
		return SimpleExpression.check(types.getArray(event), object -> {
			if (object instanceof EntityData<?> entityData) {
				return entityData.canSpawn(finalWorld);
			} else if (object instanceof EntityType entityType) {
				EntityData<?> entityData = entityType.data;
				if (entityData != null)
					return entityData.canSpawn(finalWorld);
			}
			return false;
		}, isNegated(), types.getAnd());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append(types, "is");
		if (isNegated())
			builder.append("not");
		builder.append("spawnable");
		if (world != null)
			builder.append("in", world);
		return builder.toString();
	}

}
