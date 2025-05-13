package ch.njol.util;

import org.jetbrains.annotations.Nullable;

public interface Callback<R, A> {
	
	@Nullable
	public R run(A arg);
	
}
