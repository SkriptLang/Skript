package org.skriptlang.skript.bukkit.text.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.bukkit.text.TextComponentParser;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Show Item Hover Text")
@Description("""
	Creates a '<hover:show_item:...>' string from an item stack, which can be used to show the item's hover text in chat.
	""")
@Example("""
	on right click on a chest:
		send "Contents:"
		loop items in inventory of event-block:
			send formatted " - %item hover text of loop-item%%loop-item%"
	""")
@Since("INSERT VERSION")
public class ExprItemHoverText extends SimplePropertyExpression<ItemStack, String> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprItemHoverText.class, String.class, "[show] item hover text", "itemstacks", false)
				.supplier(ExprItemHoverText::new)
				.build());
	}

	@Override
	public String convert(ItemStack from) {
		Component dummy = Component.empty().hoverEvent(from.asHoverEvent());
		return TextComponentParser.instance().toString(dummy);
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "item hover text";
	}

}
