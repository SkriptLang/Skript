package org.skriptlang.skript.bukkit.itemcomponents.tool;

import io.papermc.paper.datacomponent.item.Tool.Rule;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import net.kyori.adventure.util.TriState;
import org.bukkit.block.BlockType;
import org.jspecify.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Wrapper for creating and modifying {@link Rule}s.
 */
@SuppressWarnings("UnstableApiUsage")
public class ToolRuleWrapper {

	private ToolWrapper wrapper;
	private Rule rule;

	/**
	 * Constructs a {@link ToolRuleWrapper} with {@code rule} and the {@code wrapper} it was grabbed from.
	 * @param wrapper The {@link ToolWrapper} {@code rule} originates from.
	 * @param rule The {@link Rule}.
	 */
	public ToolRuleWrapper(ToolWrapper wrapper, Rule rule) {
		this.wrapper = wrapper;
		this.rule = rule;
	}

	/**
	 * Constructs a {@link ToolRuleWrapper} with {@code rule}.
	 * @param rule The {@link Rule} to wrap.
	 */
	public ToolRuleWrapper(Rule rule) {
		this.rule = rule;
	}

	/**
	 * Constructs a {@link ToolRuleWrapper} with a blank {@link Rule}.
	 */
	public ToolRuleWrapper() {
		this.rule = new Builder().buildRule();
	}

	/**
	 * Returns the wrapped {@link Rule}.
	 * @return {@link Rule}.
	 */
	public Rule getRule() {
		return rule;
	}

	/**
	 * Modify the wrapped {@link #rule} by changing data through the builder.
	 * If the {@link #rule} originates from a {@link ToolWrapper}, will update the {@link ToolWrapper}.
	 * @param consumer The {@link Consumer} to change data of the {@link Builder}.
	 */
	public void modify(Consumer<Builder> consumer) {
		Builder builder = toBuilder();
		consumer.accept(builder);
		Rule newRule = builder.buildRule();
		if (wrapper != null) {
			// If the Tool component no longer contains this rule, should not assume modifications of this rule
			// will be applied to the Tool component
			if (wrapper.getComponent().rules().contains(rule)) {
				wrapper.editBuilder(wrapperBuilder -> {
					wrapperBuilder.removeRule(rule);
					wrapperBuilder.addRule(newRule);
				});
			} else {
				wrapper = null;
			}
		}
		rule = newRule;
	}

	/**
	 * Convert this {@link ToolRuleWrapper} to a {@link Builder}.
	 * @return {@link Builder}.
	 */
	public Builder toBuilder() {
		return new Builder()
			.correctForDrops(rule.correctForDrops())
			.blocks(rule.blocks())
			.speed(rule.speed());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ToolRuleWrapper other))
			return false;
		return rule.blocks().values().equals(other.rule.blocks().values())
			&& rule.correctForDrops().equals(other.rule.correctForDrops())
			&& Objects.equals(rule.speed(), other.rule.speed());
	}

	/**
	 * Class for building a {@link Rule} and {@link ToolRuleWrapper}.
	 */
	public static class Builder {

		private TriState correctForDrops = TriState.NOT_SET;

		private RegistryKeySet<BlockType> blocks =
			ComponentUtils.collectionToRegistryKeySet(Collections.emptyList(), RegistryKey.BLOCK);

		private @Nullable Float speed = 1f;

		public Builder() {}

		/**
		 * Whether the blocks in {@link #blocks} should drop items when broken by the item with this {@link Rule}.
		 * @param correctForDrops Whether drops should be dropped.
		 * @return {@code this}.
		 */
		public Builder correctForDrops(boolean correctForDrops) {
			this.correctForDrops = TriState.byBoolean(correctForDrops);
			return this;
		}

		/**
		 * Whether the blocks in {@link #blocks} should drop items when broken by the item with this {@link Rule}.
		 * @param correctForDrops Whether drops should be dropped.
		 * @return {@code this}.
		 */
		public Builder correctForDrops(TriState correctForDrops) {
			this.correctForDrops = correctForDrops;
			return this;
		}

		/**
		 * The blocks to be used for {@link #correctForDrops} and {@link #speed}.
		 * @param blocks The blocks.
		 * @return {@code this}.
		 */
		public Builder blocks(List<BlockType> blocks) {
			this.blocks = ComponentUtils.collectionToRegistryKeySet(blocks, RegistryKey.BLOCK);
			return this;
		}

		/**
		 * The blocks to be used for {@link #correctForDrops} and {@link #speed}.
		 * @param blocks The blocks.
		 * @return {@code this}.
		 */
		public Builder blocks(RegistryKeySet<BlockType> blocks) {
			this.blocks = blocks;
			return this;
		}

		/**
		 * The speed at which the blocks from {@link #blocks} should be broken by an item with this {@link Rule}.
		 * @param speed The speed.
		 * @return {@code this}.
		 */
		public Builder speed(@Nullable Float speed) {
			this.speed = speed;
			return this;
		}

		/**
		 * Builds the finalized {@link Rule}.
		 * @return The {@link Rule}.
		 */
		public Rule buildRule() {
			return new Rule() {
				@Override
				public RegistryKeySet<BlockType> blocks() {
					return blocks;
				}

				@Override
				public @Nullable Float speed() {
					return speed;
				}

				@Override
				public TriState correctForDrops() {
					return correctForDrops;
				}
			};
		}

		/**
		 * Builds the finalized {@link Rule} wrapped in {@link ToolRuleWrapper}.
		 * @return The {@link ToolRuleWrapper}.
		 */
		public ToolRuleWrapper build() {
			return new ToolRuleWrapper(buildRule());
		}

	}

}
