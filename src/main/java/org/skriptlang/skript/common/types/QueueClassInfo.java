package org.skriptlang.skript.common.types;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.Fields;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyHandler.ConditionPropertyHandler;
import org.skriptlang.skript.lang.properties.PropertyHandler.ElementHandler;
import org.skriptlang.skript.lang.properties.PropertyHandler.ElementsHandler;
import org.skriptlang.skript.lang.properties.PropertyHandler.ExpressionPropertyHandler;
import org.skriptlang.skript.lang.properties.PropertyHandler.RangedElementsHandler;
import org.skriptlang.skript.lang.util.SkriptQueue;

import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@ApiStatus.Internal
public class QueueClassInfo extends ClassInfo<SkriptQueue> {

	public QueueClassInfo() {
		super(SkriptQueue.class, "queue");
		this.user("queues?")
			.name("Queue")
			.description("A queued list of values. Entries are removed from a queue when they are queried.")
			.examples(
				"set {queue} to a new queue",
				"add \"hello\" to {queue}",
				"broadcast the 1st element of {queue}"
			)
			.since("2.10")
			.changer(new QueueChanger())
			.parser(new QueueParser())
			.serializer(new QueueSerializer())
			.property(Property.AMOUNT,
				"The amount of elements in the queue.",
				Skript.instance(),
				new QueueAmountHandler())
			.property(Property.SIZE,
				"The size of the queue, in element count.",
				Skript.instance(),
				new QueueAmountHandler())
			.property(Property.IS_EMPTY,
				"Whether a queue is empty, i.e. whether there are no elements in the queue.",
				Skript.instance(),
				ConditionPropertyHandler.of(SkriptQueue::isEmpty))
			.property(Property.FIRST_ELEMENT,
				"",
				Skript.instance(),
				new QueueFirstElementHandler())
			.property(Property.LAST_ELEMENT,
				"",
				Skript.instance(),
				new QueueLastElementHandler())
			.property(Property.RANDOM_ELEMENT,
				"",
				Skript.instance(),
				new QueueRandomElementHandler())
			.property(Property.ORDINAL_ELEMENT,
				"",
				Skript.instance(),
				new QueueOrdinalElementHandler())
			.property(Property.END_ORDINAL_ELEMENT,
				"",
				Skript.instance(),
				new QueueEndOrdinalElementHandler())
			.property(Property.FIRST_X_ELEMENTS,
				"",
				Skript.instance(),
				new QueueFirstXElementsHandler())
			.property(Property.LAST_X_ELEMENTS,
				"",
				Skript.instance(),
				new QueueLastXElementsHandler())
			.property(Property.RANGED_ELEMENTS,
				"",
				Skript.instance(),
				new QueueRangedElementsHandler());
	}

	private static class QueueChanger implements Changer<SkriptQueue> {
		//<editor-fold desc="queue changer" defaultstate="collapsed">
		@Override
		public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			return switch (mode) {
				case ADD, REMOVE, DELETE -> new Class[] {Object.class};
				case RESET -> new Class[0];
				default -> null;
			};
		}

