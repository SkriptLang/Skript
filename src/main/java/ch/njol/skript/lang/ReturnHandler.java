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
package ch.njol.skript.lang;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public interface ReturnHandler<T> {

	@NonExtendable
	default void loadReturnableSectionCode(SectionNode node) {
		if (!(this instanceof TriggerSection))
			throw new SkriptAPIException("loadReturnableSectionCode called on a non-section object");
		ParserInstance parser = ParserInstance.get();
		ReturnHandlerStack stack = parser.getData(ReturnHandlerStack.class);
		stack.push(this);
		TriggerSection section = (TriggerSection) this;
		List<TriggerSection> currentSections = parser.getCurrentSections();
		currentSections.add(section);
		try {
			section.setTriggerItems(ScriptLoader.loadItems(node));
		} finally {
			currentSections.remove(currentSections.size() - 1);
			stack.pop();
		}
	}

	@NonExtendable
	default Trigger loadReturnableTrigger(SectionNode node, String name, Class<? extends Event>[] events) {
		ParserInstance parser = ParserInstance.get();
		ReturnHandlerStack stack = parser.getData(ReturnHandlerStack.class);
		boolean isSection = this instanceof Section;
		SkriptEvent skriptEvent = isSection ? new SectionSkriptEvent(name, (Section) this) : new SimpleEvent();
		String previousName = null;
		Class<? extends Event>[] previousEvents = null;
		Structure previousStructure = null;
		List<TriggerSection> previousSections = null;
		Kleenean previousDelay = null;
		Deque<ReturnHandler<?>> previousReturnStack = null;
		if (isSection) {
			previousName = parser.getCurrentEventName();
			previousEvents = parser.getCurrentEvents();
			previousStructure = parser.getCurrentStructure();
			previousSections = parser.getCurrentSections();
			previousDelay = parser.getHasDelayBefore();

			parser.setCurrentEvent(name, events);
			parser.setCurrentStructure(skriptEvent);
			parser.setCurrentSections(new ArrayList<>());
			parser.setHasDelayBefore(Kleenean.FALSE);
		}
		try {
			// push handler
			return new ReturnableTrigger<T>(
				this,
				parser.getCurrentScript(),
				name,
				skriptEvent,
				trigger -> {
					stack.push(trigger);
					return ScriptLoader.loadItems(node);
				}
			);
		} finally {
			stack.pop();
			if (isSection) {
				parser.setCurrentEvent(previousName, previousEvents);
				parser.setCurrentStructure(previousStructure);
				parser.setCurrentSections(previousSections);
				parser.setHasDelayBefore(previousDelay);
			}
		}
	}

	@NonExtendable
	default Trigger loadReturnableTrigger(SectionNode node, String name, SkriptEvent event) {
		ParserInstance parser = ParserInstance.get();
		ReturnHandlerStack stack = parser.getData(ReturnHandlerStack.class);
		try {
			return new ReturnableTrigger<T>(
				this,
				parser.getCurrentScript(),
				name,
				event,
				trigger -> {
					stack.push(trigger);
					return ScriptLoader.loadItems(node);
				}
			);
		} finally {
			stack.pop();
		}
	}

	void returnValues(T @Nullable [] values);

	boolean singleReturnValue();

	@Nullable ClassInfo<T> returnValueType();

	class ReturnHandlerStack extends ParserInstance.Data {

		private final Deque<ReturnHandler<?>> stack = new LinkedList<>();

		public ReturnHandlerStack(ParserInstance parserInstance) {
			super(parserInstance);
		}

		public Deque<ReturnHandler<?>> getStack() {
			return stack;
		}

		/**
		 * Retrieves the current {@link ReturnHandler}
		 * @return the return data
		 */
		public @Nullable ReturnHandler<?> getCurrentHandler() {
			return stack.peek();
		}

		/**
		 * Pushes the current return handler onto the return stack.
		 * <br>
		 * <b>Note: After the trigger finished loading,
		 * {@link ReturnHandlerStack#pop()} <u>MUST</u> be called</b>
		 * @param handler the return handler
		 * @see ReturnHandlerStack#pop()
		 */
		public void push(ReturnHandler<?> handler) {
			stack.push(handler);
		}

		/**
		 * Pops the current handler off the return stack.
		 * Should be called after the trigger has finished loading.
		 * @return the popped return data
		 * @see ReturnHandlerStack#push(ReturnHandler)  
		 */
		public ReturnHandler<?> pop() {
			return stack.pop();
		}

	}
	
}
