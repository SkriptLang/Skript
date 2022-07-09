/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.lang.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.log.HandlerList;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

public class ParserInstance {
	
	private static final ThreadLocal<ParserInstance> parserInstances = ThreadLocal.withInitial(ParserInstance::new);
	
	/**
	 * @return The {@link ParserInstance} for this thread.
	 */
	// TODO maybe make a one-thread cache (e.g. Pair<Thread, ParserInstance>) if it's better for performance (test)
	public static ParserInstance get() {
		return parserInstances.get();
	}
	
	// Logging
	private final HandlerList handlers = new HandlerList();
	@Nullable
	private Node node;
	
	// Script
	@Nullable
	private Config currentScript;
	private final HashMap<String, String> currentOptions = new HashMap<>();
	
	// Event
	@Nullable
	private String currentEventName;
	@Nullable
	private Class<? extends Event>[] currentEvents;
	@Nullable
	private SkriptEvent currentSkriptEvent;
	
	// Sections
	private List<TriggerSection> currentSections = new ArrayList<>();
	private Kleenean hasDelayBefore = Kleenean.FALSE;
	private String indentation = "";
	
	// Getters
	/**
	 * You probably shouldn't use this method.
	 *
	 * @return The {@link HandlerList} containing all active log handlers.
	 */
	public HandlerList getHandlers() {
		return handlers;
	}
	
	@Nullable
	public Node getNode() {
		return node;
	}
	
	@Nullable
	public Config getCurrentScript() {
		return currentScript;
	}
	
	public HashMap<String, String> getCurrentOptions() {
		return currentOptions;
	}
	
	@Nullable
	public String getCurrentEventName() {
		return currentEventName;
	}
	
	@Nullable
	public Class<? extends Event>[] getCurrentEvents() {
		return currentEvents;
	}
	
	@Nullable
	public SkriptEvent getCurrentSkriptEvent() {
		return currentSkriptEvent;
	}
	
	public List<TriggerSection> getCurrentSections() {
		return currentSections;
	}

	/**
	 * @return whether {@link #getCurrentSections()} contains
	 * an section instance of the given class (or subclass).
	 */
	public boolean isCurrentSection(Class<? extends TriggerSection> sectionClass) {
		for (TriggerSection triggerSection : currentSections) {
			if (sectionClass.isInstance(triggerSection))
				return true;
		}
		return false;
	}

	@SafeVarargs
	public final boolean isCurrentSection(Class<? extends TriggerSection>... sectionClasses) {
		for (Class<? extends TriggerSection> sectionClass : sectionClasses) {
			if (isCurrentSection(sectionClass))
				return true;
		}
		return false;
	}

	/**
	 * @return the outermost section which is an instance of the given class.
	 * Returns {@code null} if {@link #isCurrentSection(Class)} returns {@code false}.
	 * @see #getCurrentSections()
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends TriggerSection> T getCurrentSection(Class<T> sectionClass) {
		for (int i = currentSections.size(); i-- > 0;) {
			TriggerSection triggerSection = currentSections.get(i);
			if (sectionClass.isInstance(triggerSection))
				return (T) triggerSection;
		}
		return null;
	}

	/**
	 * @return a {@link List} of current sections that are an instance of the given class.
	 * Modifications to the returned list are not saved.
	 * @see #getCurrentSections()
	 */
	@SuppressWarnings("unchecked")
	@NotNull
	public <T extends TriggerSection> List<T> getCurrentSections(Class<T> sectionClass) {
		List<T> list = new ArrayList<>();
		for (TriggerSection triggerSection : currentSections) {
			if (sectionClass.isInstance(triggerSection))
				list.add((T) triggerSection);
		}
		return list;
	}
	
	/**
	 * @return whether this trigger has had delays before.
	 * Any syntax elements that modify event-values, should use this
	 * (or the {@link Kleenean} provided to in
	 * {@link ch.njol.skript.lang.SyntaxElement#init(Expression[], int, Kleenean, SkriptParser.ParseResult)})
	 * to make sure the event can't be modified when it has passed.
	 */
	public Kleenean getHasDelayBefore() {
		return hasDelayBefore;
	}
	
	public String getIndentation() {
		return indentation;
	}
	
	// Setters
	public void setNode(@Nullable Node node) {
		this.node = node == null || node.getParent() == null ? null : node;
	}
	
	public void setCurrentScript(@Nullable Config currentScript) {
		this.currentScript = currentScript;
		getDataInstances().forEach(data -> data.onCurrentScriptChange(currentScript));
	}
	
	public void setCurrentEventName(@Nullable String currentEventName) {
		this.currentEventName = currentEventName;
	}
	
	public void setCurrentEvents(@Nullable Class<? extends Event>[] currentEvents) {
		this.currentEvents = currentEvents;
		getDataInstances().forEach(data -> data.onCurrentEventsChange(currentEvents));
	}
	
	public void setCurrentSkriptEvent(@Nullable SkriptEvent currentSkriptEvent) {
		this.currentSkriptEvent = currentSkriptEvent;
	}
	
	public void deleteCurrentSkriptEvent() {
		this.currentSkriptEvent = null;
	}
	
