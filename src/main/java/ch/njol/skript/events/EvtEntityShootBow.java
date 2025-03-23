package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventConverter;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.slot.EquipmentSlot;
import ch.njol.skript.util.slot.Slot;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EvtEntityShootBow extends SkriptEvent {

	static {
		Skript.registerEvent("Entity Shoot Bow", EvtEntityShootBow.class, EntityShootBowEvent.class,
				"%entitydatas% shoot[ing] (bow|projectile)")
			.description("""
				Called when an entity shoots a bow.
				event-entity refers to the shot projectile/entity.
				""")
			.examples("""
				on player shoot bow:
					chance of 30%:
						damage event-slot by 10
						send "Your bow has taken increased damage!" to shooter
				
				on stray shooting bow:
					set {_e} to event-entity
					spawn a cow at {_e}:
						set velocity of entity to velocity of {_e}
					set event-entity to last spawned entity
				""")
			.since("INSERT VERSION");

		EventValues.registerEventValue(EntityShootBowEvent.class, ItemStack.class, EntityShootBowEvent::getBow);

		EventValues.registerEventValue(EntityShootBowEvent.class, Entity.class, new EventConverter<>() {
			@Override
			public void set(EntityShootBowEvent event, @Nullable Entity entity) {
				if (entity == null)
					return;
				event.setProjectile(entity);
			}

			@Override
			public @NotNull Entity convert(EntityShootBowEvent from) {
				return from.getProjectile();
			}
		});

		EventValues.registerEventValue(EntityShootBowEvent.class, Slot.class, event -> {
			EntityEquipment equipment = event.getEntity().getEquipment();
			if (equipment == null)
				return null;
			return new EquipmentSlot(equipment, event.getHand());
		});

	}

	private Literal<EntityData<?>> entityDatas;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		//noinspection unchecked
		entityDatas = (Literal<EntityData<?>>) args[0];
		if (entityDatas instanceof LiteralList<EntityData<?>> list && list.getAnd())
			list.invertAnd();
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof EntityShootBowEvent shootBowEvent))
			return false;
		LivingEntity eventEntity = shootBowEvent.getEntity();
		return entityDatas.check(event, entityData -> entityData.isInstance(eventEntity));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return this.entityDatas.toString(event, debug) + " shoot bow";
	}

}
