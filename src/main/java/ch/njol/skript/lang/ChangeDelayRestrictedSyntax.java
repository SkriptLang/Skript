package ch.njol.skript.lang;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.effects.EffChange;
import ch.njol.util.coll.CollectionUtils;
import org.jetbrains.annotations.Nullable;

/**
 * A syntax element that restricts the delays at which it can be changed.
 */
public interface ChangeDelayRestrictedSyntax {

	/**
	 * The supported delays at which changes may be made.
	 * <p>
	 * This method is called after {@link Changer#acceptChange(Changer.ChangeMode)} in {@link EffChange},
	 * but before {@link Changer#change(Object[], Object[], Changer.ChangeMode)}.
	 * An error is printed if the current delay is unsupported.
	 * </p>
	 *
	 * @return The supported delays which allow changes.
	 * @see CollectionUtils#array(Object...)
	 */
	default Delay[] supportedChangeDelays() {
		return new Delay[] { Delay.MAYBE, Delay.NO };
	}

	/**
	 * The known delay state in a section.
	 */
	enum Delay {

		/**
		 * There is definitely a delay.
		 */
		YES,

		/**
		 * There may be a delay.
		 */
		MAYBE,

		/**
		 * There is definitely no delay.
		 */
		NO


	}

	/**
	 * @return A string representation of the syntax used in the change delay message.
	 */
	default @Nullable String toChangeDelayString() {
		return null;
	}

}
