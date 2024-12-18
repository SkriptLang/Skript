package org.skriptlang.skript.bukkit.toolcomponent.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.inventory.meta.components.ToolComponent.ToolRule;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Name("Tool Component - Tool Rules")
@Description("The tool rules of a tool component.")
@Examples({
	"set {_rules::*} to the tool rules of {_item}",
	"",
	"set {_component} to the tool component of {_tool}",
	"set {_rules::*} to the tool rules of {_component}"
})
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")
public class ExprToolCompRules extends PropertyExpression<Object, ToolRule> {

	static {
		Skript.registerExpression(ExprToolCompRules.class, ToolRule.class, ExpressionType.PROPERTY,
			"[the] tool rules of %itemstacks/itemtypes/slots/toolcomponents%");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		return true;
	}

	@Override
	protected ToolRule @Nullable [] get(Event event, Object[] source) {
		List<ToolRule> rules = new ArrayList<>();
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof ToolComponent component) {
				rules.addAll(component.getRules());
			} else {
				ItemStack itemStack = ItemUtils.asItemStack(object);
				if (itemStack == null)
					continue;
				rules.addAll(itemStack.getItemMeta().getTool().getRules());
			}
		}
		return rules.toArray(ToolRule[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, ADD, REMOVE -> CollectionUtils.array(ToolRule[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		ToolRule[] rules = null;
		if (delta != null && delta[0] != null)
			rules = (ToolRule[]) delta;
		List<ToolRule> ruleList = rules != null ? Arrays.stream(rules).toList() : new ArrayList<>();

		Consumer<ToolComponent> changer = switch (mode) {
			case SET -> component -> {
				component.setRules(ruleList);
			};
			case DELETE -> component -> {
				component.getRules().clear();
			};
			case ADD -> component -> {
				List<ToolRule> current = component.getRules();
				current.addAll(ruleList);
				component.setRules(current);
			};
			case REMOVE -> component -> {
				ruleList.forEach(component::removeRule);
			};
			default -> throw new IllegalStateException("Unexpected value: "  + mode);
		};

		for (Object object : getExpr().getArray(event)) {
			if (object instanceof ToolComponent component) {
				changer.accept(component);
			} else {
				ItemStack itemStack = ItemUtils.asItemStack(object);
				if (itemStack == null)
					continue;
				ItemMeta meta = itemStack.getItemMeta();
				ToolComponent toolComponent = meta.getTool();
				changer.accept(toolComponent);
				meta.setTool(toolComponent);
				itemStack.setItemMeta(meta);
				if (object instanceof Slot slot) {
					slot.setItem(itemStack);
				} else if (object instanceof ItemType itemType) {
					itemType.setItemMeta(meta);
				} else if (object instanceof ItemStack itemStack1) {
					itemStack1.setItemMeta(meta);
				}
			}
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<ToolRule> getReturnType() {
		return ToolRule.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the tool rules of " + getExpr().toString(event, debug);
	}

}
