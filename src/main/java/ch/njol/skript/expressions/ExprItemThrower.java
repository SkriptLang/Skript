package ch.njol.skript.expressions;

import ch.njol.skript.bukkitutil.UUIDUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Name("Item Thrower")
@Description({
	"The entity that threw/dropped the dropped item.",
	"NOTES:",
	"Getting the item thrower will only return alive entity or a player that has played before. "
	    + "If the entity was killed, or the player has never played before, this will return null."
})
@Examples({
	"broadcast the item thrower of all dropped items",
	"set the dropped item thrower of {_dropped item} to player",
	"clear the item thrower of {_dropped item}"
})
@Since("INSERT VERSION")
public class ExprItemThrower extends SimplePropertyExpression<Item, Object> {

	static {
		registerDefault(ExprItemThrower.class, Object.class, "[dropped] item thrower", "itementities");
	}

	@Override
	public @Nullable Object convert(Item item) {
		UUID uuid = item.getThrower();
		if (uuid == null)
			return null;
		return UUIDUtils.fromUUID(uuid, true);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(OfflinePlayer.class, Entity.class, String.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		UUID newId = null;
		if (delta != null) {
			newId = UUIDUtils.asUUID(delta[0]);
		}

		for (Item item : getExpr().getArray(event)) {
			item.setThrower(newId);
		}
	}

	@Override
	public Class<Object> getReturnType() {
		return Object.class;
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return CollectionUtils.array(Entity.class, OfflinePlayer.class);
	}

	@Override
	protected String getPropertyName() {
		return "dropped item thrower";
	}

}
