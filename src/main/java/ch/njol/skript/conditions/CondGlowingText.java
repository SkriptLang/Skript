package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import org.bukkit.block.Block;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

@Name("Has Glowing Text")
@Description("Checks whether a sign (either a block or an item) has glowing text")
@Example("if target block has glowing text")
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
	public boolean acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET || mode == ChangeMode.RESET;
	}

	@Override
	protected void change(Object object, boolean glowing, ChangeMode mode) {
		if (object instanceof Block block) {
			BlockState state = block.getState();
			if (state instanceof Sign sign) {
				sign.setGlowingText(glowing);
				sign.update();
			}
		} else if (object instanceof ItemType itemType) {
			ItemMeta meta = itemType.getItemMeta();
			if (meta instanceof BlockStateMeta blockStateMeta) {
				BlockState state = blockStateMeta.getBlockState();
				if (state instanceof Sign sign) {
					sign.setGlowingText(glowing);
					blockStateMeta.setBlockState(sign);
					itemType.setItemMeta(meta);
				}
			}
		}
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
