package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jaxen.expr.Expr;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO make a 'line %number% of %text%' expression and figure out how to deal with signs (4 lines, delete = empty, etc...)
 */
@Name("Lore")
@Description("An item's lore.")
@Example("""
	set {_item} to diamond named "Bling Diamond" with lore "&bThe blingiest of diamonds"
	add "With extra steps" to lore of {_item}
	remove line 1 of lore of {_item} from lore of {_item}
	set line 3 of lore of {_item} to "-----"
	""")
@Examples("set the 1st line of the item's lore to \"&lt;orange&gt;Excalibur 2.0\"")
@Since("2.1")
public class ExprLore extends SimpleExpression<String> {

	private static final boolean IS_RUNNING_1_21_5 = Skript.isRunningMinecraft(1,21,5);

	static {
		Skript.registerExpression(ExprLore.class, String.class, ExpressionType.PROPERTY,
				"[the] lore of %itemstack/itemtype%",
						"%itemstack/itemtype%'[s] lore",
						"[the] line %number% of [the] lore of %itemstack/itemtype%",
						"[the] line %number% of %itemstack/itemtype%'[s] lore",
						"[the] %number%(st|nd|rd|th) line of [the] lore of %itemstack/itemtype%",
						"[the] %number%(st|nd|rd|th) line of %itemstack/itemtype%'[s] lore");
	}

	private @Nullable Expression<Number> lineNumber;
	private Expression<?> item;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		lineNumber = exprs.length > 1 ? (Expression<Number>) exprs[0] : null;
		item = exprs[exprs.length - 1];
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
		if (!validateItem(item.getSingle(event)))
			return null;
		ItemStack itemStack = ItemUtils.asItemStack(item.getSingle(event));
		assert itemStack != null; // Validated in validateItem
		ItemMeta itemMeta = itemStack.getItemMeta();
		//noinspection deprecation
 		List<String> itemLore = itemMeta.getLore();
		if (itemLore == null || itemLore.isEmpty())
			return null;

		if (lineNumber == null) {
			return itemLore.toArray(String[]::new);
		}

		int loreIndex = this.lineNumber.getOptionalSingle(event).orElse(0).intValue() -1;
		if (loreIndex < 0 || loreIndex >= itemLore.size()) {
			return null;
		}
		return new String[]{itemLore.get(loreIndex)};
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		boolean acceptsMany = this.lineNumber == null;
		return switch (mode) {
			case SET -> CollectionUtils.array(acceptsMany ? String[].class : String.class);
			case DELETE -> CollectionUtils.array();
			case ADD, REMOVE, REMOVE_ALL -> {
				// this should not accept remove "hello" from line 1 of lore of player's tool
				// users should instead use replace "hello" in line 1 of lore of player's tool
				if (!acceptsMany) {
					Skript.error("You cannot remove/add lore to a single line, you can however replace/concat lore within a line.");
					yield null;
				}
				yield CollectionUtils.array(String[].class);
			}
			default -> null;
		};
//  TODO: see if this method is required, should only limit ability to use event-item which isn't necessary and just falsely limits Skript
//		if (ChangerUtils.acceptsChange(item, ChangeMode.SET, ItemStack.class, ItemType.class)) {
//			return CollectionUtils.array(acceptsMany ? String[].class : String.class);
//		}
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Object item = this.item.getSingle(event);
		if (!validateItem(item))
			return; // Validates to ensure it is a valid item, has item meta,
		ItemStack modifiedItem = ItemUtils.asItemStack(item);
		assert modifiedItem != null; // validateItem has already run a check against this
		ItemMeta itemMeta = modifiedItem.getItemMeta();
//		noinspection deprecation
		List<String> modifiedLore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
		assert modifiedLore != null; // lore can never be null here, if it's unset we create an empty list

		if (this.lineNumber == null) {
			switch (mode) {
				case SET, DELETE -> modifiedLore = (delta != null) ? List.of((String[]) delta) : null;
				case ADD -> {
					assert delta != null;
					modifiedLore.addAll(List.of((String[]) delta));
				}
				case REMOVE, REMOVE_ALL -> {
					boolean isAll = mode == ChangeMode.REMOVE_ALL;
					if (isAll) {
						//noinspection DataFlowIssue
						modifiedLore.removeAll(List.of((String[]) delta));
					} else {
						//noinspection DataFlowIssue
						for (String string : (String[]) delta)
							modifiedLore.remove(string);
					}
				}
			}
		} else {
			int loreIndex = this.lineNumber.getOptionalSingle(event).orElse(0).intValue() -1;
			if (loreIndex < 0)
				return; // Cannot change anything in lore if it's negative, therefor we return
			switch (mode) {
				case SET -> {
					for (int line = modifiedLore.size()-1; line < loreIndex; line++) {
						modifiedLore.add("");
					}
					//noinspection DataFlowIssue
					modifiedLore.set(loreIndex, (String) delta[0]);
				}
				case DELETE -> {
					if (loreIndex > modifiedLore.size() || !itemMeta.hasLore())
						return; // Cannot change anything in lore, therefor we return
					modifiedLore.remove(loreIndex);
				}
			}
		}

		if (modifiedLore != null && !modifiedLore.isEmpty()) {
			if (IS_RUNNING_1_21_5) {
				// The maximum amount of lore an item can have is 256
				// this change was made in 1.21.5, was unable to find the source for older versions
				// source: https://minecraft.wiki/w/Data_component_format#lore
				modifiedLore = modifiedLore.stream().limit(256).toList();
			} else {
				modifiedLore = modifiedLore.stream().limit(99).toList();
			}
		}
		//noinspection deprecation
		itemMeta.setLore(modifiedLore);
		if (item instanceof ItemType itemType) {
			itemType.setItemMeta(itemMeta);
		} else if (item instanceof ItemStack itemStack) {
			itemStack.setItemMeta(itemMeta);
		}
	}

	private boolean validateItem(Object item) {
		ItemStack itemStack = ItemUtils.asItemStack(item);
		return itemStack != null && itemStack.hasItemMeta();
	}

	@Override
	public boolean isSingle() {
		return lineNumber != null;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder syntaxBuilder = new SyntaxStringBuilder(event, debug);
		if (lineNumber != null)
			return syntaxBuilder.append("line", lineNumber, "of the lore of", item).toString();
		return syntaxBuilder.append("the lore of", item).toString();
	}

}
