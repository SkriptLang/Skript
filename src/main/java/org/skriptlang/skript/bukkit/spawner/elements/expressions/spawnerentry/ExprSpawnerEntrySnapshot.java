package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnerentry;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExprSpawnerEntrySnapshot extends SimplePropertyExpression<SpawnerEntry, EntitySnapshot> {

	static {
		registerDefault(ExprSpawnerEntrySnapshot.class, EntitySnapshot.class, "spawner entry snapshot", "spawnerentries");
	}

	@Override
	public @NotNull EntitySnapshot convert(SpawnerEntry entry) {
		return entry.getSnapshot();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET -> CollectionUtils.array(EntitySnapshot.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		EntitySnapshot snapshot = (EntitySnapshot) delta[0];

		for (SpawnerEntry entry : getExpr().getArray(event)) {
			entry.setSnapshot(snapshot);
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
