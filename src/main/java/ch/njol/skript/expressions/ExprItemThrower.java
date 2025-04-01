package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.UUIDUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Name("Dropped Item Thrower")
@Description("The uuid of the entity or player that threw/dropped the dropped item.")
@Example("""
	set the uuid of the dropped item thrower of {_dropped item} to player
	if the uuid of the dropped item thrower of {_dropped item} is uuid of player:
	"""
)
@Example("clear the item thrower of {_dropped item}")
@Since("2.11")
public class ExprItemThrower extends SimplePropertyExpression<Item, UUID> {

	static {
		Skript.registerExpression(ExprItemThrower.class, UUID.class, ExpressionType.PROPERTY,
			"[the] uuid of [the] [dropped] item thrower [of %itementities%]",
			"[the] [dropped] item thrower's uuid [of %itementities%]");
	}

	@Override
	public @Nullable UUID convert(Item item) {
		return item.getThrower();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(OfflinePlayer.class, Entity.class, UUID.class);
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
	public Class<UUID> getReturnType() {
		return UUID.class;
	}

	@Override
	protected String getPropertyName() {
		return "uuid of the dropped item thrower";
	}

}
