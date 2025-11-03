package org.skriptlang.skript.bukkit.misc.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Text Of")
@Description("""
	Returns or changes the <a href='#string'>text/string</a> of <a href='#display'>displays</a>.
	Note that currently you can only use Skript chat codes when running Paper.
	""")
@Example("set text of the last spawned text display to \"example\"")
@Since("2.10")
public class ExprTextOf extends SimplePropertyExpression<Object, String> {

	private static final BungeeComponentSerializer SERIALIZER = BungeeComponentSerializer.get();

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprTextOf.class,
				String.class,
				"text[s]",
				"displays",
				false
			).supplier(ExprTextOf::new)
				.build()
		);
	}

	@Override
	public @Nullable String convert(Object object) {
		if (object instanceof TextDisplay textDisplay)
			return textDisplay.getText();
		return null;
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case RESET -> CollectionUtils.array();
			case SET -> CollectionUtils.array(String.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		String value = delta == null ? null : (String) delta[0];
		for (Object object : getExpr().getArray(event)) {
			if (!(object instanceof TextDisplay textDisplay))
				continue;
			if (SERIALIZER != null && value != null) {
				BaseComponent[] components = BungeeConverter.convert(ChatMessages.parseToArray(value));
				textDisplay.text(SERIALIZER.deserialize(components));
			} else {
				textDisplay.setText(value);
			}
		}
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	protected String getPropertyName() {
		return "text";
	}

}
