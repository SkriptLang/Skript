package org.skriptlang.skript.bukkit.entity.general.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Entity Snapshot")
@Description("""
	Returns the entity snapshot of a provided entity, which includes all the data associated with it \
	(name, health, attributes, etc.) at the time this expression is used.
	Individual attributes of a snapshot cannot be modified or retrieved.
	""")
@Example("""
	spawn a pig at location(0, 0, 0):
		set the max health of entity to 20
		set the health of entity to 20
		set {_snapshot} to the entity snapshot of entity
		clear entity
	spawn {_snapshot} at location(0, 0, 0)
	""")
@RequiredPlugins("Minecraft 1.20.2+")
@Since("2.10")
public class ExprEntitySnapshot extends SimplePropertyExpression<Object, EntitySnapshot> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprEntitySnapshot.class, EntitySnapshot.class, "entity snapshot", "entities/entitydatas", false)
				.supplier(ExprEntitySnapshot::new)
				.build()
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (Player.class.isAssignableFrom(exprs[0].getReturnType()) || FishHook.class.isAssignableFrom(exprs[0].getReturnType())) {
			Skript.error("One or more listed entities can not return an entity snapshot.");
			return false;
		}
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public @Nullable EntitySnapshot convert(Object object) {
		if (object instanceof Entity entity) {
			return entity.createSnapshot();
		} else if (object instanceof EntityData<?> entityData) {
			Entity entity = entityData.create();
			EntitySnapshot snapshot = entity.createSnapshot();
			entity.remove();
			return snapshot;
		}
		return null;
	}

	@Override
	protected String getPropertyName() {
		return "entity snapshot";
	}

	@Override
	public Class<EntitySnapshot> getReturnType() {
		return EntitySnapshot.class;
	}

}
