package ch.njol.skript.config.validate;

import ch.njol.skript.config.Node;

/**
 * @author Peter Güttinger
 */
public interface NodeValidator {
	
	public boolean validate(Node node);
	
}
