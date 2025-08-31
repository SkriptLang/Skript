package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.bukkit.Nameable;
import org.bukkit.command.CommandSender;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyHandler.ExpressionPropertyHandler;

public class NameableClassInfo extends ClassInfo<Nameable> {
	public NameableClassInfo() {
		super(Nameable.class, "nameable");
		this.user("nameables?")
			.name("Nameable")
			.description(
				"A variety of Bukkit types that can have names, such as entities and some blocks."
			).since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(Nameable.class))
			.property(Property.NAME, ExpressionPropertyHandler.of(nameable -> {
				if (nameable instanceof CommandSender sender) { // prioritize CommandSender names over Nameable names for "name of"
					return sender.getName();
				}
				return nameable.getCustomName();
			}, String.class))
			.property(Property.DISPLAY_NAME, ExpressionPropertyHandler.of(nameable -> nameable.getCustomName(), String.class));
	}


}
