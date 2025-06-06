package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
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
import java.util.Collection;
import java.util.List;

@SuppressWarnings("rawtypes")
@Name("Equippable Component - Allowed Entities")
@Description("The entities allowed to wear the item. "
	+ "NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work aas intended.")
@Example("set the allowed entities of {_item} to a zombie and a skeleton")
@Example("""
	set {_component} to the equippable component of {_item}
	clear the allowed entities of {_component}
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class ExprEquipCompEntities extends SimplePropertyExpression<EquippableWrapper, EntityData[]> implements EquippableExperiment {

	static {
		registerDefault(ExprEquipCompEntities.class, EntityData[].class, "allowed entities", "equippablecomponents");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<EquippableWrapper>) exprs[0]);
		return true;
	}

	@Override
	public EntityData @Nullable [] convert(EquippableWrapper wrapper) {
		EquippableComponent component = wrapper.getComponent();
		Collection<EntityType> allowed = component.getAllowedEntities();
		if (allowed == null || allowed.isEmpty())
			return null;
		return allowed.stream()
			.map(entityType -> {
				Class<? extends Entity> entityClass = entityType.getEntityClass();
				return (EntityData) EntityData.fromClass(entityClass);
			})
			.toArray(EntityData[]::new);
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
		if (delta != null) {
			for (Object object : delta) {
				if (object instanceof EntityData<?> entityData)
					converted.add(EntityUtils.toBukkitEntityType(entityData));
			}
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
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<EntityData[]> getReturnType() {
		return EntityData[].class;
	}

	@Override
	protected String getPropertyName() {
		return "allowed entities";
	}

}
