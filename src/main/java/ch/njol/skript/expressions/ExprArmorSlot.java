package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.slot.EquipmentSlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Name("Armor Slot")
@Description({
	"Equipment of living entities, i.e. the boots, leggings, chestplate or helmet.",
	"Body armor is a special slot that can only be used for:",
	"<ul>",
	"<li>Horses: Horse armour (doesn't work on zombie or skeleton horses)</li>",
	"<li>Wolves: Wolf Armor</li>",
	"<li>Llamas (regular or trader): Carpet</li>",
	"<li>Happy Ghasts: Harness</li>",
	"</ul>",
	"Saddle is a special slot that can only be used for: pigs, striders and horse types (horse, camel, llama, mule, donkey)."
})
@Example("set chestplate of the player to a diamond chestplate")
@Example("helmet of player is neither tag values of tag \"paper:helmets\" nor air # player is wearing a block, e.g. from another plugin")
@Keywords("armor")
@Since({
	"1.0, 2.8.0 (armor), 2.10 (body armor), 2.12 (saddle)",
	"2.12.1 (happy ghast)"
})
public class ExprArmorSlot extends PropertyExpression<LivingEntity, Slot> {

	private static final Set<Class<? extends Entity>> BODY_ENTITIES =
		new HashSet<>(Set.of(Horse.class, Llama.class, TraderLlama.class));
	private static final Set<Class<? extends Entity>> SADDLE_ENTITIES = Set.of(Pig.class, Strider.class, AbstractHorse.class);

	private static final boolean HAS_BODY_SLOT =
		Skript.fieldExists(org.bukkit.inventory.EquipmentSlot.class, "BODY");
	private static final boolean HAS_SADDLE_SLOT =
		Skript.fieldExists(org.bukkit.inventory.EquipmentSlot.class, "SADDLE");

	private static final org.bukkit.inventory.EquipmentSlot BODY_SLOT =
		HAS_BODY_SLOT ? org.bukkit.inventory.EquipmentSlot.valueOf("BODY") : null;
	private static final org.bukkit.inventory.EquipmentSlot SADDLE_SLOT =
		HAS_SADDLE_SLOT ? org.bukkit.inventory.EquipmentSlot.valueOf("SADDLE") : null;

	static {
		if (Material.getMaterial("WOLF_ARMOR") != null)
			BODY_ENTITIES.add(Wolf.class);
		if (Skript.classExists("org.bukkit.entity.HappyGhast"))
			BODY_ENTITIES.add(HappyGhast.class);

		register(ExprArmorSlot.class, Slot.class, "(%-*equipmentslots%|[the] armo[u]r[s]) [item:item[s]]", "livingentities");
	}

	private @Nullable Literal<org.bukkit.inventory.EquipmentSlot> slots;
	private boolean explicitSlot;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		Expression<?> slots = exprs[0];
		Expression<?> expr = exprs[1];
		if (matchedPattern == 1) {
			slots = exprs[1];
			expr = exprs[0];
		}
		if (slots != null)
			//noinspection unchecked
			this.slots = (Literal<org.bukkit.inventory.EquipmentSlot>) slots;
		explicitSlot = !parseResult.hasTag("item");
		//noinspection unchecked
		setExpr((Expression<? extends LivingEntity>) expr);
		return true;
	}

	@Override
	protected Slot @Nullable [] get(Event event, LivingEntity[] source) {
		if (slots == null) {
			return Arrays.stream(source)
				.map(LivingEntity::getEquipment)
				.flatMap(equipment -> {
					if (equipment == null)
						return null;
					// Entities with 'body armor' don't and shouldn't have other equipment slots (for now)
					if (HAS_BODY_SLOT && canUseEquipmentSlot(equipment.getHolder(), org.bukkit.inventory.EquipmentSlot.BODY))
						return Stream.of(new EquipmentSlot(equipment, org.bukkit.inventory.EquipmentSlot.BODY, explicitSlot));
					return Stream.of(
						new EquipmentSlot(equipment, org.bukkit.inventory.EquipmentSlot.HEAD, explicitSlot),
						new EquipmentSlot(equipment, org.bukkit.inventory.EquipmentSlot.CHEST, explicitSlot),
						new EquipmentSlot(equipment, org.bukkit.inventory.EquipmentSlot.LEGS, explicitSlot),
						new EquipmentSlot(equipment, org.bukkit.inventory.EquipmentSlot.FEET, explicitSlot)
					);
				}).toArray(Slot[]::new);
		}
		org.bukkit.inventory.EquipmentSlot[] equipmentSlots = this.slots.getArray(event);
		if (equipmentSlots.length == 0)
			return new EquipmentSlot[0];
		List<EquipmentSlot> slots = new ArrayList<>();
		for (LivingEntity entity : source) {
			EntityEquipment equipment = entity.getEquipment();
			if (equipment == null)
				continue;
			for (org.bukkit.inventory.EquipmentSlot equipmentSlot : equipmentSlots) {
				if (!canUseEquipmentSlot(entity, equipmentSlot))
					continue;
				slots.add(new EquipmentSlot(equipment, equipmentSlot, explicitSlot));
			}
		}
		return slots.toArray(EquipmentSlot[]::new);
	}

	private boolean canUseEquipmentSlot(Entity entity, org.bukkit.inventory.EquipmentSlot equipmentSlot) {
		Class<? extends Entity> entityClass = entity.getType().getEntityClass();
		Set<Class<? extends Entity>> entityClasses = null;
		if (HAS_BODY_SLOT && equipmentSlot == BODY_SLOT) {
			entityClasses = BODY_ENTITIES;
		} else if (HAS_SADDLE_SLOT && equipmentSlot == SADDLE_SLOT) {
			entityClasses = SADDLE_ENTITIES;
		}
		if (entityClasses != null) {
			if (entityClasses.contains(entityClass))
				return true;
			for (Class<? extends Entity> type : entityClasses) {
				if (type.isInstance(entity))
					return true;
			}
			return false;
		}
		return true;
	}

	@Override
	public boolean isSingle() {
		return slots != null && slots.isSingle();
	}

	@Override
	public Class<Slot> getReturnType() {
		return Slot.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("the");
		if (slots != null) {
			builder.append(slots);
		} else {
			builder.append("armor");
		}
		if (!explicitSlot)
			builder.append("items");
		builder.append("of", getExpr());
		return builder.toString();
	}

}
