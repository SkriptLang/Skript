package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
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
			"%entitydatas% is spawnable [in [the [world]] %-world%]",
			"%entitydatas% can be spawned [in [the [world]] %-world%]",
			"%entitydatas% (isn't|is not) spawnable [in [the [world]] %-world%]",
			"%entitydatas% (can't|can not) be spawned [in [the [world]] %-world%]");
	}

	private Expression<EntityData<?>> datas;
	private @Nullable Expression<World> world = null;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		datas = (Expression<EntityData<?>>) exprs[0];
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
		return datas.check(event, entityData -> entityData.canSpawn(finalWorld), isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append(datas, "is");
		if (isNegated())
			builder.append("not");
		builder.append("spawnable");
		if (world != null)
			builder.append("in", world);
		return builder.toString();
	}

}
