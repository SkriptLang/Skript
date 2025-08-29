package org.skriptlang.skript.lang.properties;

public record PropertyInfo<Handler>(Property<Handler> property, Handler handler) {
}
