package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import org.bukkit.block.Block;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

@Name("Has Glowing Text")
@Description("Checks whether a sign (either a block or an item) has glowing text")
@Examples("if target block has glowing text")
@Since("2.8.0")
public class CondGlowingText extends PropertyCondition<Object> {

	static {
		if (Skript.methodExists(Sign.class, "isGlowingText"))
			register(CondGlowingText.class, PropertyType.HAVE, "glowing text", "blocks/itemtypes");
	}

	@Override
	public boolean check(Object obj) {
		if (obj instanceof Block block) {
			BlockState state = block.getState();
			return state instanceof Sign sign && sign.isGlowingText();
		} else if (obj instanceof ItemType itemType) {
			ItemMeta meta = itemType.getItemMeta();
			if (meta instanceof BlockStateMeta blockStateMeta) {
				BlockState state = blockStateMeta.getBlockState();
				return state instanceof Sign sign && sign.isGlowingText();
			}
		}
		return false;
	}

	@Override
	protected PropertyType getPropertyType() {
		return PropertyType.HAVE;
	}

	@Override
	protected String getPropertyName() {
		return "glowing text";
	}

}
