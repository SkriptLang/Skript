package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.EquipmentSlot;
import ch.njol.skript.util.slot.EquipmentSlot.EquipSlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

@Name("Armor Slot")
@Description({
	"Equipment of living entities, i.e. the boots, leggings, chestplate or helmet.",
	"Body armor is a special slot that can only be used for:",
	"<ul>",
	"<li>Horses: Horse armour (doesn't work on zombie or skeleton horses)</li>",
	"<li>Wolves: Wolf Armor</li>",
	"<li>Llamas (regular or trader): Carpet</li>",
	"</ul>"
})
@Examples({
	"set chestplate of the player to a diamond chestplate",
	"helmet of player is neither a helmet nor air # player is wearing a block, e.g. from another plugin"
})
@Keywords("armor")
@Since("1.0, 2.8.0 (armor), INSERT VERSION (body armor)")
public class ExprArmorSlot extends PropertyExpression<LivingEntity, Slot> {

	private static final Set<Class<?>> bodyEntities = new HashSet<>(Arrays.asList(Horse.class, Llama.class, TraderLlama.class));

	static {
		if (Material.getMaterial("WOLF_ARMOR") != null)
			bodyEntities.add(Wolf.class);

		register(ExprArmorSlot.class, Slot.class, "((boots:(boots|shoes)|leggings:leg[ging]s|chestplate:chestplate[s]|helmet:helmet[s]) [(item|:slot)]|armour:armo[u]r[s]|bodyarmor:body armo[u]r)", "livingentities");
	}

	private @Nullable EquipSlot slot;
	private boolean explicitSlot;
	private boolean isArmor;
	private boolean isBody;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		isBody = parseResult.hasTag("bodyarmor");
		isArmor = parseResult.hasTag("armour");
		slot = (isArmor || isBody) ? null : EquipSlot.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));
		explicitSlot = parseResult.hasTag("slot"); // User explicitly asked for SLOT, not item
		setExpr((Expression<? extends LivingEntity>) exprs[0]);
		return true;
	}

	@Override
	protected Slot[] get(Event event, LivingEntity[] source) {
		if (slot == null) { // All Armour
			return Arrays.stream(source)
					.map(LivingEntity::getEquipment)
					.flatMap(equipment -> {
						if (equipment == null)
							return null;
						if (isBody) {
							if (!bodyEntities.contains(equipment.getHolder().getType().getEntityClass()))
								return null;
							return Stream.of(new EquipmentSlot(equipment, EquipSlot.BODY, explicitSlot));
						}
						return Stream.of(
								new EquipmentSlot(equipment, EquipSlot.HELMET, explicitSlot),
								new EquipmentSlot(equipment, EquipSlot.CHESTPLATE, explicitSlot),
								new EquipmentSlot(equipment, EquipSlot.LEGGINGS, explicitSlot),
								new EquipmentSlot(equipment, EquipSlot.BOOTS, explicitSlot)
						);
					})
					.toArray(Slot[]::new);
		}

		return get(source, entity -> {
			EntityEquipment equipment = entity.getEquipment();
			if (equipment == null)
				return null;
			return new EquipmentSlot(equipment, slot, explicitSlot);
		});
	}

	@Override
	public boolean isSingle() {
		return isBody || (!isArmor && super.isSingle());
	}

	@Override
	public Class<Slot> getReturnType() {
		return Slot.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return slot == null ? "armour" : slot.name().toLowerCase(Locale.ENGLISH) + " of " + getExpr().toString(event, debug);
	}

}
