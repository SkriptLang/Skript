package ch.njol.skript.conditions;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Is Wearing")
@Description("Checks whether a player is wearing some armour.")
@Examples({"player is wearing an iron chestplate and iron leggings",
		"player is wearing all diamond armour"})
@Since("1.0")
public class CondIsWearing extends Condition {
	
	static {
		PropertyCondition.register(CondIsWearing.class, "wearing %itemtypes%", "livingentities");
	}
	
	@SuppressWarnings("null")
	private Expression<LivingEntity> entities;
	@SuppressWarnings("null")
	private Expression<ItemType> types;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		entities = (Expression<LivingEntity>) vars[0];
		types = (Expression<ItemType>) vars[1];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return entities.check(e,
				en -> types.check(e,
						t -> {
							EntityEquipment equip = en.getEquipment();
							if (equip == null)
								return false; // No equipment -> not wearing anything
							for (final ItemStack is : equip.getArmorContents()) {
								if (t.isOfType(is) ^ t.isAll())
									return !t.isAll();
							}
							return t.isAll();
						}),
				isNegated());
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return PropertyCondition.toString(this, PropertyType.BE, e, debug, entities,
				"wearing " + types.toString(e, debug));
	}
	
}
