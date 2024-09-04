package ch.njol.skript.lang;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Used to describe the intention of a {@link TriggerItem}.
 * As of now, it is only used to tell whether the item halts the execution or not and print the appropriate warnings.
 *
 * @see TriggerItem#executionIntent()
 */
public sealed interface ExecutionIntent extends Comparable<ExecutionIntent>
		permits ExecutionIntent.StopTrigger, ExecutionIntent.StopSections {

	/**
	 * Creates a new stop trigger intent.
	 *
	 * @return a new stop trigger intent.
	 */
	@Contract(value = " -> new", pure = true)
	static StopTrigger stopTrigger() {
		return new StopTrigger();
	}

	/**
	 * Creates a new stop sections intent.
	 *
	 * @param levels the number of levels to stop.
	 * @return a new stop sections intent.
	 * @throws IllegalArgumentException if the depth is less than 1.
	 */
	@Contract(value = "_ -> new", pure = true)
	static StopSections stopSections(int levels) {
		Preconditions.checkArgument(levels > 0, "Depth must be at least 1");
		return new StopSections(levels);
	}

	/**
	 * Creates a new stop sections intent with a depth of 1.
	 *
	 * @return a new stop sections intent.
	 */
	@Contract(value = " -> new", pure = true)
	static StopSections stopSection() {
		return new StopSections(1);
	}

	/**
	 * Uses the current ExecutionIntent.
	 *
	 * @return a new ExecutionIntent, or null if it's exhausted.
	 */
	@Nullable ExecutionIntent use();

	/**
	 * Represents a stop trigger intent. This intent stops the execution of the trigger.
	 */
	final class StopTrigger implements ExecutionIntent {

		private StopTrigger() {}

		@Override
		public StopTrigger use() {
			return new StopTrigger();
		}

		@Override
		@SuppressWarnings("ComparatorMethodParameterNotUsed")
		public int compareTo(@NotNull ExecutionIntent other) {
			return other instanceof StopTrigger ? 0 : 1;
		}

		@Override
		public String toString() {
			return "StopTrigger";
		}

	}

	/**
	 * Represents a stop sections intent.
	 * This intent stops the execution of the current section and the specified number of levels.
	 */
	final class StopSections implements ExecutionIntent {

		private final int levels;

		private StopSections(int levels) {
			this.levels = levels;
		}

		public @Nullable ExecutionIntent.StopSections use() {
			return levels > 1 ? new StopSections(levels - 1) : null;
		}

		@Override
		public int compareTo(@NotNull ExecutionIntent other) {
			if (!(other instanceof StopSections))
				return other.compareTo(this) * -1;
			int levels = ((StopSections) other).levels;
			return Integer.compare(this.levels, levels);
		}

		/**
		 * Returns the number of levels to stop.
		 *
		 * @return the number of levels to stop.
		 */
		public int levels() {
			return levels;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj == null || obj.getClass() != this.getClass())
				return false;
			return this.levels == ((StopSections) obj).levels;
		}

		@Override
		public int hashCode() {
			return Objects.hash(levels);
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("levels", levels)
					.toString();
		}

	}

}
