package ch.njol.skript.config;

/**
 * A line of a config that could not be parsed.
 *
 * @author Peter Güttinger
 */
public class InvalidNode extends VoidNode {

//	public InvalidNode(final SectionNode parent, final ConfigReader r) {
//		super(parent, r);
//		config.errors++;
//	}

	public InvalidNode(final String value, final String comment, final SectionNode parent, final int lineNum) {
		super(value, comment, parent, lineNum);
		config.errors++;
	}

}
