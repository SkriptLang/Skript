package org.skriptlang.skript.bukkit.toolcomponent.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.components.ToolComponent.ToolRule;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Tool Rule - Blocks")
@Description("The blocks of a tool rule.")
@Examples({
	"set {_rule} to a new tool rule with block types oak log, stone and obsidian",
	"set the tool rule speed of {_rule} to 10",
	"enable the tool rule drops of {_rule}",
	"add {_rule} to the tool rules of {_item}"
})
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")
public class ExprToolRuleBlocks extends PropertyExpression<ToolRule, ItemType> {

	static {
		Skript.registerExpression(ExprToolRuleBlocks.class, ItemType.class, ExpressionType.PROPERTY,
			"[the] tool rule[s] block types of %toolrules%");
	}


	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends ToolRule>) exprs[0]);
		return true;
	}

	@Override
	protected ItemType @Nullable [] get(Event event, ToolRule[] source) {
		List<ItemType> types = new ArrayList<>();
		for (ToolRule rule : getExpr().getArray(event)) {
			types.addAll(rule.getBlocks().stream().map(ItemType::new).toList());
		}
		return types.toArray(new ItemType[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(ItemType[].class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		ItemType[] types = (ItemType[]) delta;
		List<Material> materials = Arrays.stream(types).map(ItemType::getMaterial).toList();
		for (ToolRule rule : getExpr().getArray(event)) {
			rule.setBlocks(materials);
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<ItemType> getReturnType() {
		return ItemType.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the tool rule block types " + getExpr().toString(event, debug);
	}

}
