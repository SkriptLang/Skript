package org.skriptlang.skript.bukkit.toolcomponent.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.ToolComponent.ToolRule;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Name("New Tool Rule")
@Description({
	"Gets a new tool rule with provided block types.",
	"NOTE: A tool rule must have at least one block type or will be considered invalid."
})
@Examples({
	"set {_rule} to a new tool rule with block types oak log, stone and obsidian",
	"set the tool rule speed of {_rule} to 10",
	"enable the tool rule drops of {_rule}",
	"add {_rule} to the tool rules of {_item}"
})
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")
public class ExprNewToolRule extends SimpleExpression<ToolRule> {

	static {
		Skript.registerExpression(ExprNewToolRule.class, ToolRule.class, ExpressionType.SIMPLE,
			"a new tool rule with block types %itemtypes%");
	}

	private Expression<ItemType> types;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		types = (Expression<ItemType>) expressions[0];
		return true;
	}

	@Override
	protected ToolRule @Nullable [] get(Event event) {
		List<Material> materials = types.stream(event).map(ItemType::getMaterial).toList();
		return new ToolRule[]{(new ItemStack(Material.APPLE).getItemMeta().getTool().addRule(materials, null, null))};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends ToolRule> getReturnType() {
		return ToolRule.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return null;
	}

}
