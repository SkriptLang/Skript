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
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Item/Inventory Type With Name")
@Description({
	"Returns a copy of an item with a new name or creates a new inventory with a specific name.",
	"If you want to change the name of an existing item or inventory, use the <a href='#ExprName'>name</a> expression.",
})
@Example("give a diamond sword of sharpness 100 named \"&lt;gold&gt;Excalibur\" to the player")
@Example("set tool of player to the player's tool named \"&lt;gold&gt;Wand\"")
@Example("set the name of the player's tool to \"&lt;gold&gt;Wand\"")
@Example("open hopper inventory named \"Magic Hopper\" to player")
@Since("2.0, 2.2-dev34 (inventories)")
public class ExprNamed extends PropertyExpression<Object, Object> {

	static {
		Skript.registerExpression(ExprNamed.class, Object.class, ExpressionType.PROPERTY,
				"%itemtype/inventorytype% (named|with name[s]) %textcomponent%");
	}

	private Expression<Component> name;
	private Class<?>[] returnTypes;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr(exprs[0]);
		if (exprs[0] instanceof Literal<?> lit && lit.getSingle() instanceof InventoryType inventoryType && !inventoryType.isCreatable()) {
			Skript.error("Cannot create an inventory of type " + Classes.toString(inventoryType));
			return false;
		}
		//noinspection unchecked
		name = (Expression<Component>) exprs[1];

		List<Class<?>> returnTypes = new ArrayList<>();
		if (exprs[0].canReturn(ItemType.class)) {
			returnTypes.add(ItemType.class);
		}
		if (exprs[0].canReturn(InventoryType.class)) {
			returnTypes.add(Inventory.class);
		}
		this.returnTypes = returnTypes.toArray(new Class<?>[0]);

		return true;
	}

	@Override
	protected Object[] get(Event event, Object[] source) {
		Component name = this.name.getSingle(event);
		if (name == null) {
			return get(source, obj -> obj); // No name provided, do nothing
		}
		return get(source, object -> {
			if (object instanceof InventoryType inventoryType) {
				if (!inventoryType.isCreatable()) {
					return null;
				}
				return Bukkit.createInventory(null, inventoryType, name);
			} else {
				ItemType item = (ItemType) object;
				item = item.clone();
				ItemMeta meta = item.getItemMeta();
				meta.displayName(name);
				item.setItemMeta(meta);
				return item;
			}
		});
	}

	@Override
	public Class<?> getReturnType() {
		if (returnTypes.length == 1) {
			return returnTypes[0];
		}
		return Object.class;
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return Arrays.copyOf(returnTypes, returnTypes.length);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return getExpr().toString(event, debug) + " named " + name.toString(event, debug);
	}

}
