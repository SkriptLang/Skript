package ch.njol.util;

import org.jetbrains.annotations.Nullable;

public interface NullableChecker<T> extends Checker<T> {
	
	@Override
	public boolean check(@Nullable T o);
	
	public static final NullableChecker<Object> nullChecker = new NullableChecker<Object>() {
		@Override
		public boolean check(final @Nullable Object o) {
			return o != null;
		}
	};
	
}
