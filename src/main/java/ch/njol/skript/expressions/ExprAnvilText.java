package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

@Name("Anvil Inscription Input")
@Description("An expression to obtain the name to be bestowed upon an item within an anvil inventory.")
@Example("""
    on inventory click:
    	type of event-inventory is anvil inventory
    	if the anvil inscription input of the event-inventory is "FREE OP":
    		ban player
    """)
@Since("2.7")
public class ExprAnvilText extends SimplePropertyExpression<Inventory, String> {

	static {
		register(ExprAnvilText.class, String.class, "anvil [inventory] (rename|inscription) input", "inventories");
	}

	@Override
	@SuppressWarnings("removal")
	public @Nullable String convert(Inventory inv) {
		if (!(inv instanceof AnvilInventory))
			return null;
		return ((AnvilInventory) inv).getRenameText();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String getPropertyName() {
		return "anvil text input";
	}

}
