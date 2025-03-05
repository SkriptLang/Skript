package org.skriptlang.skript.lang.entry;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryValidator.EntryValidatorBuilder;
import org.skriptlang.skript.lang.structure.Structure;

/**
 * An entry data for handling a {@link SectionNode} as the root node of another {@link EntryValidator}.
 * This enables nested entry data validation.
 */
public class ContainerEntryData extends EntryData<EntryContainer> {

	private final EntryValidator entryValidator;
	private @Nullable EntryContainer entryContainer;

	public ContainerEntryData(String key, boolean optional, EntryValidator entryValidator) {
		super(key, null, optional);
		this.entryValidator = entryValidator;
	}

	public ContainerEntryData(String key, boolean optional, EntryValidatorBuilder validatorBuilder) {
		super(key, null, optional);
		this.entryValidator = validatorBuilder.build();
	}

	/**
	 * Since this will and should never be the main {@link EntryValidator} when used for a {@link Structure}
	 * We need to validate it when the main {@link EntryValidator} is being validated.
	 */
	public void validate(SectionNode sectionNode) {
		EntryContainer container = entryValidator.validate(sectionNode);
		entryContainer = container;
	}

	@Override
	public @Nullable EntryContainer getValue(Node node) {
		return entryContainer;
	}

	@Override
	public boolean canCreateWith(Node node) {
		if (!(node instanceof SectionNode sectionNode))
			return false;
		String key = node.getKey();
		if (key == null)
			return false;
		key = ScriptLoader.replaceOptions(key);
		if (!getKey().equalsIgnoreCase(key))
			return false;
		validate(sectionNode);
		return true;
	}

}
