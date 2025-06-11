package org.skriptlang.skript.bukkit.itemcomponents.tool.elements;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.components.ToolComponent.ToolRule;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolExperiment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Tool Rule - Blocks")
@Description("""
	The block types of a tool rule.
	A tool rule consists of:
		- Block types that the rule should be applied to
		- Mining speed for the blocks
		- Whether the blocks should drop their respective items
	""")
@Example("""
	set {_rule} to a custom tool rule with block types oak log, stone and obsidian
	set the tool rule speed of {_rule} to 10
	enable the tool rule drops for {_rule}
	add {_rule} to the tool rules of {_item}
	""")
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")

@SuppressWarnings("UnstableApiUsage")
public class ExprToolRuleBlocks extends PropertyExpression<ToolRule, ItemType> implements ToolExperiment {

	static {
		registerDefault(ExprToolRuleBlocks.class, ItemType.class, "tool rule[s] block types", "toolrules");
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
		assert delta != null;
		ItemType[] types = (ItemType[]) delta;
		List<Material> materials = Arrays.stream(types).map(ItemType::getMaterial).toList();
		getExpr().stream(event).forEach(toolRule -> toolRule.setBlocks(materials));
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
		return new SyntaxStringBuilder(event, debug)
			.append("the tool rule block types of", getExpr())
			.toString();
	}

}
