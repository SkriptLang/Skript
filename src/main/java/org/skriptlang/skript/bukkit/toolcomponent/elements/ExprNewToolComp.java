package org.skriptlang.skript.bukkit.toolcomponent.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.jetbrains.annotations.Nullable;

@Name("New Tool Component")
@Description("Gets a blank tool component.")
@Examples({
	"set {_component} to a new blank tool component",
	"set the tool component of {_item} to {_component}"
})
@RequiredPlugins("Minecraft 1.20.6+")
@Since("INSERT VERSION")
public class ExprNewToolComp extends SimpleExpression<ToolComponent> {

	static {
		Skript.registerExpression(ExprNewToolComp.class, ToolComponent.class, ExpressionType.SIMPLE,
			"a new [blank|empty] tool component");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected ToolComponent @Nullable [] get(Event event) {
		return new ToolComponent[]{(new ItemStack(Material.APPLE)).getItemMeta().getTool()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<ToolComponent> getReturnType() {
		return ToolComponent.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a new tool component";
	}

}
