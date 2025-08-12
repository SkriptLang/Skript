package ch.njol.skript.expressions;

import ch.njol.skript.doc.Example;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;

@Name("Custom Chest Inventory")
@Description({
	"Returns a chest inventory with the given amount of rows and the name.",
	"Use the <a href=#EffOpenInventory>open inventory</a> effect to open it."
})
@Example("open chest inventory with 1 row named \"test\" to player")
@Example("""
	set {_inventory} to a chest inventory with 1 row
	set slot 4 of {_inventory} to a diamond named "example"
	open {_inventory} to player
	""")
@Example("open chest inventory named \"<#00ff00>hex coloured title!\" with 6 rows to player")
@Since("2.2-dev34, 2.8.0 (chat format)")
public class ExprChestInventory extends SimpleExpression<Inventory> {

	static {
		Skript.registerExpression(ExprChestInventory.class, Inventory.class, ExpressionType.COMBINED,
				"[a] [new] chest inventory (named|with name) %textcomponent% [with %-number% row[s]]",
				"[a] [new] chest inventory with %number% row[s] [(named|with name) %-textcomponent%]");
	}

	private static final Component DEFAULT_CHEST_TITLE = InventoryType.CHEST.defaultTitle();
	private static final int DEFAULT_CHEST_ROWS = InventoryType.CHEST.getDefaultSize() / 9;

	private @Nullable Expression<Number> rows;

	private @Nullable Expression<Component> name;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		name = (Expression<Component>) exprs[matchedPattern];
		rows = (Expression<Number>) exprs[matchedPattern ^ 1];
		return true;
	}

	@Override
	protected Inventory[] get(Event event) {
		Component name = this.name != null ? this.name.getOptionalSingle(event).orElse(DEFAULT_CHEST_TITLE) : DEFAULT_CHEST_TITLE;
		Number rows = this.rows != null ? this.rows.getOptionalSingle(event).orElse(DEFAULT_CHEST_ROWS) : DEFAULT_CHEST_ROWS;

		int size = rows.intValue() * 9;
		if (size % 9 != 0)
			size = 27;

		// Sanitize inventory size
		if (size < 0)
			size = 0;
		if (size > 54) // Too big values cause visual weirdness, or exceptions on newer server versions
			size = 54;

		return CollectionUtils.array(Bukkit.createInventory(null, size, name));
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Inventory> getReturnType() {
		return Inventory.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("a chest inventory");
		if (name != null) {
			builder.append("named", name);
		}
		if (rows != null) {
			builder.append("with", rows, "rows");
		}
		return builder.toString();
	}

}