	public void setCurrentSections(List<TriggerSection> currentSections) {
		this.currentSections = currentSections;
	}
	
	/**
	 * This method should be called to indicate that
	 * the trigger will (possibly) be delayed from this point on.
	 *
	 * @see ch.njol.skript.util.AsyncEffect
	 */
	public void setHasDelayBefore(Kleenean hasDelayBefore) {
		this.hasDelayBefore = hasDelayBefore;
	}
	
	public void setIndentation(String indentation) {
		this.indentation = indentation;
	}
	
	// Other
	@SafeVarargs
	public final void setCurrentEvent(String name, @Nullable Class<? extends Event>... events) {
		currentEventName = name;
		setCurrentEvents(events);
		hasDelayBefore = Kleenean.FALSE;
	}
	
	public void deleteCurrentEvent() {
		currentEventName = null;
		setCurrentEvents(null);
		hasDelayBefore = Kleenean.FALSE;
	}

	/**
	 * This method checks whether <i>at least one</i> of the current event classes
	 * is covered by the argument event class (i.e. equal to the class or a subclass of it).
	 * <br>
	 * Using this method in an event-specific syntax element requires a runtime check, for example <br>
	 * {@code if (!(e instanceof BlockBreakEvent)) return null;}
	 * <br>
	 * This check is required because there can be more than 1 event class at parse-time, but this method
	 * only checks if one of them matches the argument class.
	 *
	 * <br><br>
	 * See also {@link #isCurrentEvent(Class[])} for checking with multiple argument classes
	 */
	public boolean isCurrentEvent(@Nullable Class<? extends Event> event) {
		return CollectionUtils.containsSuperclass(currentEvents, event);
	}

	/**
	 * Same as {@link #isCurrentEvent(Class)}, but allows for plural argument input.
	 * <br>
	 * This means that this method will return whether any of the current event classes is covered
	 * by any of the argument classes.
	 * <br>
	 * Using this method in an event-specific syntax element {@link #isCurrentEvent(Class) requires a runtime check},
	 * you can use {@link CollectionUtils#isAnyInstanceOf(Object, Class[])} for this, for example: <br>
	 * {@code if (!CollectionUtils.isAnyInstanceOf(e, BlockBreakEvent.class, BlockPlaceEvent.class)) return null;}
	 *
	 * @see #isCurrentEvent(Class)
	 */
	@SafeVarargs
	public final boolean isCurrentEvent(Class<? extends Event>... events) {
		return CollectionUtils.containsAnySuperclass(currentEvents, events);
	}

	/*
	 * Addon data
	 */
	/**
	 * An abstract class for addons that want to add data bound to a ParserInstance.
	 * Extending classes may listen to the events {@link #onCurrentScriptChange(Config)}
	 * and {@link #onCurrentEventsChange(Class[])}.
	 * It is recommended you make a constructor with a {@link ParserInstance} parameter that
	 * sends that parser instance upwards in a super call, so you can use
	 * {@code ParserInstance.registerData(MyData.class, MyData::new)}
	 */
	public static abstract class Data {
		
		private final ParserInstance parserInstance;
		
		public Data(ParserInstance parserInstance) {
			this.parserInstance = parserInstance;
		}
		
		protected final ParserInstance getParser() {
			return parserInstance;
		}
		
		public void onCurrentScriptChange(@Nullable Config currentScript) { }
		
		public void onCurrentEventsChange(@Nullable Class<? extends Event>[] currentEvents) { }
		
	}
	
	private static final Map<Class<? extends Data>, Function<ParserInstance, ? extends Data>> dataRegister = new HashMap<>();
	// Should be Map<Class<? extends Data>, ? extends Data>, but that caused issues (with generics) in #getData(Class)
	private final Map<Class<? extends Data>, Data> dataMap = new HashMap<>();
	
	/**
	 * Registers a data class to all {@link ParserInstance}s.
	 *
	 * @param dataClass the data class to register.
	 * @param dataFunction an instance creator for the data class.
	 */
	public static <T extends Data> void registerData(Class<T> dataClass,
													 Function<ParserInstance, T> dataFunction) {
		dataRegister.put(dataClass, dataFunction);
	}
	
	public static boolean isRegistered(Class<? extends Data> dataClass) {
		return dataRegister.containsKey(dataClass);
	}
	
	/**
	 * @return the data object for the given class from this {@link ParserInstance},
	 * or null (after {@code false} has been asserted) if the given data class isn't registered.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Data> T getData(Class<T> dataClass) {
		if (dataMap.containsKey(dataClass)) {
			return (T) dataMap.get(dataClass);
		} else if (dataRegister.containsKey(dataClass)) {
			T data = (T) dataRegister.get(dataClass).apply(this);
			dataMap.put(dataClass, data);
			return data;
		}
		assert false;
		return null;
	}
	
	private List<? extends Data> getDataInstances() {
		// List<? extends Data> gave errors, so using this instead
		List<Data> dataList = new ArrayList<>();
		for (Class<? extends Data> dataClass : dataRegister.keySet()) {
			// This will include all registered data, even if not already initiated
			Data data = getData(dataClass);
			if (data != null)
				dataList.add(data);
		}
		return dataList;
	}
	
}
