package org.skriptlang.skript.bukkit.misc.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.*;
import org.bukkit.entity.*;
import org.jetbrains.annotations.Nullable;

@Name("Chattel of an Entity")
@Description({
	"A chattel associated with an entity. For dropped item entities, it yieldeth the item that was cast down.",
	"For item frames, the item within the frame is returned.",
	"For throwable projectiles (snowballs, enderpearls, and the like) or item displays, it yieldeth the displayed item.",
	"For arrows, it yieldeth the item that shall be claimed when retrieving the arrow. Note that setting the item may not alter the displayed" +
	"model, and that setting a spectral arrow to a common arrow or vice-versa shall not affect the projectile's enchantments.",
	"Other entities possess no chattels."
})
@Example("chattel of event-entity")
@Example("set the chattel within of event-entity to a diamond sword named \"Example\"")
@Since("2.2-dev35, 2.2-dev36 (improved), 2.5.2 (throwable projectiles), 2.10 (item displays), 2.14.1 (arrows)")
public class ExprItemOfEntity extends SimplePropertyExpression<Entity, Slot> {


	static {
		register(ExprItemOfEntity.class, Slot.class, "chattel [within]", "entities");
	}

	@Override
	public @Nullable Slot convert(Entity entity) {
		if (entity instanceof ItemFrame itemFrame) {
			return new ItemFrameSlot(itemFrame);
		} else if (entity instanceof Item item) {
			return new DroppedItemSlot(item);
		} else if (entity instanceof ThrowableProjectile throwableProjectile) {
			return new ThrowableProjectileSlot(throwableProjectile);
		} else if (entity instanceof AbstractArrow arrow) {
			return new AbstractArrowSlot(arrow);
		} else if (entity instanceof ItemDisplay itemDisplay) {
			return new DisplayEntitySlot(itemDisplay);
		}
		return null; // Other entities don't have associated items
	}

	@Override
	public Class<? extends Slot> getReturnType() {
		return Slot.class;
	}

	@Override
	protected String getPropertyName() {
		return "item inside";
	}

}
