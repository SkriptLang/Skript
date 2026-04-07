package org.skriptlang.skript.bukkit.pdc.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.NamespacedUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ClassInfoReference;
import ch.njol.skript.util.slot.Slot;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.pdc.PDCSerializer;
import org.skriptlang.skript.bukkit.pdc.SkriptDataType;
import org.skriptlang.skript.lang.arithmetic.Arithmetics;
import org.skriptlang.skript.lang.arithmetic.Operator;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;

import static ch.njol.skript.classes.Changer.ChangerUtils.getArithmeticChangeTypes;

@Name("Persistent Data Value")
@Description("""
	Provides access to the 'persistent data container' Bukkit provides on many objects. These values are stored on the \
	chunk/world/item/entity directly, like custom NBT, but are much faster and reliable to access.
	Persistent values natively support numbers and text, but any Skript type that can be saved in a variable can also be \
	stored in PDC via this expression. Lists of objects can also be saved.
	If you attempt to save invalid types, runtime errors will be thrown.
	
	The names of tags must be valid namespaced keys, i.e. a-z, 0-9, '_', '.', '/', and '-' are the allowed characters. \
	If no namespace is provided, it will default to 'minecraft'.
	""")
@Example("set persistent data tag \"custom_damage\" of player's tool to 10")
@Example("""
	on jump:
		if data tag "boost" of player's boots is set:
			push player upwards
	""")
@Example("""
	on shoot:
		set {_strength} to number data tag "strength" of shooter's tool
		if {_strength} is set:
			set number data tag "damage" of projectile to {_strength}
	
	on damage:
		set {_damage} to data tag "damage" of projectile
		if {_damage} is set:
			set damage to {_damage}
	""")
