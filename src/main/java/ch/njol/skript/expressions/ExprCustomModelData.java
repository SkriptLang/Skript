package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Custom Model Data")
@Description("Get/set the CustomModelData tag for an item. (Value is an integer between 0 and 99999999)")
@Examples({"set custom model data of player's tool to 3",
	"set {_model} to custom model data of player's tool"})
@RequiredPlugins("1.14+")
@Since("2.5")
public class ExprCustomModelData extends SimplePropertyExpression<ItemType, Long> {
	
	static {
		if (Skript.methodExists(ItemMeta.class, "hasCustomModelData")) {
			register(ExprCustomModelData.class, Long.class, "[custom] model data", "itemtypes");
		}
	}
	
	@Override
	public Long convert(ItemType item) {
		ItemMeta meta = item.getItemMeta();
		assert meta != null;
		if (meta.hasCustomModelData())
			return (long) meta.getCustomModelData();
		else
			return 0L;
	}
	
	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}
	
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		return CollectionUtils.array(Number.class);
	}
	
	@Override
	protected String getPropertyName() {
		return "custom model data";
	}
	
	@Override
	public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
		long data = delta == null ? 0 : ((Number) delta[0]).intValue();
		if (data > 99999999 || data < 0) data = 0;
		for (ItemType item : getExpr().getArray(e)) {
			long oldData = 0;
			ItemMeta meta = item.getItemMeta();
			if (meta.hasCustomModelData())
				oldData = meta.getCustomModelData();
			switch (mode) {
				case ADD:
					data = oldData + data;
					break;
				case REMOVE:
					data = oldData - data;
					break;
				case DELETE:
				case RESET:
				case REMOVE_ALL:
					data = 0;
			}
			meta.setCustomModelData((int) data);
			item.setItemMeta(meta);
		}
	}
	
	@Override
	public String toString(@Nullable Event e, boolean d) {
		return "custom model data of " + getExpr().toString(e, d);
	}
	
}
