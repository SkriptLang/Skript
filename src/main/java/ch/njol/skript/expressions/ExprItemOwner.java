package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.UUIDUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Name("Dropped Item Owner")
@Description("""
	The uuid of the owner of the dropped item.
	Setting the owner of a dropped item means only that entity or player can pick it up.
	Dropping an item does not automatically make the entity or player the owner. 
	""")
@Example("""
		set the uuid of the dropped item owner of last dropped item to player
		if the uuid of the dropped item owner of last dropped item is uuid of player:
	""")
@Since("2.11")
public class ExprItemOwner extends SimplePropertyExpression<Item, UUID> {

	static {
		Skript.registerExpression(ExprItemOwner.class, UUID.class, ExpressionType.PROPERTY,
			"[the] uuid of [the] [dropped] item owner [of %itementities%]",
			"[the] [dropped] item owner's uuid [of %itementities%]");
	}

	@Override
	public @Nullable UUID convert(Item item) {
		return item.getOwner();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.DELETE)
			return CollectionUtils.array(Entity.class, OfflinePlayer.class, UUID.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		UUID uuid = delta == null ? null : UUIDUtils.asUUID(delta[0]);
		for (Item item : getExpr().getArray(event)) {
			item.setOwner(uuid);
		}
	}

	@Override
	public Class<? extends UUID> getReturnType() {
		return UUID.class;
	}

	@Override
	protected String getPropertyName() {
		return "uuid of the dropped item owner";
	}

}
