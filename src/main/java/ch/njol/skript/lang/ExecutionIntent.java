package ch.njol.skript.lang;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used to describe the intention of a {@link TriggerItem}.
 * Currently only used to tell whether the item halts the execution or not and print the appropriate warnings.
 * 
 * @see TriggerItem#executionIntent() 
 */
public sealed interface ExecutionIntent extends Comparable<ExecutionIntent>
	permits ExecutionIntent.StopTrigger, ExecutionIntent.StopSections {

	static StopTrigger stopTrigger() {
		return new StopTrigger();
	}

	static StopSections stopSections(int levels) {
		Preconditions.checkArgument(levels > 0, "Depth must be at least 1");
		return new StopSections(levels);
	}

	static StopSections stopSection() {
		return new StopSections(1); 
	}

	@Nullable ExecutionIntent use();

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

	record StopSections(int levels) implements ExecutionIntent {

		public @Nullable ExecutionIntent.StopSections use() {
			return levels > 1 ? new StopSections(levels - 1) : null;
		}

		@Override
		public int compareTo(@NotNull ExecutionIntent other) {
			if (other instanceof StopTrigger)
				return -1;
			int levels = ((StopSections) other).levels;
			return Integer.compare(this.levels, levels);
		}

	}

}
