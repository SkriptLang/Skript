package org.skriptlang.skript.lang.parsing.constraints;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * A stack of all the parsing constraints currently applied.
 * Each parsing site may push a new set of constraints onto the stack,
 * which may contain both permanent and temporary constraints.
 * The stack is used to check whether infos and elements are accepted
 * during parsing.
 * Temporary constraints are only applied from the top of the stack,
 * while permanent constraints are applied from all levels of the stack.
 */
public class ConstraintStack  {

	private final Deque<Constraints> stack = new ArrayDeque<>();

	/**
	 * Pushes a new set of constraints onto the stack.
	 * @param constraints The constraints to push.
	 */
	public void push(Constraints constraints) {
		this.stack.push(constraints);
	}

	/**
	 * Gets the top set of constraints on the stack.
	 */
	public Constraints peek() {
		return this.stack.peek();
	}

	/**
	 * Pops the top set of constraints off the stack.
	 * @throws IllegalStateException if the stack is empty.
	 */
	public Constraints pop() throws IllegalStateException {
		if (this.stack.isEmpty()) {
			throw new IllegalStateException("Constraint stack is empty");
		}
		return this.stack.pop();
	}

	/**
	 * Checks if the stack is empty.
	 * @return Whether the stack is empty.
	 */
	public boolean isEmpty() {
		return this.stack.isEmpty();
	}

	/**
	 * Converts this stack into a single Constraints object.
	 * Permanent constraints are combined from all levels of the stack,
	 * while temporary constraints are taken from the top level only.
	 * @return The combined Constraints object.
	 */
	public Constraints asConstraints() {
		return new Constraints() {
			@Override
			public Iterable<Constraint> permanentConstraints() {
				if (stack.isEmpty()) {
					return List.of();
				}
				return stack.stream()
					.flatMap(c -> StreamSupport.stream(c.permanentConstraints().spliterator(), false))
					::iterator;
			}

			@Override
			public Iterable<Constraint> temporaryConstraints() {
				if (stack.isEmpty()) {
					return List.of();
				}
				return stack.peek().temporaryConstraints();
			}
		};
	}

}
