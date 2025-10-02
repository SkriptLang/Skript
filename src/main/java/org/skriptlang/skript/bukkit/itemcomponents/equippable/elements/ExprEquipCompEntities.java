package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Name("Equippable Component - Allowed Entities")
@Description("""
	The entities allowed to wear the item.
	NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("set the allowed entities of {_item} to a zombie and a skeleton")
@Example("""
	set {_component} to the equippable component of {_item}
	clear the allowed entities of {_component}
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("2.13")
@SuppressWarnings({"rawtypes", "UnstableApiUsage"})
public class ExprEquipCompEntities extends PropertyExpression<EquippableWrapper, EntityData> implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprEquipCompEntities.class, EntityData.class, "allowed entities", "equippablecomponents", true)
				.supplier(ExprEquipCompEntities::new)
				.build()
		);
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
			Collection<EntityType> allowed = wrapper.getAllowedEntities();
			allowed.forEach(entityType -> types.add(EntityUtils.toSkriptEntityData(entityType)));
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
		List<EntityType> converted = new ArrayList<>();
		if (delta != null) {
			for (Object object : delta) {
				converted.add(EntityUtils.toBukkitEntityType((EntityData<?>) object));
			}
		}

		getExpr().stream(event).forEach(wrapper -> {
			Collection<EntityType> allowed = wrapper.getAllowedEntities();
			List<EntityType> current = new ArrayList<>(allowed);
			switch (mode) {
				case SET -> {
					current.clear();
					current.addAll(converted);
				}
				case ADD -> current.addAll(converted);
				case REMOVE -> current.removeAll(converted);
				case DELETE -> current.clear();
			}
			wrapper.editBuilder(builder -> builder.allowedEntities(EquippableWrapper.convertAllowedEntities(current)));
		});
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<EntityData> getReturnType() {
		return EntityData.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the allowed entities of " + getExpr().toString(event, debug);
	}

}
