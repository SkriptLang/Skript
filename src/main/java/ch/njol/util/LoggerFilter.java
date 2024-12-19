package ch.njol.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.jetbrains.annotations.Nullable;

public final class LoggerFilter implements Filter, Closeable {
	private final Logger l;
	private final Collection<Filter> filters = new ArrayList<>(5);
	@Nullable
	private final Filter oldFilter;
	
	public LoggerFilter(final Logger l) {
		this.l = l;
		oldFilter = l.getFilter();
		l.setFilter(this);
	}
	
	@Override
	public boolean isLoggable(final @Nullable LogRecord record) {
		if (oldFilter != null && !oldFilter.isLoggable(record))
			return false;
		for (final Filter f : filters)
			if (!f.isLoggable(record))
				return false;
		return true;
	}
	
	public final void addFilter(final Filter f) {
		filters.add(f);
	}
	
	public final boolean removeFilter(final Filter f) {
		return filters.remove(f);
	}
	
	@Override
	public void close() {
		l.setFilter(oldFilter);
	}
}
