package org.skriptlang.skript.bukkit.itemcomponents.tool;

import io.papermc.paper.datacomponent.item.Tool.Rule;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.util.TriState;
import org.bukkit.block.BlockType;
import org.jspecify.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public class ToolRuleWrapper {

	private ToolWrapper wrapper;
	private Rule rule;

	public ToolRuleWrapper(ToolWrapper wrapper, Rule rule) {
		this.wrapper = wrapper;
		this.rule = rule;
	}

	public ToolRuleWrapper(Rule rule) {
		this.rule = rule;
	}

	public ToolRuleWrapper() {
		this.rule = new Builder().buildRule();
	}

	public Rule getRule() {
		return rule;
	}

	public void modify(Consumer<Builder> consumer) {
		Builder builder = toBuilder();
		consumer.accept(builder);
		Rule newRule = builder.buildRule();
		if (wrapper != null) {
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

	public Builder toBuilder() {
		return new Builder()
			.correctForDrops(rule.correctForDrops())
			.blocks(rule.blocks())
			.speed(rule.speed());
	}

	public static class Builder {

		private TriState correctForDrops = TriState.NOT_SET;

		private RegistryKeySet<BlockType> blocks =
			ComponentUtils.collectionToRegistryKeySet(Collections.emptyList(), RegistryKey.BLOCK);

		private Float speed = 1f;

		public Builder() {}

		public Builder correctForDrops(boolean correctForDrops) {
			this.correctForDrops = TriState.byBoolean(correctForDrops);
			return this;
		}

		public Builder correctForDrops(TriState correctForDrops) {
			this.correctForDrops = correctForDrops;
			return this;
		}

		public Builder blocks(List<BlockType> blocks) {
			this.blocks = RegistrySet.keySetFromValues(RegistryKey.BLOCK, blocks);
			return this;
		}

		public Builder blocks(RegistryKeySet<BlockType> blocks) {
			this.blocks = blocks;
			return this;
		}

		public Builder speed(Float speed) {
			this.speed = speed;
			return this;
		}

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

		public ToolRuleWrapper build() {
			return new ToolRuleWrapper(buildRule());
		}

	}

}
