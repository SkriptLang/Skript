package ch.njol.skript.expressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.Changer.ChangerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;

/**
 * TODO make a 'line %number% of %text%' expression and figure out how to deal with signs (4 lines, delete = empty, etc...)
 *
 * @author joeuguce99
 */
@Name("Lore")
@Description("An item's lore.")
@Examples("set the 1st line of the item's lore to \"&lt;orange&gt;Excalibur 2.0\"")
@Since("2.1")
public class ExprLore extends SimpleExpression<String> {

	static {
			Skript.registerExpression(ExprLore.class, String.class, ExpressionType.PROPERTY,
					"[the] lore of %itemstack/itemtype%", "%itemstack/itemtype%'[s] lore",
					"[the] line %number% of [the] lore of %itemstack/itemtype%",
					"[the] line %number% of %itemstack/itemtype%'[s] lore",
					"[the] %number%(st|nd|rd|th) line of [the] lore of %itemstack/itemtype%",
					"[the] %number%(st|nd|rd|th) line of %itemstack/itemtype%'[s] lore");
	}

	@Nullable
	private Expression<Number> lineNumber;

	@SuppressWarnings("null")
	private Expression<?> item;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		lineNumber = exprs.length > 1 ? (Expression<Number>) exprs[0] : null;
		item = exprs[exprs.length - 1];
		return true;
	}

	@Override
	@Nullable
	protected String[] get(final Event e) {
		final Object i = item.getSingle(e);
		final Number n = lineNumber != null ? lineNumber.getSingle(e) : null;
		if (n == null && lineNumber != null)
			return null;
		if (i == null || i instanceof ItemStack && ((ItemStack) i).getType() == Material.AIR)
			return new String[0];
		final ItemMeta meta = i instanceof ItemStack ? ((ItemStack) i).getItemMeta() : (ItemMeta) ((ItemType) i).getItemMeta();
		if (meta == null || !meta.hasLore())
			return new String[0];
		final List<String> lore = meta.getLore();
		assert lore != null; // hasLore() called before
		if (n == null)
			return lore.toArray(new String[0]);
		final int l = n.intValue() - 1;
		if (l < 0 || l >= lore.size())
			return new String[0];
		return new String[]{lore.get(l)};
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		boolean acceptsMany = lineNumber == null;
		switch (mode) {
			case REMOVE:
			case REMOVE_ALL:
			case DELETE:
				acceptsMany = false;
			case SET:
			case ADD:
				if (ChangerUtils.acceptsChange(item, ChangeMode.SET, ItemStack.class, ItemType.class)) {
					return CollectionUtils.array(acceptsMany ? String[].class : String.class);
				}
				return null;
			case RESET:
			default:
				return null;
		}
	}

	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		Object i = item.getSingle(e);

		String[] stringDelta = delta == null ? null : Arrays.copyOf(delta, delta.length, String[].class);

		// air is just nothing, it can't have a lore
		if (i == null || i instanceof ItemStack && ((ItemStack) i).getType() == Material.AIR)
			return;

		ItemMeta meta = i instanceof ItemStack ? ((ItemStack) i).getItemMeta() : (ItemMeta) ((ItemType) i).getItemMeta();
		if (meta == null)
			meta = Bukkit.getItemFactory().getItemMeta(Material.STONE);

		Number lineNumber = this.lineNumber != null ? this.lineNumber.getSingle(e) : null;
		List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

		if (lineNumber == null) {
			// if the condition below is true, the pattern with the line %number% expression was used,
			// but the line number turned out to be null at runtime, meaning we should ignore it
			if (this.lineNumber != null) {
				return;
			}

			switch (mode) {
				case SET:
					assert stringDelta != null;
					List<String> newLore = new ArrayList<>();
					for (String line : stringDelta) {
						if (line.contains("\n")) {
							Collections.addAll(newLore, line.split("\n"));
							continue;
						}
						newLore.add(line);
					}
					lore = newLore;
					break;
				case ADD:
					assert stringDelta != null;
					List<String> addLore = new ArrayList<>();
					for (String line : stringDelta) {
						if (line.contains("\n")) {
							Collections.addAll(addLore, line.split("\n"));
							continue;
						}
						addLore.add(line);
					}
					lore.addAll(addLore);
					break;
				case DELETE:
					lore = null;
					break;
				case REMOVE:
				case REMOVE_ALL:
					assert stringDelta != null;
					lore = Arrays.asList(handleRemove(
							StringUtils.join(lore, "\n"), stringDelta[0], mode == ChangeMode.REMOVE_ALL).split("\n"));
					break;
				case RESET:
					assert false;
					return;
			}
		} else {
			// Note: line number is changed from one-indexed to zero-indexed here
			int lineNum = Math2.fit(0, lineNumber.intValue() - 1, 99); // TODO figure out the actual maximum

			// Fill in the empty lines above the line being set with empty strings (avoids index out of bounds)
			while (lore.size() <= lineNum)
				lore.add("");
			switch (mode) {
				case SET:
					assert stringDelta != null;
					lore.set(lineNum, stringDelta[0]);
					break;
				case ADD:
					assert stringDelta != null;
					lore.set(lineNum, lore.get(lineNum) + stringDelta[0]);
					break;
				case DELETE:
					lore.remove(lineNum);
					break;
				case REMOVE:
				case REMOVE_ALL:
					assert stringDelta != null;
					lore.set(lineNum, handleRemove(lore.get(lineNum), stringDelta[0], mode == ChangeMode.REMOVE_ALL));
					break;
				case RESET:
					assert false;
					return;
			}
		}

		meta.setLore(lore == null || lore.size() == 0 ? null : lore);
		if (i instanceof ItemStack) {
			((ItemStack) i).setItemMeta(meta);
		} else {
			((ItemType) i).setItemMeta(meta);
		}

		if (ChangerUtils.acceptsChange(item, ChangeMode.SET, i.getClass())) {
			Object[] itemDelta = i instanceof ItemStack ? new ItemStack[]{(ItemStack) i} : new ItemType[]{(ItemType) i};
			item.change(e, itemDelta, ChangeMode.SET);
		} else {
			Object[] itemDelta = i instanceof ItemStack ? new ItemType[]{new ItemType((ItemStack) i)} :
					new ItemStack[]{((ItemType) i).getRandom()};
			item.change(e, itemDelta, ChangeMode.SET);
		}
	}

	private String handleRemove(String input, String toRemove, boolean all) {
		if (SkriptConfig.caseSensitive.value()) {
			if (all) {
				return input.replace(toRemove, "");
			} else {
				// .replaceFirst requires the regex to be quoted, .replace does it internally
				return input.replaceFirst(Pattern.quote(toRemove), "");
			}
		} else {
			final Matcher m = Pattern.compile(Pattern.quote(toRemove),
					Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(input);
			return all ? m.replaceAll("") : m.replaceFirst("");
		}
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
	public String toString(final @Nullable Event e, final boolean debug) {
		return (lineNumber != null ? "the line " + lineNumber.toString(e, debug) + " of " : "") + "the lore of " + item.toString(e, debug);
	}
}
