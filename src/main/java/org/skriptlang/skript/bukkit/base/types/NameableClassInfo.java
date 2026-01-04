package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.bukkit.Nameable;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@ApiStatus.Internal
public class NameableClassInfo extends ClassInfo<Nameable> {

	public NameableClassInfo() {
		super(Nameable.class, "nameable");
		this.user("nameables?")
			.name("Nameable")
			.description(
				"A variety of Bukkit types that can have names, such as entities and some blocks."
			).since("2.13")
			.defaultExpression(new EventValueExpression<>(Nameable.class))
			.after("entity", "commandsender", "block", "player")
			.property(Property.NAME,
				"The name of the nameable, if it has one, as text. Use 'display name' if you need a changeable name.",
				Skript.instance(),
				ExpressionPropertyHandler.of(nameable -> {
				if (nameable instanceof CommandSender sender) { // prioritize CommandSender names over Nameable names for "name of"
					return sender.getName();
				}
				return nameable.getCustomName();
			}, String.class))
			.property(Property.DISPLAY_NAME,
				"The custom name of the nameable, if it has one, as text. Can be set or reset.",
				Skript.instance(),
				new NameableNameHandler());
	}

	private static class NameableNameHandler implements ExpressionPropertyHandler<Nameable, String> {
		//<editor-fold desc="name property for nameables" defaultstate="collapsed">
		@Override
		public String convert(Nameable nameable) {
			return nameable.getCustomName();
		}

		@Override
		public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
				return new Class[] {String.class};
			return null;
		}

		@Override
		public void change(Nameable propertyHolder, Object @Nullable [] delta, ChangeMode mode) {
			assert mode == ChangeMode.SET || mode == ChangeMode.RESET;
			if (mode == ChangeMode.SET) {
				assert delta != null;
				if (delta.length == 1) {
					propertyHolder.setCustomName((String) delta[0]);
				}
			} else {
				propertyHolder.setCustomName(null);
			}
		}

		@Override
		public @NotNull Class<String> returnType() {
			return String.class;
		}
		//</editor-fold>
	}

}
