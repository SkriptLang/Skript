package ch.njol.skript.log;

import java.util.Iterator;
import java.util.LinkedList;

import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
public class HandlerList implements Iterable<LogHandler> {
	
	private final LinkedList<LogHandler> list = new LinkedList<>();
	
	public void add(LogHandler h) {
		list.addFirst(h);
	}
	
	@Nullable
	public LogHandler remove() {
		return list.pop();
	}
	
	@SuppressWarnings("null")
	@Override
	public Iterator<LogHandler> iterator() {
		return list.iterator();
	}
	
	public boolean contains(LogHandler h) {
		return list.contains(h);
	}
	
}
