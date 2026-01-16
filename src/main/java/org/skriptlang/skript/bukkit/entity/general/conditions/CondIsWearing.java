package org.skriptlang.skript.bukkit.entity.general.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;

@Name("Is Wearing")
@Description("Checks whether an entity is wearing some items (usually armor).")
@Example("player is wearing an iron chestplate and iron leggings")
@Example("target is wearing wolf armor")
@Since("1.0")
public class CondIsWearing extends Condition {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			PropertyCondition.infoBuilder(
				CondIsWearing.class,
				PropertyType.BE,
				"wearing %itemtypes%",
				"livingentities"
			).supplier(CondIsWearing::new)
				.build()
		);
	}

	private Expression<LivingEntity> entities;
	private Expression<ItemType> types;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] vars, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) vars[0];
		types = (Expression<ItemType>) vars[1];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(Event event) {
		ItemType[] cachedTypes = types.getAll(event);

		return entities.check(event, entity -> {
			EntityEquipment equipment = entity.getEquipment();
			if (equipment == null)
				return false; // spigot nullability, no identifier as to why this occurs

			ItemStack[] contents = Arrays.stream(EquipmentSlot.values())
				.filter(entity::canUseEquipmentSlot)
				.map(equipment::getItem)
				.toArray(ItemStack[]::new);

			return SimpleExpression.check(cachedTypes, type -> {
				for (ItemStack content : contents) {
					if (type.isOfType(content) ^ type.isAll())
						return !type.isAll();
				}
				return type.isAll();
			}, false, false);
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.BE, event, debug, entities,
				"wearing " + types.toString(event, debug));
	}
	
}
