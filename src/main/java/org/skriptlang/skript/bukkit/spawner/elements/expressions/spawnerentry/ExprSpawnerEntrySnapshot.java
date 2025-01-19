package org.skriptlang.skript.bukkit.spawner.elements.expressions.spawnerentry;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.spawner.SpawnerModule;

@Name("Spawner Entry - Entity Snapshot")
@Description("The entity snapshot of the spawner entry. Entity snapshots determines what entity the spawner will spawn.")
@Examples({
	"set {_entry} to a spawner entry using entity snapshot of a pig",
	"add {_entry} to potential spawns of target block",
	"# the spawner will now spawn pigs"
})
@Since("INSERT VERSION")
@RequiredPlugins("MC 1.21+")
public class ExprSpawnerEntrySnapshot extends SimplePropertyExpression<SpawnerEntry, EntitySnapshot> {

	static {
		registerDefault(SpawnerModule.SYNTAX_REGISTRY, ExprSpawnerEntrySnapshot.class, EntitySnapshot.class,
				"entity snapshot", "spawnerentries");
	}

	@Override
	public @NotNull EntitySnapshot convert(SpawnerEntry entry) {
		return entry.getSnapshot();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(EntitySnapshot.class);
		return null;
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
