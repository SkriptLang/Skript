package org.skriptlang.skript.bukkit.item.elements;

import java.util.ArrayList;
import java.util.List;

import ch.njol.skript.doc.Example;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.SyntaxStringBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.skriptlang.skript.bukkit.text.TextComponentParser;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Lore")
@Description("Returns the lore of an item.")
@Example("set the 1st line of the item's lore to \"&lt;orange&gt;Excalibur 2.0\"")
@Since("2.1")
public class ExprLore extends SimpleExpression<String> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprLore.class, String.class)
			.supplier(ExprLore::new)
			.priority(PropertyExpression.DEFAULT_PRIORITY)
			.addPatterns("[the] lore of %itemtype%",
				"%itemtype%'[s] lore",
				"[the] line %number% of [the] lore of %itemtype%",
				"[the] line %number% of %itemtype%'[s] lore",
				"[the] %number%(st|nd|rd|th) line of [the] lore of %itemtype%",
				"[the] %number%(st|nd|rd|th) line of %itemtype%'[s] lore")
			.build());
	}

	private @Nullable Expression<Number> line;
	private Expression<ItemType> item;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		line = exprs.length > 1 ? (Expression<Number>) exprs[0] : null;
		item = (Expression<ItemType>) exprs[exprs.length - 1];
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event event) {
		ItemType itemType = item.getSingle(event);
		if (itemType == null) {
			return null;
		}
		ItemMeta itemMeta = itemType.getItemMeta();
		if (!itemMeta.hasLore()) {
			return new String[0];
		}
		List<String> lore = itemMeta.getLore();
		assert lore != null; // NotNull by hasLore check
		if (line == null) {
			return lore.toArray(new String[0]);
		}
		int line = this.line.getOptionalSingle(event).orElse(0).intValue() - 1;
		if (line < 0 || line >= lore.size()) {
			return new String[0];
		}
		return new String[]{lore.get(line)};
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, SET -> CollectionUtils.array(line == null ? Component[].class : Component.class);
			case REMOVE, DELETE, REMOVE_ALL -> CollectionUtils.array(Component.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		ItemType itemType = item.getSingle(event);
		if (itemType == null) {
			return;
		}

		ItemMeta itemMeta = itemType.getItemMeta();
		List<Component> lore;
		if (itemMeta.hasLore()) {
			lore = itemMeta.lore();
		} else {
			lore = new ArrayList<>();
		}
		assert lore != null; // NotNull by hasLore check

		// italic is true by default, change for compatibility
		if (delta != null && TextComponentParser.instance().colorsCauseReset()) {
			for (int i = 0; i < delta.length; i++) {
				if (delta[i] instanceof Component component) {
					delta[i] = component.decorationIfAbsent(TextDecoration.ITALIC, State.FALSE);
				}
			}
		}

		if (line == null) {
			switch (mode) {
				case ADD -> {
					assert delta != null;
					//noinspection unchecked, rawtypes
					lore.addAll((List<Component>) (List) List.of(delta));
				}
				case SET -> {
					assert delta != null;
					//noinspection unchecked, rawtypes
					lore = (List<Component>) (List) List.of(delta);
				}
				case REMOVE, REMOVE_ALL -> {
					assert delta != null;
					if (mode == ChangeMode.REMOVE_ALL) {
						lore.removeIf(component -> component.equals(delta[0]));
					} else {
						lore.remove((Component) delta[0]);
					}
				}
				case DELETE -> lore = null;
				default -> {
					assert false;
					return;
				}
			}
		} else {
			int line = this.line.getOptionalSingle(event).orElse(0).intValue() - 1;
			if (line < 0) {
				return;
			}
			while (lore.size() <= line) {
				lore.add(Component.empty());
			}
			switch (mode) {
				case ADD -> {
					assert delta != null;
					lore.set(line, lore.get(line).append((Component) delta[0]));
				}
				case SET -> {
					assert delta != null;
					lore.set(line, (Component) delta[0]);
				}
				case REMOVE, REMOVE_ALL -> {
					assert delta != null;
					if (lore.get(line).equals(delta[0])) {
						lore.remove(line);
					}
				}
				case DELETE -> lore.remove(line);
				default -> {
					assert false;
					return;
				}
			}
		}

		itemMeta.lore(lore);
		itemType.setItemMeta(itemMeta);
	}

	@Override
	public boolean isSingle() {
		return line != null;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (line != null) {
			builder.append("the line", line, "of");
		}
		builder.append("the lore of", item);
		return builder.toString();
	}

}
