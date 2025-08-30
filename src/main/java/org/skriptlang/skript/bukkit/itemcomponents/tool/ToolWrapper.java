package org.skriptlang.skript.bukkit.itemcomponents.tool;

import ch.njol.skript.Skript;
import ch.njol.skript.util.ItemSource;
import io.papermc.paper.datacomponent.DataComponentType.Valued;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Tool;
import io.papermc.paper.datacomponent.item.Tool.Builder;
import io.papermc.paper.datacomponent.item.Tool.Rule;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentWrapper;
import org.skriptlang.skript.bukkit.itemcomponents.tool.ToolWrapper.ToolBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A {@link ComponentWrapper} for getting and setting data on an {@link Tool} component.
 */
@SuppressWarnings("UnstableApiUsage")
public class ToolWrapper extends ComponentWrapper<Tool, ToolBuilder> {

	public static final boolean HAS_CAN_DESTROY_BLOCKS_IN_CREATIVE =
		Skript.methodExists(ToolComponent.class, "canDestroyBlocksInCreative");

	public ToolWrapper(ItemStack itemStack) {
		super(itemStack);
	}

	public ToolWrapper(ItemSource<?> itemSource) {
		super(itemSource);
	}

	public ToolWrapper(Tool component) {
		super(component);
	}

	public ToolWrapper(ToolBuilder builder) {
		super(builder);
	}

	@Override
	public Valued<Tool> getDataComponentType() {
		return DataComponentTypes.TOOL;
	}

	@Override
	protected Tool getComponent(ItemStack itemStack) {
		Tool tool = itemStack.getData(DataComponentTypes.TOOL);
		if (tool != null)
			return tool;
		return Tool.tool().build();
	}

	@Override
	protected ToolBuilder getBuilder(ItemStack itemStack) {
		Tool tool = itemStack.getData(DataComponentTypes.TOOL);
		if (tool != null)
			return new ToolBuilder(tool);
		return new ToolBuilder();
	}

	@Override
	protected void setComponent(ItemStack itemStack, Tool tool) {
		itemStack.setData(DataComponentTypes.TOOL, tool);
	}

	@Override
	public ToolBuilder toBuilder(Tool component) {
		return new ToolBuilder(component);
	}

	@Override
	public ToolWrapper clone() {
		ToolWrapper clone = newInstance();
		Tool base = getComponent();
		clone.editBuilder(builder -> {
			builder.addRules(new ArrayList<>(base.rules()));
			builder.damagePerBlock(base.damagePerBlock());
			builder.defaultMiningSpeed(base.defaultMiningSpeed());
			if (HAS_CAN_DESTROY_BLOCKS_IN_CREATIVE)
				builder.canDestroyBlocksInCreative(base.canDestroyBlocksInCreative());
		});
		return clone;
	}

	@Override
	public Tool newComponent() {
		return newBuilder().build();
	}

	@Override
	public ToolBuilder newBuilder() {
		return new ToolBuilder();
	}

	@Override
	public ToolWrapper newWrapper() {
		return newInstance();
	}

	/**
	 * Get all {@link Rule}s from {@link #component} in the form of {@link ToolRuleWrapper}s.
	 * @return The {@link ToolRuleWrapper}s.
	 */
	public List<ToolRuleWrapper> getRules() {
		List<ToolRuleWrapper> wrappers = new ArrayList<>();
		getComponent().rules().forEach(rule -> wrappers.add(new ToolRuleWrapper(this, rule)));
		return wrappers;
	}

	/**
	 * Get a {@link ToolWrapper} with a blank {@link ToolComponent}.
	 */
	public static ToolWrapper newInstance() {
		return new ToolWrapper(new ToolBuilder());
	}

	/**
	 * Custom builder class for {@link Tool}.
	 */
	public static class ToolBuilder implements Builder {

		private boolean canDestroyBlocksInCreative = false;
		private int damagePerBlock = 1;
		private float defaultMiningSpeed = 1;
		private List<Rule> rules = new ArrayList<>();

		public ToolBuilder() {}

		/**
		 * Constructs a {@link ToolBuilder} with the same data from {@code tool}.
		 * @param tool The {@link Tool} component to copy data from/
		 */
		public ToolBuilder(Tool tool) {
			damagePerBlock = tool.damagePerBlock();
			defaultMiningSpeed = tool.defaultMiningSpeed();
			rules = new ArrayList<>(tool.rules());
			if (HAS_CAN_DESTROY_BLOCKS_IN_CREATIVE)
				canDestroyBlocksInCreative = tool.canDestroyBlocksInCreative();
		}

		/**
		 * Set whether the {@link Tool} component should break blocks when used in creative.
		 * @param canDestroyBlocksInCreative Whether to break blocks.
		 * @return {@code this}.
		 */
		public ToolBuilder canDestroyBlocksInCreative(boolean canDestroyBlocksInCreative) {
			this.canDestroyBlocksInCreative = canDestroyBlocksInCreative;
			return this;
		}

		/**
		 * Set the amount of damage the {@link Tool} component should do per block.
		 * @param damagePerBlock The amount of damage.
		 * @return {@code this}.
		 */
		public ToolBuilder damagePerBlock(int damagePerBlock) {
			this.damagePerBlock = damagePerBlock;
			return this;
		}

		/**
		 * Set the default mining speed of the {@link Tool} component.
		 * @param defaultMiningSpeed The mining speed.
		 * @return {@code this}.
		 */
		public ToolBuilder defaultMiningSpeed(float defaultMiningSpeed) {
			this.defaultMiningSpeed = defaultMiningSpeed;
			return this;
		}

		/**
		 * Set the tool rules for the {@link Tool} component.
		 * @param rules The tool rules.
		 * @return {@code this}.
		 */
		public ToolBuilder setRules(List<Rule> rules) {
			this.rules.clear();
			this.rules.addAll(rules);
			return this;
		}

		@Override
		public ToolBuilder addRules(Collection<Rule> rules) {
			this.rules.addAll(rules);
			return this;
		}

		@Override
		public ToolBuilder addRule(Rule rule) {
			this.rules.add(rule);
			return this;
		}

		/**
		 * Remove tool rules for the {@link Tool} component.
		 * @param rules The tool rules.
		 * @return {@code this}.
		 */
		public ToolBuilder removeRules(Collection<Rule> rules) {
			this.rules.removeAll(rules);
			return this;
		}

		/**
		 * Remove a tool rule for the {@link Tool} component.
		 * @param rule The tool rule.
		 * @return {@code this}.
		 */
		public ToolBuilder removeRule(Rule rule) {
			this.rules.remove(rule);
			return this;
		}

		@Override
		public Tool build() {
			Builder builder = Tool.tool()
				.defaultMiningSpeed(defaultMiningSpeed)
				.damagePerBlock(damagePerBlock)
				.addRules(rules);
			if (HAS_CAN_DESTROY_BLOCKS_IN_CREATIVE)
				builder.canDestroyBlocksInCreative(canDestroyBlocksInCreative);
			return builder.build();
		}

	}

}
