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

import ch.njol.skript.ScriptLoader.ScriptInfo;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Script;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.lang.structure.Structure;
import ch.njol.skript.log.HandlerList;
import ch.njol.skript.structures.StructOptions;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
	private Script currentScript;
	private ScriptInfo scriptInfo;
	private final List<Structure> loadedStructures = new ArrayList<>();

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
	public Script getCurrentScript() {
		return currentScript;
	}

	// Just for the deprecated method so that we don't bother creating empty maps
	private static final HashMap<String, String> DUMMY_MAP = new HashMap<>(0);

	/**
	 * Deprecated. Use {@link ch.njol.skript.structures.StructOptions#getOptions(Script)} instead.
	 */
	@Deprecated
	public HashMap<String, String> getCurrentOptions() {
		Script script = getCurrentScript();
		if (script == null)
			return DUMMY_MAP;
		HashMap<String, String> options = StructOptions.getOptions(script);
		if (options == null)
			return DUMMY_MAP;
		return options;
	}

	public ScriptInfo getScriptInfo() {
		return scriptInfo;
	}

	public List<Structure> getLoadedStructures() {
		return loadedStructures;
	}

	/**
	 * @return the {@link #getLoadedStructures()} that are an instance of the given Structure class
	 */
	@SuppressWarnings("unchecked")
	public <E extends Structure> List<E> getLoadedStructures(Class<? extends E> structureClass) {
		return loadedStructures.stream()
			.filter(structureClass::isInstance)
			.map(structure -> (E) structure)
			.collect(Collectors.toList());
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
	
	public void setCurrentScript(@Nullable Script currentScript) {
		Script previous = this.currentScript;
		this.currentScript = currentScript;
		getDataInstances().forEach(data -> data.onCurrentScriptChange(previous, currentScript));

		// "Script" events
		if (previous != null)
			previous.getEventHandlers().forEach(eventHandler -> eventHandler.onUnload(currentScript));
		if (currentScript != null)
			currentScript.getEventHandlers().forEach(eventHandler -> eventHandler.onLoad(previous));
	}

	public void setScriptInfo(ScriptInfo scriptInfo) {
		this.scriptInfo = scriptInfo;
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
	
	public boolean isCurrentEvent(@Nullable Class<? extends Event> event) {
		return CollectionUtils.containsSuperclass(currentEvents, event);
	}
	
	@SafeVarargs
	public final boolean isCurrentEvent(Class<? extends Event>... events) {
		return CollectionUtils.containsAnySuperclass(currentEvents, events);
	}
	
	/*
	 * Addon data
	 */
	/**
	 * An abstract class for addons that want to add data bound to a ParserInstance.
	 * Extending classes may listen to the events {@link #onCurrentScriptChange(Script, Script)}
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

		public void onCurrentScriptChange(@Nullable Script oldScript, @Nullable Script newScript) { }

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
