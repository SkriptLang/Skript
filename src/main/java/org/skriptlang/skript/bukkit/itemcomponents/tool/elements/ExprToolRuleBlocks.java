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
import org.bukkit.Registry;
import org.bukkit.block.BlockType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentUtils;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolExperimentalSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolRuleWrapper;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Name("Tool Rule - Blocks")
@Description("""
	The block types of a tool rule.
	A tool rule consists of:
		- Block types that the rule should be applied to
		- Mining speed for the blocks
		- Whether the blocks should drop their respective items
	NOTE: Tool component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	set {_rule} to a custom tool rule with block types oak log, stone and obsidian
	set the tool rule speed of {_rule} to 10
	enable the tool rule drops for {_rule}
	add {_rule} to the tool rules of {_item}
	""")
@RequiredPlugins("Minecraft 1.21.3+")
@Since("INSERT VERSION")
@SuppressWarnings("UnstableApiUsage")
public class ExprToolRuleBlocks extends PropertyExpression<ToolRuleWrapper, ItemType> implements ToolExperimentalSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprToolRuleBlocks.class, ItemType.class, "tool rule[s] block types", "toolrules", true)
				.supplier(ExprToolRuleBlocks::new)
				.build()
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<ToolRuleWrapper>) exprs[0]);
		return true;
	}

	@Override
	protected ItemType @Nullable [] get(Event event, ToolRuleWrapper[] source) {
		List<ItemType> types = new ArrayList<>();
		for (ToolRuleWrapper ruleWrapper : source) {
			Collection<BlockType> blockTypes = ComponentUtils.registryKeySetToCollection(
				ruleWrapper.getRule().blocks(),
				Registry.BLOCK
			);
			List<ItemType> itemTypes = blockTypes.stream()
				.map(BlockType::asMaterial)
				.map(ItemType::new)
				.toList();
			types.addAll(itemTypes);
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
		ItemType[] types = Arrays.stream(delta)
			.map(o -> (ItemType) o)
			.toArray(ItemType[]::new);
		List<BlockType> blockTypes = Arrays.stream(types)
			.map(ItemType::getMaterial)
			.filter(Material::isBlock)
			.map(Material::asBlockType)
			.toList();
		getExpr().stream(event).forEach(ruleWrapper ->
			ruleWrapper.modify(builder -> builder.blocks(blockTypes)));
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
