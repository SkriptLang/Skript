package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Spawnable")
@Description("""
	Whether an entity type can be spawned in a world.
	Any general types such as 'monster, mob, entity, living entity' etc. will never be spawnable.
	""")
@Example("""
	if a pig is spawnable in world "world": # true
	if a monster can be spawned in {_world}: # false
	""")
@Since("2.13")
public class CondIsSpawnable extends Condition {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			PropertyCondition.infoBuilder(CondIsSpawnable.class, PropertyType.BE, "spawnable [in [the [world]] %world%]", "entitydatas")
				.addPatterns(PropertyCondition.getPatterns(PropertyType.CAN, "be spawned [in [the [world]] %world%]", "entitydatas"))
				.supplier(CondIsSpawnable::new)
				.build()
		);
	}

	private Expression<EntityData<?>> datas;
	private Expression<World> world = null;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		datas = (Expression<EntityData<?>>) exprs[0];
		//noinspection unchecked
		world = (Expression<World>) exprs[1];
		setNegated(matchedPattern % 2 != 0);
		return true;
	}

	@Override
	public boolean check(Event event) {
		World world = this.world.getSingle(event);
		if (world == null)
			return false;

		return datas.check(event, entityData -> entityData.canSpawn(world), isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return PropertyCondition.toString(
			this,
			PropertyType.BE,
			event,
			debug,
			datas,
			"spawnable in" + world.toString(event, debug)
		);
	}

}