		@Override
		public void change(SkriptQueue[] what, Object @Nullable [] delta, ChangeMode mode) {
			for (SkriptQueue queue : what) {
				switch (mode) {
					case RESET, DELETE -> queue.clear();
					case ADD -> {
						assert delta != null;
						queue.addAll(Arrays.asList(delta));
					}
					case REMOVE -> {
						assert delta != null;
						queue.removeAll(Arrays.asList(delta));
					}
				}
			}
		}
		//</editor-fold>
	}

	private static class QueueParser extends Parser<SkriptQueue> {
		//<editor-fold desc="queue parser" defaultstate="collapsed">
		@Override
		public boolean canParse(ParseContext context) {
			return false;
		}

		@Override
		public String toString(SkriptQueue queue, int flags) {
			return Classes.toString(queue.toArray(), flags, true);
		}

		@Override
		public String toVariableNameString(SkriptQueue queue) {
			return this.toString(queue, 0);
		}
		//</editor-fold>
	}

	private static class QueueSerializer extends Serializer<SkriptQueue> {
		//<editor-fold desc="queue serializer" defaultstate="collapsed">
		@Override
		public Fields serialize(SkriptQueue queue) {
			Fields fields = new Fields();
			fields.putObject("contents", queue.toArray());
			return fields;
		}

		@Override
		public void deserialize(SkriptQueue queue, Fields fields) throws StreamCorruptedException {
			Object[] contents = fields.getObject("contents", Object[].class);
			queue.clear();
			if (contents != null)
				queue.addAll(List.of(contents));
		}

		@Override
		public boolean mustSyncDeserialization() {
			return false;
		}

		@Override
		protected boolean canBeInstantiated() {
			return true;
		}
		//</editor-fold>
	}

	private static class QueueAmountHandler implements ExpressionPropertyHandler<SkriptQueue, Integer> {
		//<editor-fold desc="queue amount property" defaultstate="collapsed">
		@Override
		public Integer convert(SkriptQueue propertyHolder) {
			return propertyHolder.size();
		}

		@Override
		public @NotNull Class<Integer> returnType() {
			return Integer.class;
		}
		//</editor-fold>
	}

	private static class QueueFirstElementHandler implements ElementHandler<SkriptQueue, Object> {
		//<editor-fold desc="first element handler" defaultstate="collapsed">
		@Override
		public @Nullable Object getElement(SkriptQueue queue) {
			return queue.pollFirst();
		}

		@Override
		public @NotNull Class<Object> returnType() {
			return Object.class;
		}
		//</editor-fold>
	}

	private static class QueueLastElementHandler implements ElementHandler<SkriptQueue, Object> {
		//<editor-fold desc="last element handler" defaultstate="collapsed">
		@Override
		public @Nullable Object getElement(SkriptQueue queue) {
			return queue.pollLast();
		}

		@Override
		public @NotNull Class<Object> returnType() {
			return Object.class;
		}
		//</editor-fold>
	}

	private static class QueueRandomElementHandler implements ElementHandler<SkriptQueue, Object> {
		//<editor-fold desc="random element handler" defaultstate="collapsed">
		@Override
		public @Nullable Object getElement(SkriptQueue queue) {
			return queue.removeSafely(ThreadLocalRandom.current().nextInt(0, queue.size()));
		}

		@Override
		public @NotNull Class<Object> returnType() {
			return Object.class;
		}
		//</editor-fold>
	}

	private static class QueueFirstXElementsHandler implements ElementsHandler<SkriptQueue, Object> {
		//<editor-fold desc="first x elements handler" defaultstate="collapsed">
		@Override
		public Object @Nullable [] getElements(SkriptQueue queue, Integer index) {
			return CollectionUtils.array(queue.removeRangeSafely(0, index));
		}
		@Override
		public @NotNull Class<Object> returnType() {
			return Object.class;
		}
		//</editor-fold>
	}

	private static class QueueLastXElementsHandler implements ElementsHandler<SkriptQueue, Object> {
		//<editor-fold desc="last x elements handler" defaultstate="collapsed">
		@Override
		public Object @Nullable [] getElements(SkriptQueue queue, Integer index) {
			return CollectionUtils.array(queue.removeRangeSafely(queue.size() - index, queue.size()));
		}
		@Override
		public @NotNull Class<Object> returnType() {
			return Object.class;
		}
		//</editor-fold>
	}

	private static class QueueOrdinalElementHandler implements ElementsHandler<SkriptQueue, Object> {
		//<editor-fold desc="ordinal element handler" defaultstate="collapsed">
		@Override
		public Object @Nullable [] getElements(SkriptQueue queue, Integer index) {
			return CollectionUtils.array(queue.removeSafely(index - 1));
		}

		@Override
		public @NotNull Class<Object> returnType() {
			return Object.class;
		}
		//</editor-fold>
	}

	private static class QueueEndOrdinalElementHandler implements ElementsHandler<SkriptQueue, Object> {
		//<editor-fold desc="end ordinal element handler" defaultstate="collapsed">
		@Override
		public Object @Nullable [] getElements(SkriptQueue queue, Integer index) {
			return CollectionUtils.array(queue.removeSafely(queue.size() - index));
		}

		@Override
		public @NotNull Class<Object> returnType() {
			return Object.class;
		}
		//</editor-fold>
	}

	private static class QueueRangedElementsHandler implements RangedElementsHandler<SkriptQueue, Object> {
		//<editor-fold desc="ranged element property handler" defaultstate="collapsed">
		@Override
		public Object @Nullable [] getElements(SkriptQueue queue, Integer start, Integer end) {
			boolean reverse = start > end;
			int from = Math.min(start, end) - 1;
			int to = Math.max(start, end);
			Object[] elements = CollectionUtils.array(queue.removeRangeSafely(from , to));
			if (reverse)
				ArrayUtils.reverse(elements);
			return elements;
		}

		@Override
		public @NotNull Class<Object> returnType() {
			return Object.class;
		}
		//</editor-fold>
	}

}
