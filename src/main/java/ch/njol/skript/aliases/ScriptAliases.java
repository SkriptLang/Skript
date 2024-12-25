package ch.njol.skript.aliases;


import org.skriptlang.skript.lang.script.ScriptData;

/**
 * Per-script aliases provider and parser container.
 */
public class ScriptAliases implements ScriptData {
	
	/**
	 * Aliases provider.
	 */
	public final AliasesProvider provider;
	
	/**
	 * Aliases parser linked to our provider.
	 */
	public final AliasesParser parser;
	
	ScriptAliases(AliasesProvider provider, AliasesParser parser) {
		this.provider = provider;
		this.parser = parser;
	}

}
