package org.skriptlang.skript.bukkit.spawners.elements.expressions.spawnerentry;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawners.util.SkriptSpawnerEntry;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Spawner Entry Entity")
@Description("""
	Returns the entity snapshot of the spawner entry. The snapshot defines what entity the spawner entry represents.
	""")
@Example("""
	set {_entry} to a spawner entry of a pig
	broadcast "The entry is a %spawner entry snapshot of {_entry}%" # broadcasts "The entry is a pig"
	""")
@Since("INSERT VERSION")
public class ExprSpawnerEntrySnapshot extends SimplePropertyExpression<SkriptSpawnerEntry, EntitySnapshot> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprSpawnerEntrySnapshot.class, EntitySnapshot.class,
			"spawner entry [entity] snapshot[s]", "spawnerentries", true)
			.supplier(ExprSpawnerEntrySnapshot::new)
			.build()
		);
	}

	@Override
	public @NotNull EntitySnapshot convert(SkriptSpawnerEntry entry) {
		return entry.getEntitySnapshot();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(EntitySnapshot.class, EntityData.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		Object object = delta[0];

		EntitySnapshot snapshot = null;
		if (object instanceof EntitySnapshot entitySnapshot) {
			snapshot = entitySnapshot;
		} else if (object instanceof EntityData<?> entityData) {
			Entity entity = entityData.create();
			if (entity == null)
				return;

			//noinspection UnstableApiUsage
			snapshot = entity.createSnapshot();
			if (snapshot == null)
				return;
			entity.remove();
		}

		assert snapshot != null;

		for (SkriptSpawnerEntry entry : getExpr().getArray(event)) {
			entry.setEntitySnapshot(snapshot);
		}
	}

	@Override
	public Class<? extends EntitySnapshot> getReturnType() {
		return EntitySnapshot.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawner entry snapshot";
	}

}
