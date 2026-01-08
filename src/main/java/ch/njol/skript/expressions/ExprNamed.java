package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Peter Güttinger
 */
@Name("Named Item/Inventory")
@Description("Directly names an item/inventory, useful for defining a named item/inventory in a script. " +
		"If you want to (re)name existing items/inventories you can either use this expression or use <code>set <a href='#PropExprName'>name of &lt;item/inventory&gt;</a> to &lt;text&gt;</code>.")
@Example("give a diamond sword of sharpness 100 named \"<gold>Excalibur\" to the player")
@Example("set tool of player to the player's tool named \"<gold>Wand\"")
@Example("set the name of the player's tool to \"<gold>Wand\"")
@Example("open hopper inventory named \"Magic Hopper\" to player")
@Since("2.0, 2.2-dev34 (inventories)")
public class ExprNamed extends PropertyExpression<Object, Object> {
	static {
		Skript.registerExpression(ExprNamed.class, Object.class, ExpressionType.PROPERTY,
				"%itemtype/inventorytype% (named|with name[s]) %string%");
	}

	private Expression<String> name;
	private Class<?>[] returnTypes;

	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr(exprs[0]);
		if (exprs[0] instanceof Literal<?> lit && lit.getSingle() instanceof InventoryType inventoryType && !inventoryType.isCreatable()) {
			Skript.error("Cannot create an inventory of type " + Classes.toString(inventoryType));
			return false;
		}
		//noinspection unchecked
		name = (Expression<String>) exprs[1];

		List<Class<?>> returnTypes = new ArrayList<>();
		if (exprs[0].canReturn(ItemType.class))
			returnTypes.add(ItemType.class);
		if (exprs[0].canReturn(InventoryType.class))
			returnTypes.add(Inventory.class);
		this.returnTypes = returnTypes.toArray(new Class<?>[0]);

		return true;
	}
	
	@Override
	protected Object[] get(Event event, Object[] source) {
		String name = this.name.getSingle(event);
		if (name == null)
			return get(source, obj -> obj); // No name provided, do nothing
		return get(source, object -> {
			if (object instanceof InventoryType inventoryType) {
				if (!inventoryType.isCreatable())
					return null;
				return Bukkit.createInventory(null, inventoryType, name);
			}
			if (object instanceof ItemStack stack) {
				stack = stack.clone();
				ItemMeta meta = stack.getItemMeta();
				if (meta != null) {
					meta.setDisplayName(name);
					stack.setItemMeta(meta);
				}
				return new ItemType(stack);
			}
			ItemType item = (ItemType) object;
			item = item.clone();
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(name);
			item.setItemMeta(meta);
			return item;
		});
	}

	@Override
	public Class<?> getReturnType() {
		if (returnTypes.length == 1)
			return returnTypes[0];
		return Object.class;
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return Arrays.copyOf(returnTypes, returnTypes.length);
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return getExpr().toString(e, debug) + " named " + name;
	}
	
}
