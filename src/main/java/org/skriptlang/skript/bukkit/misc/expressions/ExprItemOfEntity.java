package org.skriptlang.skript.bukkit.misc.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.DisplayEntitySlot;
import ch.njol.skript.util.slot.DroppedItemSlot;
import ch.njol.skript.util.slot.ItemFrameSlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.skript.util.slot.ThrowableProjectileSlot;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.ThrowableProjectile;
import org.jetbrains.annotations.Nullable;

@Name("Item of an Entity")
@Description({
	"An item associated with an entity. For dropped item entities, it gets the item that was dropped. ",
	"For item frames, the item inside the frame is returned.",
	"For throwable projectiles (snowballs, enderpearls etc.) it gets the displayed item.",
	"For display entities (snowballs, enderpearls etc.) it gets the displayed item.",
	"Other entities do not have items associated with them."
})
@Examples({
	"item of event-entity",
	"",
	"set the item inside of event-entity to a diamond sword named \"Example\""
})
@Since("2.2-dev35, 2.2-dev36 (improved), 2.5.2 (throwable projectiles)")
@RequiredPlugins("Minecraft 1.15.2+ (throwable projectiles), 1.19.4+ (displays)")
public class ExprItemOfEntity extends SimplePropertyExpression<Entity, Slot> {

	private static final boolean PROJECTILE_SUPPORT = Skript.classExists("org.bukkit.entity.ThrowableProjectile");
	private static final boolean DISPLAYS_SUPPORT = Skript.classExists("org.bukkit.entity.ItemDisplay");

	static {
		register(ExprItemOfEntity.class, Slot.class, "[the] item [inside]", "entities");
	}

	@Override
	public @Nullable Slot convert(Entity entity) {
		if (entity instanceof ItemFrame) {
			return new ItemFrameSlot((ItemFrame) entity);
		} else if (entity instanceof Item) {
			return new DroppedItemSlot((Item) entity);
		} else if (PROJECTILE_SUPPORT && entity instanceof ThrowableProjectile) {
			return new ThrowableProjectileSlot((ThrowableProjectile) entity);
		} else if (DISPLAYS_SUPPORT && entity instanceof ItemDisplay) {
			return new DisplayEntitySlot((ItemDisplay) entity);
		}
		return null; // Other entities don't have associated items
	}

	@Override
	protected String getPropertyName() {
		return "item of entity";
	}

	@Override
	public Class<? extends Slot> getReturnType() {
		return Slot.class;
	}

}
