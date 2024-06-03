package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
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
@Since("2.8.9")
public class ExprHand extends SimpleExpression<EquipmentSlot> {
	private static final WeakHashMap<Class<? extends Event>, Method> GET_HAND_METHODS = new WeakHashMap<>();

	static {
		Skript.registerExpression(ExprHand.class, EquipmentSlot.class, ExpressionType.SIMPLE, "[event( |-)]hand");
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
	protected @Nullable EquipmentSlot[] get(Event event) {
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
