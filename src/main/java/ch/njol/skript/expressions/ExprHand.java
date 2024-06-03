package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import java.lang.reflect.Method;
import java.util.WeakHashMap;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import ch.njol.skript.util.slot.EquipmentSlot;
import org.eclipse.jdt.annotation.Nullable;

@Name("Hand")
@Description({"Returns which hand player uses in some InteractEvents"})
@Examples({
	"on click:",
	"    send \"%event-hand%\" to event-player",
	"    if event-hand is off hand",
    "        send \"It is off hand\" to event-player"
})
@Since("2.8.6")
public class ExprHand extends SimpleExpression<Slot> {
	private static final WeakHashMap<Class<? extends Event>, Method> GET_HAND_METHODS = new WeakHashMap<>();

	static {
		Skript.registerExpression(ExprHand.class, Slot.class, ExpressionType.SIMPLE, "[event( |-)]hand");
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (e == null)
			return "the hand";
		return Classes.getDebugMessage(getSingle(e));
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends EquipmentSlot> getReturnType() {
		return EquipmentSlot.class;
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (getParser().getCurrentEvents() == null) {
			Skript.error("There is no 'event-hand' without a event");
			return false;
		}
		for (Class<? extends Event> currentEvent : getParser().getCurrentEvents()) {
			if (Skript.methodExists(currentEvent, "getHand")) {
				try {
					GET_HAND_METHODS.put(currentEvent, currentEvent.getDeclaredMethod("getHand"));  //Cache it when init
				} catch (NoSuchMethodException ignored) {
				}
				return true;
			}
		}
		Skript.error("There is no 'event-hand' in this event: " + getParser().getCurrentEventName());
		return false;
	}

	@Override
	protected @Nullable Slot[] get(Event event) {
		if (!GET_HAND_METHODS.containsKey(event.getClass())) {
			try {
				GET_HAND_METHODS.put(event.getClass(), event.getClass().getDeclaredMethod("getHand"));
			} catch (NoSuchMethodException ignored) {
				return null;  //If it is not cached, and cannot get the method, stop trigger
			}
		}
		LivingEntity entity = null;
		if (event instanceof EntityEvent) {
			if (((EntityEvent) event).getEntity() instanceof LivingEntity) {
				entity = (LivingEntity) ((EntityEvent) event).getEntity();
			}
		} else if (event instanceof PlayerEvent) {
			entity = ((PlayerEvent) event).getPlayer();
		}
		if (entity == null || entity.getEquipment() == null) return null;
		try {
			org.bukkit.inventory.EquipmentSlot bkEquip = (org.bukkit.inventory.EquipmentSlot) GET_HAND_METHODS.get(event.getClass()).invoke(event);
			return new EquipmentSlot[] {new EquipmentSlot(entity.getEquipment(), bkEquip == org.bukkit.inventory.EquipmentSlot.HAND ? EquipmentSlot.EquipSlot.TOOL : EquipmentSlot.EquipSlot.OFF_HAND)};
		} catch (ReflectiveOperationException ignored) {
			return null;
		}
	}
}
