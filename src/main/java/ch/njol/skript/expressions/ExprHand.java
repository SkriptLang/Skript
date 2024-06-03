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
import ch.njol.util.Kleenean;
import java.lang.reflect.Method;
import java.util.WeakHashMap;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.eclipse.jdt.annotation.Nullable;

@Name("Hand")
@Description({"Returns which hand player uses in InteractEvent"})
@Examples({
	"on click:",
	"    send \"%event-hand%\" to event-player",
	"    if event-hand is off hand",
    "        send \"It is off hand\" to event-player"
})
@Since("2.8.6")
public class ExprHand extends SimpleExpression<EquipmentSlot> {
	private static final WeakHashMap<Class<? extends Event>, Method> GET_HAND_METHODS = new WeakHashMap<>();
	private int pattern;

	static {
		Skript.registerExpression(ExprHand.class, EquipmentSlot.class, ExpressionType.SIMPLE, "1¦([event( |-)]hand)|2¦main hand|3¦off[ ]hand|4¦helmet|5¦chestplate|6¦leggings|7¦boots");
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
		pattern = parseResult.mark;
		if (pattern == 1) {
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
		return true;
	}

	@Override
	protected @Nullable EquipmentSlot[] get(Event event) {
		switch (pattern) {
			case 2:
				return new EquipmentSlot[] {EquipmentSlot.HAND};
			case 3:
				return new EquipmentSlot[] {EquipmentSlot.OFF_HAND};
			case 4:
				return new EquipmentSlot[] {EquipmentSlot.HEAD};
			case 5:
				return new EquipmentSlot[] {EquipmentSlot.CHEST};
			case 6:
				return new EquipmentSlot[] {EquipmentSlot.LEGS};
			case 7:
				return new EquipmentSlot[] {EquipmentSlot.FEET};
		}
		if (!GET_HAND_METHODS.containsKey(event.getClass())) {
			try {
				GET_HAND_METHODS.put(event.getClass(), event.getClass().getDeclaredMethod("getHand"));
			} catch (NoSuchMethodException ignored) {
				return null;  //If it is not cached, and cannot get the method, stop trigger
			}
		}
		try {
			return new EquipmentSlot[] {(EquipmentSlot) GET_HAND_METHODS.get(event.getClass()).invoke(event)};
		} catch (ReflectiveOperationException ignored) {
			return null;
		}
	}
}