@Example("set {_pet-uuids::*} to list data tag \"pets\" of player")
@Since("2.15")
@Keywords({"pdc", "persistent data container", "custom data", "nbt"})
public class ExprPersistentData extends PropertyExpression<Object, Object> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprPersistentData.class, Object.class,
				"[persistent] [%-*classinfo%] [:list] data (value|tag) %string%",
					"chunks/worlds/entities/blocks/itemtypes/offlineplayers",
				false
			)
			.supplier(ExprPersistentData::new)
			.build());
	}

	private @Nullable ClassInfoReference parsedType;
	private Expression<String> tag;
	private boolean plural;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		tag = (Expression<String>) expressions[matchedPattern + 1];
		var classInfoExpression = (Expression<ClassInfo<?>>) expressions[matchedPattern];
		if (classInfoExpression != null) {
			var type = ClassInfoReference.wrap(classInfoExpression);
			parsedType = ((Literal<ClassInfoReference>) type).getSingle();
			// check if serializable
			ClassInfo<?> classInfo = parsedType.getClassInfo();
			if (classInfo.getSerializer() == null) {
				Skript.error("Skript cannot serialize " + classInfo.getName().toString(true) + " as persistent data!");
				return false;
			}
		}
		plural = parseResult.hasTag("list") || (parsedType != null && parsedType.isPlural().isTrue());
		setExpr(expressions[matchedPattern == 0 ? 2 : 0]);
		return true;
	}

	/**
	 * Result of retrieving elements from PDC, including storage format info.
	 * @param elements The deserialized elements
	 * @param storedAsList Whether the data was stored as a list (vs a single value)
	 */
	private record ElementsResult(List<Object> elements, boolean storedAsList) {}

	/**
	 * Gets all elements from the PDC, whether stored as a single value or a list.
	 * Also indicates whether the data was stored as a list.
	 */
	private ElementsResult getAllElements(PersistentDataContainerView container, NamespacedKey key) {
		List<Object> elements = new ArrayList<>();

		// Try representable types first (singular storage)
		for (var candidateType : PDCSerializer.getRepresentablePDCTypes()) {
			if (container.has(key, candidateType)) {
				Object value = container.get(key, candidateType);
				if (value != null) {
					elements.add(value);
				}
				return new ElementsResult(elements, false);
			}
		}

		// Try SkriptDataType for compound objects (singular storage)
		if (container.has(key, SkriptDataType.get())) {
			Object value = container.get(key, SkriptDataType.get());
			if (value != null) {
				elements.add(value);
			}
			return new ElementsResult(elements, false);
		}

		// Try as a list
		if (container.has(key, PersistentDataType.LIST.dataContainers())) {
			List<PersistentDataContainer> containers = container.get(key, PersistentDataType.LIST.dataContainers());
			if (containers != null) {
				for (var subContainer : containers) {
					elements.add(PDCSerializer.deserialize(subContainer, container.getAdapterContext()));
				}
			}
			return new ElementsResult(elements, true);
		}

		// Key doesn't exist
		return new ElementsResult(elements, false);
	}

	@Override
	protected Object[] get(Event event, Object[] source) {
		String tagName = tag.getSingle(event);
		if (tagName == null)
			return new Object[0];
		NamespacedKey key = NamespacedUtils.checkValidationAndSend(tagName.toLowerCase(Locale.ENGLISH), this);
		if (key == null)
			return new Object[0];

		List<Object> values = new ArrayList<>();
		for (Object holder : source) {
			getPersistentDataContainer(holder, container -> {
				ElementsResult result = getAllElements(container, key);
				List<Object> elements = result.elements();
				if (elements.isEmpty())
					return;

				// Check for list/singular mismatch
				if (plural && !result.storedAsList()) {
					error("The data in tag '" + tagName + "' is a single value, not a list. "
							+ "Use 'data tag' instead of 'list data tag'.");
					return;
				}
				if (!plural && result.storedAsList()) {
					error("The data in tag '" + tagName + "' is a list, not a single value. "
							+ "Use 'list data tag' instead of 'data tag'.");
					return;
				}

				if (parsedType != null) {
					// we have a specific type to aim for
					ClassInfo<?> classInfo = parsedType.getClassInfo();

					if (plural) {
						// Plural: get all matching elements, warn on mismatches
						Set<Class<?>> mismatches = new HashSet<>();
						for (Object element : elements) {
							if (classInfo.getC().isInstance(element)) {
								values.add(element);
							} else {
								mismatches.add(element.getClass());
							}
						}
						if (!mismatches.isEmpty()) {
							warning(mismatches.size() + " element(s) in tag '" + tagName + "' were of type(s) "
									+ Classes.toString(mismatches.stream()
										.map(Classes::getSuperClassInfo)
										.toArray(ClassInfo[]::new), true)
									+ ", not the expected type "
									+ Classes.toString(classInfo) + ". Skipping.");
						}
					} else {
						// Singular: get first element and check type
						Object first = elements.getFirst();
						if (classInfo.getC().isInstance(first)) {
							values.add(first);
						} else {
							error("The data in tag '" + tagName + "' was of type "
									+ Classes.toString(Classes.getSuperClassInfo(first.getClass()))
									+ ", not the expected type "
									+ Classes.toString(classInfo) + ".");
						}
					}
				} else {
					// No type specified: return all elements if plural, else first
					if (plural) {
						values.addAll(elements);
					} else {
						values.add(elements.getFirst());
					}
				}
			});
		}
		return values.toArray(new Object[0]);
	}

	@Override
	public boolean isSingle() {
		return !plural;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case DELETE -> new Class[0];
			case SET, ADD, REMOVE, RESET -> {
				if (parsedType != null) {
					ClassInfo<?> type = parsedType.getClassInfo();
					Changer<?> changer = type.getChanger();
					if (changer != null) {
						yield changer.acceptChange(mode);
					}
					Class<?> changeType = type.getC();
					if (plural)
						changeType = changeType.arrayType();
					if (mode == ChangeMode.SET) {
						yield CollectionUtils.array(changeType);
					} else if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE) {
						if (plural)
							yield CollectionUtils.array(changeType);
						yield getArithmeticChangeTypes(type.getC(), mode, operation -> type.getC().isAssignableFrom(operation.returnType()));
					}
					yield null;
				}
				yield CollectionUtils.array(plural ? Object[].class : Object.class);
			}
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		String tagName = tag.getSingle(event);
		if (tagName == null)
			return;
		NamespacedKey key = NamespacedUtils.checkValidationAndSend(tagName.toLowerCase(Locale.ENGLISH), this);
		if (key == null)
			return;

		// ensure set to correct types
		ClassInfo<?> classInfo = null;
		if (mode == ChangeMode.SET || (plural && (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE))) {
			assert delta != null;
			for (Object deltaValue : delta) {
				classInfo = Classes.getSuperClassInfo(deltaValue.getClass());
				if (classInfo.getSerializer() == null) {
					error("Skript cannot serialize " + classInfo.getName().toString(true) + " as persistent data!");
					return;
				}
			}
		} else if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE) {
			assert delta != null;
			classInfo = Classes.getSuperClassInfo(delta[0].getClass()); // plural is false so this is safe
		}

		Set<Block> invalidBlocks = new HashSet<>();
		final ClassInfo<?> finalClassInfo = classInfo;
		for (Object holder : getExpr().getArray(event)) {
			if (holder instanceof Block block && !(block.getState() instanceof TileState)) {
				invalidBlocks.add(block);
				continue;
			}
			editPersistentDataContainer(holder, container -> {
				switch (mode) {
					case SET -> {
						if (!plural) {
							// Singular: store first value only
							//noinspection unchecked
							PersistentDataType<?, Object> tagType = (PersistentDataType<?, Object>) PDCSerializer.getPDCType(finalClassInfo);
							container.set(key, tagType, delta[0]);
						} else {
							// List: always store as list, even for single element
							List<PersistentDataContainer> containers = new ArrayList<>();
							for (Object object : delta) {
								containers.add(SkriptDataType.get().toPrimitive(object, container.getAdapterContext()));
							}
							container.set(key, PersistentDataType.LIST.dataContainers(), containers);
						}
					}
					case DELETE -> container.remove(key);
					case ADD, REMOVE -> {
						if (!plural) {
							// Check for list/singular mismatch
							if (container.has(key, PersistentDataType.LIST.dataContainers())) {
								error("The data in tag '" + tagName + "' is a list, not a single value. "
									+ "Use 'list data tag' instead of 'data tag'.");
								return;
							}
							//noinspection unchecked
							var tagType = (PersistentDataType<?, Object>) PDCSerializer.getPDCType(finalClassInfo);
							if (!container.has(key, tagType))
								return;
							Object original = container.get(key, tagType);
							addOrRemoveFromSingleValue(original, delta, mode, value -> {
								// arithmetic may have modified the value's type, so need to check again
								//noinspection unchecked
								var resultType = (PersistentDataType<?, Object>) PDCSerializer.getPDCType(Classes.getSuperClassInfo(value.getClass()));
								container.set(key, resultType, value);
							});
						} else {
							// Check for list/singular mismatch
							if (container.has(key) && !container.has(key, PersistentDataType.LIST.dataContainers())) {
								error("The data in tag '" + tagName + "' is a single value, not a list. "
									+ "Use 'data tag' instead of 'list data tag'.");
								return;
							}
							addOrRemoveFromList(container, key, delta, mode);
						}
					}
					case null, default -> {
					}
				}
			});
		}
		if (!invalidBlocks.isEmpty()) {
			Block[] blocks = invalidBlocks.stream().limit(3).toArray(Block[]::new);
			warning("Could not set persistent data on blocks (" + Classes.toString(blocks, true)
					+ ") as they are not tile entities (chests, furnaces, signs, etc.).");
		}
	}

	/**
	 * Gets the data container of an object. The returned container should not be modified.
	 * Use {@link #editPersistentDataContainer(Object, Consumer)} if editing is desired.
	 * @param holder Source of the container
	 * @param consumer Code to run with the container
	 */
	private void getPersistentDataContainer(Object holder, Consumer<PersistentDataContainerView> consumer) {
		switch (holder) {
			case PersistentDataHolder dataHolder -> consumer.accept(dataHolder.getPersistentDataContainer());
			case ItemType itemType -> {
				var meta = itemType.getItemMeta();
				consumer.accept(meta.getPersistentDataContainer());
			}
			case ItemStack itemStack -> {
				if (!itemStack.hasItemMeta())
					return;
				consumer.accept(itemStack.getPersistentDataContainer());
			}
			case Slot slot -> {
				var item = slot.getItem();
				if (item == null || !item.hasItemMeta())
					return;
				consumer.accept(item.getPersistentDataContainer());
			}
			case Block block when block.getState() instanceof TileState tileState ->
				consumer.accept(tileState.getPersistentDataContainer());
			case null, default -> {
			}
		}

	}

	/**
	 * Helper to easily edit PDCs.
	 * @param holder The holder of the PDC.
	 * @param consumer The method to run to edit the PDC.
	 */
	private void editPersistentDataContainer(Object holder, Consumer<PersistentDataContainer> consumer) {
		switch (holder) {
			case PersistentDataHolder dataHolder -> consumer.accept(dataHolder.getPersistentDataContainer());
			case ItemType itemType -> {
				var meta = itemType.getItemMeta();
				consumer.accept(meta.getPersistentDataContainer());
				itemType.setItemMeta(meta);
			}
			case ItemStack itemStack -> {
				if (!itemStack.hasItemMeta())
					return;
				itemStack.editPersistentDataContainer(consumer);
			}
			case Slot slot -> {
				var item = slot.getItem();
				if (item == null || !item.hasItemMeta())
					return;
				item.editPersistentDataContainer(consumer);
				slot.setItem(item);
			}
			case Block block when block.getState() instanceof TileState tileState -> {
				consumer.accept(tileState.getPersistentDataContainer());
				tileState.update();
			}
			case null, default -> {
			}
		}
	}

	@Override
	public Class<?> getReturnType() {
		if (parsedType != null) {
			return parsedType.getClassInfo().getC();
		}
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder ssb = new SyntaxStringBuilder(event, debug);
		if (parsedType != null) {
			ssb.append(parsedType.getClassInfo().getName().toString());
		}
		ssb.appendIf(plural, "list").append("data tag", tag, "of", getExpr());
		return ssb.toString();
	}

	/**
	 * Helper for adding/removing values from a given value. Falls back to arithmetic, then type changers.
	 * See {@link Variables} for initial impl. Adapted due to need to maintain type.
	 * @param originalValue The previous existing value.
	 * @param delta The values to add/remove
	 * @param mode Whether to add or remove.
	 * @param setSingle A consumer used to set the new value if arithmetic is used.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private void addOrRemoveFromSingleValue(Object originalValue, Object[] delta, ChangeMode mode, Consumer<Object> setSingle) {
		// Todo: decide if to, and how best to, de-duplicate the code shared between this and Variable#change(). Perhaps pr to improve changer standards/utils.
		Class<?> clazz = originalValue == null ? null : originalValue.getClass();
		Operator operator = mode == ChangeMode.ADD ? Operator.ADDITION : Operator.SUBTRACTION;
		Changer<?> changer;
		Class<?>[] acceptedClasses;
		// attempt to find arithmetic for each value in delta
		if (clazz == null || !Arithmetics.getOperations(operator, clazz).isEmpty()) {
			boolean changed = false;
			for (Object newValue : delta) {
				var info = Arithmetics.getOperationInfo(operator, clazz != null ? (Class) clazz : newValue.getClass(), newValue.getClass());
				if (info == null)
					continue;
				Object value = originalValue == null ? Arithmetics.getDefaultValue(info.left()) : originalValue;
				if (value == null)
					continue;
				// ensure the operation returns a valid pdc-serializable value
				if (Classes.getSuperClassInfo(info.returnType()).getSerializer() == null)
					continue;
				originalValue = info.operation().calculate(value, newValue);
				changed = true;
			}
			if (changed)
				setSingle.accept(originalValue);
			// attempt to use the class's changer
		} else if ((changer = Classes.getSuperClassInfo(clazz).getChanger()) != null && (acceptedClasses = changer.acceptChange(mode)) != null) {
			Object[] originalValueArray = (Object[]) Array.newInstance(originalValue.getClass(), 1);
			originalValueArray[0] = originalValue;

			Class<?>[] singularAcceptedClasses = new Class<?>[acceptedClasses.length];
			for (int i = 0; i < acceptedClasses.length; i++)
				singularAcceptedClasses[i] = acceptedClasses[i].isArray() ? acceptedClasses[i].getComponentType() : acceptedClasses[i];

			Object[] convertedDelta = Converters.convert(delta, singularAcceptedClasses, Object.class);
			Changer.ChangerUtils.change(changer, originalValueArray, convertedDelta, mode);
		}
	}

	/**
	 * Helper for adding/removing values from a given list.
	 * @param container The container to add to/remove from.
	 * @param key The key for the existing list.
	 * @param delta The values to add/remove
	 * @param mode Whether to add or remove.
	 */
	private void addOrRemoveFromList(PersistentDataContainer container, NamespacedKey key, Object[] delta, ChangeMode mode) {
		// get all values
		List<PersistentDataContainer> containers = new ArrayList<>();
		if (container.has(key, PersistentDataType.LIST.dataContainers())) {
			var data = container.get(key, PersistentDataType.LIST.dataContainers());
			assert data != null;
			containers.addAll(data);
		}
		// add/remove (for remove, we need to convert object, compare, then remove if needed)
		if (mode == ChangeMode.ADD) {
			for (Object object : delta) {
				containers.add(SkriptDataType.get().toPrimitive(object, container.getAdapterContext()));
			}
		} else {
			var containerIterator = containers.iterator();
			List<Object> toRemove = new ArrayList<>(delta.length);
			toRemove.addAll(Arrays.asList(delta));
			while (containerIterator.hasNext()) {
				// deserialize
				var toDeserialize = containerIterator.next();
				Object value = PDCSerializer.deserialize(toDeserialize, toDeserialize.getAdapterContext());
				// compare
				var removeIterator = toRemove.iterator();
				while (removeIterator.hasNext()) {
					Object removeCandidate = removeIterator.next();
					if (Relation.EQUAL.isImpliedBy(Comparators.compare(removeCandidate, value))) {
						// remove
						containerIterator.remove();
						removeIterator.remove();
						break;
					}
				}
			}
		}
		// re-write remaining values
		container.set(key, PersistentDataType.LIST.dataContainers(), containers);
	}

}
