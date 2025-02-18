package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

@Name("Block Hardness")
@Description("Obtains the block's hardness level (also known as \"strength\"). This number is used to calculate the time required to break each block.")
@Examples({"set {_hard} to block hardness of target block",
	"if block hardness of target block > 5:"})
@RequiredPlugins("Minecraft 1.13+")
@Since("2.6")
public class ExprBlockHardness extends SimplePropertyExpression<ItemType, Number> {

	static {
		if (Skript.methodExists(Material.class, "getHardness"))
			register(ExprBlockHardness.class, Number.class, "[block] hardness", "itemtypes");
	}

	@Nullable
	@Override
	public Number convert(ItemType itemType) {
		Material material = itemType.getMaterial();
		if (material.isBlock())
			return material.getHardness();
		return null;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "block hardness";
	}

}
