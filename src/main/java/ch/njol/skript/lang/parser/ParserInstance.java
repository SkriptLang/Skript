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

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.log.HandlerList;
import ch.njol.skript.structures.StructOptions.OptionsData;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.ExperimentSet;
import org.skriptlang.skript.lang.experiment.Experimented;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ParserInstance implements Experimented {

	private static final ThreadLocal<ParserInstance> PARSER_INSTANCES = ThreadLocal.withInitial(ParserInstance::new);

	/**
	 * @return The {@link ParserInstance} for this thread.
	 */
	// TODO maybe make a one-thread cache (e.g. Pair<Thread, ParserInstance>) if it's better for performance (test)
	public static ParserInstance get() {
		return PARSER_INSTANCES.get();
	}

	private boolean isActive = false;

	/**
	 * Internal method for updating a ParserInstance's {@link #isActive()} status!
	 * You probably don't need to use this method!
	 */
	@ApiStatus.Internal
	public void setInactive() {
		this.isActive = false;
		reset();
		setCurrentScript((Script) null);
	}

	/**
	 * Internal method for updating a ParserInstance's {@link #isActive()} status!
	 * You probably don't need to use this method!
	 */
	@ApiStatus.Internal
	public void setActive(Script script) {
		this.isActive = true;
		reset(); // just to be safe
		setCurrentScript(script);
	}

	/**
	 * @return Whether this ParserInstance is currently active.
	 * An active ParserInstance may be loading, parsing, or unloading scripts.
	 * Please note that some methods may be unavailable if this method returns <code>false</code>.
	 * You should consult the documentation of the method you are calling.
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * Resets this ParserInstance to its default state.
	 * The only data retained is {@link #getCurrentScript()} and any Logging API.
	 */
	public void reset() {
		this.currentStructure = null;
		this.currentEventName = null;
		this.currentEvents = null;
		this.currentSections = new ArrayList<>();
		this.hasDelayBefore = Kleenean.FALSE;
		this.node = null;
		dataMap.clear();
	}

	// Script API

	private @Nullable Script currentScript = null;

	/**
	 * Internal method for updating the current script. Allows null parameter.
	 * @param currentScript The new Script to mark as the current Script.
	 * Please note that this method will do nothing if the current Script is the same as the new Script.
	 */
	private void setCurrentScript(@Nullable Script currentScript) {
		if (currentScript == this.currentScript) // Do nothing as it's the same script
			return;

		Script previous = this.currentScript;
		this.currentScript = currentScript;
		getDataInstances().forEach(
			data -> data.onCurrentScriptChange(currentScript != null ? currentScript.getConfig() : null)
		);

		// "Script" events
		if (previous != null) { // 'previous' is becoming inactive
			ScriptLoader.eventRegistry().events(ScriptActivityChangeEvent.class)
					.forEach(event -> event.onActivityChange(this, previous, false, currentScript));
			previous.eventRegistry().events(ScriptActivityChangeEvent.class)
					.forEach(event -> event.onActivityChange(this, previous, false, currentScript));
		}
		if (currentScript != null) { // 'currentScript' is becoming active
			ScriptLoader.eventRegistry().events(ScriptActivityChangeEvent.class)
					.forEach(event -> event.onActivityChange(this, currentScript, true, previous));
			currentScript.eventRegistry().events(ScriptActivityChangeEvent.class)
					.forEach(event -> event.onActivityChange(this, currentScript, true, previous));
		}
	}

	/**
	 * @return The Script currently being handled by this ParserInstance.
	 * @throws SkriptAPIException If this ParserInstance is not {@link #isActive()}.
	 */
	public Script getCurrentScript() {
		if (currentScript == null)
			throw new SkriptAPIException("This ParserInstance is not currently parsing/loading something!");
		return currentScript;
	}

	// Structure API

	private @Nullable Structure currentStructure = null;

	/**
	 * Updates the Structure currently being handled by this ParserInstance.
	 * @param structure The new Structure to be handled.
	 */
	public void setCurrentStructure(@Nullable Structure structure) {
		currentStructure = structure;
	}

	/**
	 * @return The Structure currently being handled by this ParserInstance.
	 */
	public @Nullable Structure getCurrentStructure() {
		return currentStructure;
	}

	/**
	 * @return Whether {@link #getCurrentStructure()} is an instance of the given Structure class.
	 */
	public boolean isCurrentStructure(Class<? extends Structure> structureClass) {
		return structureClass.isInstance(currentStructure);
	}

	/**
	 * @return Whether {@link #getCurrentStructure()} is an instance of one of the given Structure classes.
	 */
	@SafeVarargs
	public final boolean isCurrentStructure(Class<? extends Structure>... structureClasses) {
		for (Class<? extends Structure> structureClass : structureClasses) {
			if (isCurrentStructure(structureClass))
				return true;
		}
		return false;
	}

	// Event API

	private @Nullable String currentEventName;

	private Class<? extends Event> @Nullable [] currentEvents = null;

	public void setCurrentEventName(@Nullable String currentEventName) {
		this.currentEventName = currentEventName;
	}

	public @Nullable String getCurrentEventName() {
		return currentEventName;
	}

	/**
	 * @param currentEvents The events that may be present during execution.
	 *                      An instance of the events present in the provided array MUST be used to execute any loaded items.
	 */
	public void setCurrentEvents(Class<? extends Event> @Nullable [] currentEvents) {
		this.currentEvents = currentEvents;
		getDataInstances().forEach(data -> data.onCurrentEventsChange(currentEvents));
	}

	@SafeVarargs
	public final void setCurrentEvent(String name, @Nullable Class<? extends Event>... events) {
		currentEventName = name;
		setCurrentEvents(events);
		setHasDelayBefore(Kleenean.FALSE);
	}

	public void deleteCurrentEvent() {
		currentEventName = null;
		setCurrentEvents(null);
		setHasDelayBefore(Kleenean.FALSE);
	}

	public Class<? extends Event> @Nullable [] getCurrentEvents() {
		return currentEvents;
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
	public boolean isCurrentEvent(Class<? extends Event> event) {
		for (Class<? extends Event> currentEvent : currentEvents) {
			// check that current event is same or child of event we want
			if (event.isAssignableFrom(currentEvent))
				return true;
		}
		return false;
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
		for (Class<? extends Event> event : events) {
			if (isCurrentEvent(event))
				return true;
		}
		return false;
	}

	// Section API

	private List<TriggerSection> currentSections = new ArrayList<>();

	/**
	 * Updates the list of sections currently being handled by this ParserInstance.
	 * @param currentSections A new list of sections to handle.
	 */
	public void setCurrentSections(List<TriggerSection> currentSections) {
		this.currentSections = currentSections;
	}

	/**
	 * @return A list of all sections this ParserInstance is currently within.
	 */
	public List<TriggerSection> getCurrentSections() {
		return currentSections;
	}

	/**
	 * @return The outermost section which is an instance of the given class.
	 * Returns {@code null} if {@link #isCurrentSection(Class)} returns {@code false}.
	 * @see #getCurrentSections()
	 */
	public <T extends TriggerSection> @Nullable T getCurrentSection(Class<T> sectionClass) {
		for (int i = currentSections.size(); i-- > 0;) {
			TriggerSection triggerSection = currentSections.get(i);
			if (sectionClass.isInstance(triggerSection))
				//noinspection unchecked
				return (T) triggerSection;
		}
		return null;
	}

	/**
	 * @return a {@link List} of current sections that are an instance of the given class.
	 * Modifications to the returned list are not saved.
	 * @see #getCurrentSections()
	 */
	public <T extends TriggerSection> @NotNull List<T> getCurrentSections(Class<T> sectionClass) {
		List<T> list = new ArrayList<>();
		for (TriggerSection triggerSection : currentSections) {
			if (sectionClass.isInstance(triggerSection))
				//noinspection unchecked
				list.add((T) triggerSection);
		}
		return list;
	}

	/**
	 * @return Whether {@link #getCurrentSections()} contains
	 * a section instance of the given class (or subclass).
	 */
	public boolean isCurrentSection(Class<? extends TriggerSection> sectionClass) {
		for (TriggerSection triggerSection : currentSections) {
			if (sectionClass.isInstance(triggerSection))
				return true;
		}
		return false;
	}

	/**
	 * @return Whether {@link #getCurrentSections()} contains
	 * a section instance of one of the given classes (or subclasses).
	 */
	@SafeVarargs
	public final boolean isCurrentSection(Class<? extends TriggerSection>... sectionClasses) {
		for (Class<? extends TriggerSection> sectionClass : sectionClasses) {
			if (isCurrentSection(sectionClass))
				return true;
		}
		return false;
	}

	// Delay API

	private Kleenean hasDelayBefore = Kleenean.FALSE;

	/**
	 * This method should be called to indicate that
	 * the trigger will (possibly) be delayed from this point on.
	 *
	 * @see ch.njol.skript.util.AsyncEffect
	 */
	public void setHasDelayBefore(Kleenean hasDelayBefore) {
		this.hasDelayBefore = hasDelayBefore;
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

	// Logging API

	private final HandlerList handlers = new HandlerList();

	/**
	 * You probably shouldn't use this method.
	 *
	 * @return The {@link HandlerList} containing all active log handlers.
	 */
	public HandlerList getHandlers() {
		return handlers;
	}

	private @Nullable Node node;

	/**
	 * @param node The node to mark as being handled. This is mainly used for logging.
	 * Null means to mark it as no node currently being handled (that the ParserInstance is aware of).
	 */
	public void setNode(@Nullable Node node) {
		this.node = (node == null || node.getParent() == null) ? null : node;
	}

	/**
	 * @return The node currently marked as being handled. This is mainly used for logging.
	 * Null indicates no node is currently being handled (that the ParserInstance is aware of).
	 */
	public @Nullable Node getNode() {
		return node;
	}

	private String indentation = "";

	public void setIndentation(String indentation) {
		this.indentation = indentation;
	}

	public String getIndentation() {
		return indentation;
	}

	// Experiments API

	@Override
	public boolean hasExperiment(String featureName) {
		return Skript.experiments().isUsing(this.getCurrentScript(), featureName);
	}


	@Override
	public boolean hasExperiment(Experiment experiment) {
		return Skript.experiments().isUsing(this.getCurrentScript(), experiment);
	}

	/**
	 * Marks this as using an experimental feature.
	 * @param experiment The feature to register.
	 */
	@ApiStatus.Internal
	public void addExperiment(Experiment experiment) {
		Script script = this.getCurrentScript();
		ExperimentSet set = script.getData(ExperimentSet.class, () -> new ExperimentSet());
		set.add(experiment);
	}

	/**
	 * Marks this as no longer using an experimental feature (e.g. during de-registration or reload).
	 * @param experiment The feature to unregister.
	 */
	@ApiStatus.Internal
	public void removeExperiment(Experiment experiment) {
		Script script = this.getCurrentScript();
		@Nullable ExperimentSet set = script.getData(ExperimentSet.class);
		if (set == null)
			return;
		set.remove(experiment);
	}

	/**
	 * A snapshot of the experiments this script is currently known to be using.
	 * This is safe to retain during runtime (e.g. to defer a check) but will
	 * not see changes, such as if a script subsequently 'uses' another experiment.
	 *
	 * @return A snapshot of the current experiment flags in use
	 */
	public Experimented experimentSnapshot() {
		Script script = this.getCurrentScript();
		@Nullable ExperimentSet set = script.getData(ExperimentSet.class);
		if (set == null)
			return new ExperimentSet();
		return new ExperimentSet(set);
	}

	// ParserInstance Data API

	/**
	 * An abstract class for addons that want to add data bound to a ParserInstance.
	 * Extending classes may listen to the events like {@link #onCurrentEventsChange(Class[])}.
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

		/**
		 * @deprecated See {@link ScriptLoader.LoaderEvent}.
		 */
		@Deprecated
		public void onCurrentScriptChange(@Nullable Config currentScript) { }

		public void onCurrentEventsChange(Class<? extends Event> @Nullable [] currentEvents) { }

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

	/**
	 * Called when a {@link Script} is made active or inactive in a {@link ParserInstance}.
	 * This event will trigger <b>after</b> the change in activity has occurred.
	 * @see #isActive()
	 */
	@FunctionalInterface
	public interface ScriptActivityChangeEvent extends ScriptLoader.LoaderEvent, Script.Event {

		/**
		 * The method that is called when this event triggers.
		 * @param parser The ParserInstance where the activity change occurred.
		 * @param script The Script this event was registered for.
		 * @param active Whether <code>script</code> became active or inactive within <code>parser</code>.
		 * @param other The Script that was made active or inactive.
		 *  Whether it was made active or inactive is the negation of the <code>active</code>.
		 *  That is to say, if <code>script</code> became active, then <code>other</code> became inactive.
		 *  Null if <code>parser</code> was inactive (meaning no script became inactive)
		 *   or became inactive (meaning no script became active).
		 */
		void onActivityChange(ParserInstance parser, Script script, boolean active, @Nullable Script other);

	}

	// Backup API

	/**
	 * A Backup represents a ParserInstance at a certain point in time.
	 * It does not include anything regarding a ParserInstance's logging data.
	 * It is important to understand that this does not create a deep-copy of all data.
	 *  That is, the contents of any collections will remain the same, but there is no guarantee that
	 *  the contents themselves will remain unchanged.
	 * @see #backup()
	 * @see #restoreBackup(Backup) 
	 */
	public static class Backup {

		private final Script currentScript;
		private final @Nullable Structure currentStructure;
		private final @Nullable String currentEventName;
		private final Class<? extends Event> @Nullable [] currentEvents;
		private final List<TriggerSection> currentSections;
		private final Kleenean hasDelayBefore;
		private final Map<Class<? extends Data>, Data> dataMap;

		private Backup(ParserInstance parser) {
			//noinspection ConstantConditions - parser will be active, meaning there is a current script
			this.currentScript = parser.currentScript;
			this.currentStructure = parser.currentStructure;
			this.currentEventName = parser.currentEventName != null ? parser.currentEventName : null;
			this.currentEvents = parser.currentEvents != null
				? Arrays.copyOf(parser.currentEvents, parser.currentEvents.length)
				: null;
			this.currentSections = new ArrayList<>(parser.currentSections);
			this.hasDelayBefore = parser.hasDelayBefore;
			this.dataMap = new HashMap<>(parser.dataMap);
		}

		private void apply(ParserInstance parser) {
			parser.setCurrentScript(currentScript);
			parser.currentStructure = this.currentStructure;
			parser.currentEventName = this.currentEventName;
			parser.currentEvents = this.currentEvents;
			parser.currentSections = this.currentSections;
			parser.hasDelayBefore = this.hasDelayBefore;
			parser.dataMap.clear();
			parser.dataMap.putAll(this.dataMap);
		}

	}

	/**
	 * Creates a backup of this ParserInstance, which represents its current state (excluding any Logging API).
	 * @return A backup of this ParserInstance.
	 * @see #restoreBackup(Backup)
	 */
	public Backup backup() {
		if (!isActive())
			throw new SkriptAPIException("Backups may only be created from active ParserInstances");
		return new Backup(this);
	}

	/**
	 * Restores a backup onto this ParserInstance.
	 *  That is, this entire ParserInstance, except any Logging API, will be overridden.
	 * @param backup The backup to apply.
	 * @see #backup()
	 */
	public void restoreBackup(Backup backup) {
		backup.apply(this);
	}

	// Deprecated API

	/**
	 * @deprecated Use {@link Script#getData(Class)} instead. The {@link OptionsData} class should be obtained.
	 * Example: <code>script.getData(OptionsData.class)</code>
	 */
	@Deprecated
	public HashMap<String, String> getCurrentOptions() {
		if (!isActive())
			return new HashMap<>(0);
		OptionsData data = getCurrentScript().getData(OptionsData.class);
		if (data == null)
			return new HashMap<>(0);
		return new HashMap<>(data.getOptions()); // ensure returned map is modifiable
	}

	/**
	 * @deprecated Use {@link #getCurrentStructure()}
	 */
	@Deprecated
	public @Nullable SkriptEvent getCurrentSkriptEvent() {
		Structure structure = getCurrentStructure();
		if (structure instanceof SkriptEvent)
			return (SkriptEvent) structure;
		return null;
	}

	/**
	 * @deprecated Use {@link #setCurrentStructure(Structure)}.
	 */
	@Deprecated
	public void setCurrentSkriptEvent(@Nullable SkriptEvent currentSkriptEvent) {
		setCurrentStructure(currentSkriptEvent);
	}

	/**
	 * @deprecated Use {@link #setCurrentStructure(Structure)} with 'null'.
	 */
	@Deprecated
	public void deleteCurrentSkriptEvent() {
		setCurrentStructure(null);
	}

	/**
	 * @deprecated Addons should no longer be modifying this.
	 */
	@Deprecated
	public void setCurrentScript(@Nullable Config currentScript) {
		if (currentScript == null)
			return;
		File file = currentScript.getFile();
		if (file == null)
			return;
		Script script = ScriptLoader.getScript(file);
		if (script != null)
			setActive(script);
	}

}
