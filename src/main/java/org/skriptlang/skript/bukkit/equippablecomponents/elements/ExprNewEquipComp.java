package org.skriptlang.skript.bukkit.equippablecomponents.elements;

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
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.jetbrains.annotations.Nullable;

@Name("New Equippable Component")
@Description("Gets a blank equippable component.")
@Examples({
	"set {_component} to a new blank equippable component",
	"set the equippable component of {_item} to {_component}"
})
@RequiredPlugins("Minecraft 1.21.2+")
@Since("INSERT VERSION")
public class ExprNewEquipComp extends SimpleExpression<EquippableComponent> {

	static {
		Skript.registerExpression(ExprNewEquipComp.class, EquippableComponent.class, ExpressionType.SIMPLE,
			"a new [blank|empty] equippable component");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected EquippableComponent @Nullable [] get(Event event) {
		return new EquippableComponent[]{(new ItemStack(Material.APPLE)).getItemMeta().getEquippable()};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<EquippableComponent> getReturnType() {
		return EquippableComponent.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "a new equippable component";
	}

}
