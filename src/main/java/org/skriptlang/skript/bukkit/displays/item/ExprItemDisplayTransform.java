package org.skriptlang.skript.bukkit.displays.item;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Item Display Transform")
@Description("Returns or changes the <a href='classes.html#itemdisplaytransform'>item display transform</a> of <a href='classes.html#display'>item displays</a>.")
@Examples({
	"set the item transform of the last spawned item display to first person left handed",
	"set the item transform of the last spawned item display to no transform # Reset to default"
})
@Since("2.10")
public class ExprItemDisplayTransform extends SimplePropertyExpression<Display, ItemDisplayTransform> {

	static {
		registerDefault(ExprItemDisplayTransform.class, ItemDisplayTransform.class, "item [display] transform", "displays");
	}

	@Override
	public @Nullable ItemDisplayTransform convert(Display display) {
		if (display instanceof ItemDisplay itemDisplay)
			return itemDisplay.getItemDisplayTransform();
		return null;
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case RESET -> CollectionUtils.array();
			case SET -> CollectionUtils.array(ItemDisplayTransform.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		//noinspection ConstantConditions
		ItemDisplayTransform transform = mode == ChangeMode.SET ? (ItemDisplayTransform) delta[0] : ItemDisplayTransform.NONE;
		for (Display display : getExpr().getArray(event)) {
			if (display instanceof ItemDisplay itemDisplay)
				itemDisplay.setItemDisplayTransform(transform);
		}
	}

	@Override
	public Class<? extends ItemDisplayTransform> getReturnType() {
		return ItemDisplayTransform.class;
	}

	@Override
	protected String getPropertyName() {
		return "item display transform";
	}

}
