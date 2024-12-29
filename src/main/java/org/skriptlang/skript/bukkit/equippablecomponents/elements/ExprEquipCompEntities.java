package org.skriptlang.skript.bukkit.equippablecomponents.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("rawtypes")
@Name("Equippable Component - Allowed Entities")
@Description("The entities allowed to wear the item.")
@Examples({
	"set the allowed entities of {_item} to zombie and skeleton",
	"",
	"set {_component} to the equippable component of {_item}",
	"clear the allowed entities of {_component}",
	"set the equippable component of {_item} to {_component}"
})
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class ExprEquipCompEntities extends PropertyExpression<Object, EntityData> {

	static {
		Skript.registerExpression(ExprEquipCompEntities.class, EntityData.class, ExpressionType.PROPERTY,
			"[the] allowed entities of %itemstacks/itemtypes/slots/equippablecomponents%"
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}

	@Override
	protected EntityData @Nullable [] get(Event event, Object[] source) {
		List<EntityData> types = new ArrayList<>();
		for (Object object : getExpr().getArray(event)) {
			EquippableComponent component = null;
			if (object instanceof EquippableComponent equippableComponent) {
				component = equippableComponent;
			} else {
				ItemStack itemStack = ItemUtils.asItemStack(object);
				if (itemStack != null)
					component = itemStack.getItemMeta().getEquippable();
			}
			if (component != null) {
				component.getAllowedEntities().forEach(entityType -> {
					Class<? extends Entity> clazz = entityType.getEntityClass();
					types.add(EntityData.fromClass(clazz));
				});
			}
		}
		return types.toArray(new EntityData[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, REMOVE, ADD -> CollectionUtils.array(EntityData[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {

		EntityData[] types = (EntityData[]) delta;
		List<EntityType> converted = new ArrayList<>();
		if (types != null && types.length > 0) {
			Arrays.stream(types).forEach(entityData -> {
				converted.add(EntityUtils.toBukkitEntityType(entityData));
			});
		}

		Consumer<EquippableComponent> changer = switch (mode) {
			case SET -> component -> {
				component.setAllowedEntities(converted);
			};
			case ADD -> component -> {
				List<EntityType> current = component.getAllowedEntities().stream().toList();
				current.addAll(converted);
				component.setAllowedEntities(current);
			};
			case REMOVE -> component -> {
				List<EntityType> current = component.getAllowedEntities().stream().toList();
				current.removeAll(converted);
				component.setAllowedEntities(current);
			};
			case DELETE -> component -> {
				component.setAllowedEntities(new ArrayList<>());
			};
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		};

		for (Object object : getExpr().getArray(event)) {
			if (object instanceof EquippableComponent component) {
				changer.accept(component);
			} else {
				ItemStack itemStack = ItemUtils.asItemStack(object);
				if (itemStack == null)
					continue;
				ItemMeta meta = itemStack.getItemMeta();
				EquippableComponent component = meta.getEquippable();
				changer.accept(component);
				meta.setEquippable(component);
				itemStack.setItemMeta(meta);
				if (object instanceof Slot slot) {
					slot.setItem(itemStack);
				} else if (object instanceof ItemType itemType) {
					itemType.setItemMeta(meta);
				} else if (object instanceof ItemStack itemStack1) {
					itemStack1.setItemMeta(meta);
				}
			}
		}
	}

	@Override
	public Class<EntityData> getReturnType() {
		return EntityData.class;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the allowed entities of " + getExpr().toString(event, debug);
	}
}
