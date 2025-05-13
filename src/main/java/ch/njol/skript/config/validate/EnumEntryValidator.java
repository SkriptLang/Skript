package ch.njol.skript.config.validate;

import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.util.Setter;

/**
 * @author Peter Güttinger
 */
public class EnumEntryValidator<E extends Enum<E>> extends EntryValidator {
	
	private final Class<E> enumType;
	private final Setter<E> setter;
	@Nullable
	private String allowedValues = null;
	
	public EnumEntryValidator(final Class<E> enumType, final Setter<E> setter) {
		assert enumType != null;
		this.enumType = enumType;
		this.setter = setter;
		if (enumType.getEnumConstants().length <= 12) {
			final StringBuilder b = new StringBuilder(enumType.getEnumConstants()[0].name());
			for (final E e : enumType.getEnumConstants()) {
				if (b.length() != 0)
					b.append(", ");
				b.append(e.name());
			}
			allowedValues = "" + b.toString();
		}
	}
	
	public EnumEntryValidator(final Class<E> enumType, final Setter<E> setter, final String allowedValues) {
		assert enumType != null;
		this.enumType = enumType;
		this.setter = setter;
		this.allowedValues = allowedValues;
	}
	
	@Override
	public boolean validate(final Node node) {
		if (!super.validate(node))
			return false;
		final EntryNode n = (EntryNode) node;
		try {
			final E e = Enum.valueOf(enumType, n.getValue().toUpperCase(Locale.ENGLISH).replace(' ', '_'));
			assert e != null;
//			if (setter != null)
			setter.set(e);
		} catch (final IllegalArgumentException e) {
			Skript.error("'" + n.getValue() + "' is not a valid value for '" + n.getKey() + "'" + (allowedValues == null ? "" : ". Allowed values are: " + allowedValues));
			return false;
		}
		return true;
	}
	
}
