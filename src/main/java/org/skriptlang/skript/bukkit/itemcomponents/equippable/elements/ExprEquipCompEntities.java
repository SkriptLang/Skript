package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperiment;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("rawtypes")
@Name("Equippable Component - Allowed Entities")
@Description("The entities allowed to wear the item. "
	+ "Note that equippable component elements are experimental making them subject to change and may not work as intended.")
@Example("set the allowed entities of {_item} to a zombie and a skeleton")
@Example("""
	set {_component} to the equippable component of {_item}
	clear the allowed entities of {_component}
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class ExprEquipCompEntities extends PropertyExpression<EquippableWrapper, EntityData> implements EquippableExperiment {

	static {
		register(ExprEquipCompEntities.class, EntityData.class, "allowed entities", "equippablecomponents");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<EquippableWrapper>) exprs[0]);
		return true;
	}

	@Override
	protected EntityData @Nullable [] get(Event event, EquippableWrapper[] source) {
		List<EntityData> types = new ArrayList<>();
		for (EquippableWrapper wrapper : source) {
			EquippableComponent component = wrapper.getComponent();
			Collection<EntityType> allowed = component.getAllowedEntities();
			if (allowed == null || allowed.isEmpty())
				continue;
			allowed.forEach(entityType -> {
				Class<? extends Entity> entityClass = entityType.getEntityClass();
				types.add(EntityData.fromClass(entityClass));
			});
		}
		return types.toArray(EntityData[]::new);
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

		getExpr().stream(event).forEach(wrapper -> wrapper.editComponent(component -> {
			Collection<EntityType> allowed = component.getAllowedEntities();
			List<EntityType> current = allowed != null ? new ArrayList<>(allowed) : new ArrayList<>();
			switch (mode) {
				case SET -> component.setAllowedEntities(converted);
				case ADD -> {
					current.addAll(converted);
					component.setAllowedEntities(current);
				}
				case REMOVE -> {
					current.removeAll(converted);
					component.setAllowedEntities(current);
				}
				case DELETE -> component.setAllowedEntities(new ArrayList<>());
				default -> throw new IllegalStateException("Unexpected value: " + mode);
			}
		}));
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
