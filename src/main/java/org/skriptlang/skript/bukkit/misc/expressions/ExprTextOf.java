package org.skriptlang.skript.bukkit.misc.expressions;

import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.ServerPlatform;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.chat.BungeeConverter;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;

@Name("Text Of")
@Description({
	"Returns or changes the <a href='classes.html#string'>text/string</a> of <a href='classes.html#display'>displays</a>.",
	"Note that currently you can only use Skript chat codes when running Paper."
})
@Examples("set text of the last spawned text display to \"example\"")
@Since("INSERT VERSION")
public class ExprTextOf extends SimplePropertyExpression<Object, String> {

	private static final boolean IS_RUNNING_PAPER = Skript.getServerPlatform() == ServerPlatform.BUKKIT_PAPER;
	private static BungeeComponentSerializer serializer;

	static {
		String types = "";
		if (Skript.isRunningMinecraft(1, 19, 4)) {
			serializer = BungeeComponentSerializer.get();
			types += "displays";
		}
		// This is because this expression is setup to support future types.
		// Remove this if non-versioning.
		if (!types.isEmpty())
			register(ExprTextOf.class, String.class, "(banana|string|text)[s]", types);
	}

	@Override
	public @Nullable String convert(Object object) {
		if (object instanceof TextDisplay)
			return ((TextDisplay) object).getText();
		return null;
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		switch (mode) {
			case ADD:
			case DELETE:
			case REMOVE:
			case REMOVE_ALL:
				break;
			case RESET:
				return CollectionUtils.array();
			case SET:
				return CollectionUtils.array(String.class);
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		//noinspection ConstantConditions
		String value = mode == ChangeMode.RESET ? null : (String) delta[0];
		for (Object object : getExpr().getArray(event)) {
			if (!(object instanceof TextDisplay))
				continue;
			if (IS_RUNNING_PAPER && serializer != null && value != null) {
				BaseComponent[] components = BungeeConverter.convert(ChatMessages.parseToArray(value));
				((TextDisplay) object).text(serializer.deserialize(components));
			} else {
				((TextDisplay) object).setText(value);
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
