package org.skriptlang.skript.variables.storage;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.variables.VariableStorage;
import ch.njol.skript.variables.VariablesMap;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;

import java.io.File;

/**
 * Variable storage that stores variables in heap memory and
 * discards them on close.
 * <p>
 * This implementation of storage can be used without loading it explicitly.
 */
public class InMemoryVariableStorage extends VariableStorage {

	private final VariablesMap variablesMap = new VariablesMap();

	public InMemoryVariableStorage(SkriptAddon source, String type) {
		super(source, type);
	}

	@Override
	protected boolean load(SectionNode n) {
		return true;
	}

	@Override
	protected boolean requiresFile() {
		return false;
	}

	@Override
	protected File getFile(String fileName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public @Nullable Object getVariable(String name) {
		return variablesMap.getVariable(name);
	}

	@Override
	public void setVariable(String name, @Nullable Object value) {
		variablesMap.setVariable(name, value);
	}

	@Override
	public long loadedVariables() {
		return variablesMap.size();
	}

	@Override
	public void close() {
	}

}
