package org.skriptlang.skript.lang.properties;

public record PropertyInfo<Handler extends Property.PropertyHandler<?>>(Property<Handler> property, Handler handler) {
}
