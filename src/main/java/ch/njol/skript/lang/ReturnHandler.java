package ch.njol.skript.lang;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.parser.ParserInstance;
import org.bukkit.event.Event;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;
import java.util.LinkedList;

public interface ReturnHandler<T> {

	/**
	 * Loads the code in the given {@link SectionNode} using the same logic as
	 * {@link Section#loadCode(SectionNode)} and pushes the section onto the
	 * return handler stack
	 * <br>
	 * <b>This method may only be called by a {@link Section}</b>
	 * @throws SkriptAPIException if this return handler is not a {@link Section}
	 */
	@NonExtendable
	default void loadReturnableSectionCode(SectionNode node) {
		if (!(this instanceof Section))
			throw new SkriptAPIException("loadReturnableSectionCode called on a non-section object");
		ParserInstance parser = ParserInstance.get();
		ReturnHandlerStack stack = parser.getData(ReturnHandlerStack.class);
		stack.push(this);
		Section section = (Section) this;
		try {
			section.loadCode(node);
		} finally {
			stack.pop();
		}
	}

	/**
	 * Loads the code in the given {@link SectionNode} using the same logic as
	 * {@link Section#loadCode(SectionNode, String, Class[])} and pushes the section onto the
	 * return handler stack
	 * <br>
	 * <b>This method may only be called by a {@link Section}</b>
	 * @param node the section node
	 * @param name the name of the event(s) being used
	 * @param events the event(s) during the section's execution
	 * @return a returnable trigger containing the loaded section.
	 * This should be stored and used to run the section one or more times
	 * @throws SkriptAPIException if this return handler is not a {@link Section}
	 */
	@NonExtendable
	default ReturnableTrigger<T> loadReturnableSectionCode(SectionNode node, String name, Class<? extends Event>[] events) {
		if (!(this instanceof Section))
			throw new SkriptAPIException("loadReturnableSectionCode called on a non-section object");
		ParserInstance parser = ParserInstance.get();
		ParserInstance.Backup parserBackup = parser.backup();
		parser.reset();

		parser.setCurrentEvent(name, events);
		SkriptEvent skriptEvent = new SectionSkriptEvent(name, (Section) this);
		parser.setCurrentStructure(skriptEvent);
		ReturnHandlerStack stack = parser.getData(ReturnHandlerStack.class);

		try {
			return new ReturnableTrigger<>(
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
			parser.restoreBackup(parserBackup);
		}
	}

	/**
	 * Loads the code in the given {@link SectionNode} into a {@link ReturnableTrigger}.
	 * <br>
	 * This is a general method to load a section node without extra logic
	 * done to the {@link ParserInstance}.
	 * The calling code is expected to manage the {@code ParserInstance} accordingly, which may vary depending on
	 * where the code being loaded is located and what state the {@code ParserInstance} is in.
	 * @param node the section node to load
	 * @param name the name of the trigger
	 * @param event the {@link SkriptEvent} of the trigger
	 * @return a returnable trigger containing the loaded section node
	 */
	@NonExtendable
	default ReturnableTrigger<T> loadReturnableTrigger(SectionNode node, String name, SkriptEvent event) {
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

	/**
	 * Called when {@link ch.njol.skript.effects.EffReturn} is executed
	 * @param event the event providing context
	 * @param value an expression representing the value(s) to return
	 */
	void returnValues(Event event, Expression<? extends T> value);

	/**
	 * @return whether this return handler may accept multiple return values
	 */
	boolean isSingleReturnValue();

	/**
	 * The return type of this return handler, or null if it can't
	 * accept return values in this context (e.g. a function without a return type).
	 *
	 * @return the return type
	 */
	@Nullable Class<? extends T> returnValueType();

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
