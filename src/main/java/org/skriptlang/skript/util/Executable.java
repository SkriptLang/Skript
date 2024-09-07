package org.skriptlang.skript.util;

public interface Executable<Caller, Result> {

	Result execute(Caller caller, Object... arguments);

}
