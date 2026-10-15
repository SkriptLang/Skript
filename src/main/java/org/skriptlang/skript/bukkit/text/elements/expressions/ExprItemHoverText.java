package org.skriptlang.skript.bukkit.text.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Show Item Hover Component")
@Description("""
	Creates a '<hover:show_item:...>' component from an item stack, which can be used to show the item's hover text in chat.
	""")
@Example("""
	on right click on a chest:
		send "Contents:"
		loop items in inventory of event-block:
			send formatted " - %item hover text of loop-item%%loop-item%"
	""")
@Since("INSERT VERSION")
public class ExprItemHoverText extends SimplePropertyExpression<ItemStack, Component> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			SyntaxInfo.Expression.builder(ExprItemHoverText.class, Component.class)
				.addPattern("[a[n]] [show] item hover [text] component[s] (for|using|from) %itemstacks%")
				.supplier(ExprItemHoverText::new)
				.build());
	}

	@Override
	public Component convert(ItemStack from) {
		return Component.empty().hoverEvent(from.asHoverEvent());
	}

	@Override
	public Class<? extends Component> getReturnType() {
		return Component.class;
	}

	@Override
	protected String getPropertyName() {
		return "item hover component";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "item hover component for " + getExpr().toString(event, debug);
	}

}
