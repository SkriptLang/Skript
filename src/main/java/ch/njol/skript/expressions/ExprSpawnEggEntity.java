package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.jetbrains.annotations.Nullable;

@Name("Spawn Egg Entity")
@Description({
	"Gets or sets the entity snapshot that the provided spawn eggs will spawn when used."
})
@Examples({
	"set {_item} to a zombie spawn egg",
	"broadcast the spawn egg entity of {_item}",
	"",
	"spawn a pig at location(0,0,0):",
		"\tset the max health of entity to 20",
		"\tset the health of entity to 20",
		"\tset {_snapshot} to the entity snapshot of entity",
		"\tclear entity",
	"set the spawn egg entity of {_item} to {_snapshot}",
	"if the spawn egg entity of {_item} is {_snapshot}: # Minecraft 1.20.5+",
	"",
	"set the spawn egg entity of {_item} to (random element out of all entities)",
	"",
	"set the spawn egg entity of {_item} to a zombie"
})
@RequiredPlugins("Minecraft 1.20.2+, Minecraft 1.20.5+ (comparisons)")
@Since("2.10")
public class ExprSpawnEggEntity extends SimplePropertyExpression<Object, EntitySnapshot> {

	static {
		if (Skript.classExists("org.bukkit.entity.EntitySnapshot"))
			register(ExprSpawnEggEntity.class, EntitySnapshot.class, "spawn egg entity", "itemstacks/itemtypes/slots");
	}

	@Override
	public @Nullable EntitySnapshot convert(Object object) {
		ItemStack itemStack = ItemUtils.asItemStack(object);
		if (itemStack == null || !(itemStack.getItemMeta() instanceof SpawnEggMeta eggMeta))
			return null;
		return eggMeta.getSpawnedEntity();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(EntitySnapshot.class, Entity.class, EntityData.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta == null)
			return;
		EntitySnapshot snapshot = null;
		if (delta[0] instanceof EntitySnapshot entitySnapshot) {
			snapshot = entitySnapshot;
		} else if (delta[0] instanceof Entity entity) {
			snapshot = entity.createSnapshot();
		} else if (delta[0] instanceof EntityData<?> entityData) {
			Entity entity = entityData.create();
			snapshot = entity.createSnapshot();
			entity.remove();
		}
		if (snapshot == null)
			return;

		for (Object object : getExpr().getArray(event)) {
			ItemStack item = ItemUtils.asItemStack(object);
			if (item == null || !(item.getItemMeta() instanceof SpawnEggMeta eggMeta))
				continue;
			eggMeta.setSpawnedEntity(snapshot);
			if (object instanceof Slot slot) {
				item.setItemMeta(eggMeta);
				slot.setItem(item);
			} else if (object instanceof ItemType itemType) {
				itemType.setItemMeta(eggMeta);
			} else if (object instanceof ItemStack itemStack) {
				itemStack.setItemMeta(eggMeta);
			}
		}
	}

	@Override
	public Class<EntitySnapshot> getReturnType() {
		return EntitySnapshot.class;
	}

	@Override
	protected String getPropertyName() {
		return "spawn egg entity";
	}

}
