package org.skriptlang.skript.bukkit.item.misc;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.text.TextComponentParser;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Name("Lore")
@Description("Returns the lore of an item.")
@Example("set the 1st line of the item's lore to \"<orange>Excalibur 2.0\"")
@Since("2.1")
public class ExprLore extends SimpleExpression<Component> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprLore.class, Component.class)
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
	protected Component[] get(Event event) {
		ItemType itemType = item.getSingle(event);
		if (itemType == null) {
			return null;
		}
		ItemMeta itemMeta = itemType.getItemMeta();
		if (!itemMeta.hasLore()) {
			return new Component[0];
		}
		List<Component> lore = itemMeta.lore();
		assert lore != null; // NotNull by hasLore check
		if (line == null) {
			return lore.toArray(new Component[0]);
		}
		int line = this.line.getOptionalSingle(event).orElse(0).intValue() - 1;
		if (line < 0 || line >= lore.size()) {
			return new Component[0];
		}
		return new Component[]{lore.get(line)};
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		// TODO we need access to string inputs for newline splitting
		// However, this prevents parse time component parsing which is not ideal
		// Need to find a way to change delta...
		return switch (mode) {
			case ADD, SET -> line == null ? CollectionUtils.array(Component[].class, String[].class) : CollectionUtils.array(Component.class);
			case REMOVE, DELETE, REMOVE_ALL -> CollectionUtils.array(line == null ? Component.class : String.class);
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

		if (line == null) {
			switch (mode) {
				case ADD -> {
					assert delta != null;
					lore.addAll(parseLore(delta));
				}
				case SET -> {
					assert delta != null;
					lore = parseLore(delta);
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
					TextReplacementConfig.Builder builder = TextReplacementConfig.builder();
					if (mode == ChangeMode.REMOVE) {
						builder.once();
					}
					int flags = SkriptConfig.caseSensitive.value() ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
					builder.match(Pattern.compile(Pattern.quote((String) delta[0]), flags));
					builder.replacement(Component.empty());
					lore.set(line, lore.get(line).replaceText(builder.build()));
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
	public Class<? extends Component> getReturnType() {
		return Component.class;
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

	static Expression<?> convertStrings(Expression<?> expression) {
		boolean isString = String.class.isAssignableFrom(expression.getReturnType());
		if (expression instanceof ExpressionList<?> list) {
			boolean hasComponent = false;
			Expression<?>[] expressions = list.getExpressions();
			for (int i = 0; i < expressions.length; i++) {
				expressions[i] = convertStrings(expressions[i]);
				hasComponent |= Component.class.isAssignableFrom(expressions[i].getReturnType());
			}
			if (isString && hasComponent) { // return type has changed, rebuild expression list
				return new ExpressionList<>(expressions, Object.class, list.getAnd());
			}
		} else if (isString && expression instanceof Literal<?> string) {
			return string.getConvertedExpression(Component.class);
		}
		return expression;
	}

	static List<Component> parseLore(Object[] lore) {
		List<Component> loreList = new ArrayList<>();
		// process runtime resolved strings
		for (Object line : lore) {
			if (line instanceof Component component) {
				loreList.add(component);
			} else {
				for (String textLine : ((String) line).split("\n")) {
					loreList.add(TextComponentParser.instance().parse(textLine));
				}
			}
		}
		return loreList;
	}

}
