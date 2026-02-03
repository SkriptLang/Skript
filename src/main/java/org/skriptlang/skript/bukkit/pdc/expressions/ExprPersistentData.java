package org.skriptlang.skript.bukkit.pdc.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ClassInfoReference;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;
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
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ExprPersistentData extends PropertyExpression<Object, Object> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprPersistentData.class, Object.class,
				"[persistent] [%-*classinfo%] [:list] data (value|tag) %string%", "chunks/worlds/entities/blocks/itemtypes/offlineplayers",
				false
			).build());
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
		}
		plural = parseResult.hasTag("list") || (parsedType != null && parsedType.isPlural().isTrue());
		setExpr(expressions[matchedPattern == 0 ? 2 : 0]);
		return true;
	}

	/**
	 * Gets all elements from the PDC, whether stored as a single value or a list.
	 */
	private List<Object> getAllElements(PersistentDataContainer container, NamespacedKey key) {
		List<Object> elements = new ArrayList<>();

		// Try representable types first
		for (var candidateType : PDCSerializer.getRepresentablePDCTypes()) {
			if (container.has(key, candidateType)) {
				Object value = container.get(key, candidateType);
				if (value != null) {
					elements.add(value);
				}
				return elements;
			}
		}

		// Try SkriptDataType for compound objects
		if (container.has(key, SkriptDataType.get())) {
			Object value = container.get(key, SkriptDataType.get());
			if (value != null) {
				elements.add(value);
			}
			return elements;
		}

		// Try as a list
		if (container.has(key, PersistentDataType.LIST.dataContainers())) {
			List<PersistentDataContainer> containers = container.get(key, PersistentDataType.LIST.dataContainers());
			if (containers != null) {
				for (var subContainer : containers) {
					elements.add(PDCSerializer.deserialize(subContainer, container.getAdapterContext()));
				}
			}
		}

		return elements;
	}

	@Override
	protected Object[] get(Event event, Object[] source) {
		String tagName = tag.getSingle(event);
		if (tagName == null)
			return new Object[0];
		NamespacedKey key = NamespacedKey.fromString(tagName);

		List<Object> values = new ArrayList<>();
		for (Object holder : source) {
			editPersistentDataContainer(holder, container -> {
				assert key != null;
				List<Object> elements = getAllElements(container, key);
				if (elements.isEmpty())
					return;

				if (parsedType != null) {
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
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.DELETE) {
			return new Class<?>[0];
		}
		if (mode == Changer.ChangeMode.SET) {
			if (parsedType != null) {
				Class<?> typeClass = parsedType.getClassInfo().getC();
				return new Class<?>[]{plural ? typeClass.arrayType() : typeClass};
			}
			return new Class<?>[]{plural ? Object[].class : Object.class};
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
		var tagName = tag.getSingle(event);
		if (tagName == null)
			return;
		var key = NamespacedKey.fromString(tagName);
		if (key == null)
			return; // Invalid key, cannot proceed

		// ensure set to correct types
		ClassInfo<?> classInfo = null;
		if (mode == Changer.ChangeMode.SET) {
			assert delta != null;
			for (Object deltaValue : delta) {
				classInfo = Classes.getSuperClassInfo(deltaValue.getClass());
				if (classInfo.getSerializer() == null) {
					error("Skript cannot serialize " + classInfo.getName().toString(true) + " as persistent data!");
					return;
				}
			}
		}

		final ClassInfo<?> finalClassInfo = classInfo;
		for (Object holder : getExpr().getArray(event)) {
			editPersistentDataContainer(holder, container -> {
				if (mode == Changer.ChangeMode.SET) {
					// don't use wrapping list if not needed.
					if (delta.length == 1) {
						//noinspection unchecked
						PersistentDataType<?, Object> tagType = (PersistentDataType<?, Object>) PDCSerializer.getPDCType(finalClassInfo);
						container.set(key, tagType, delta[0]);
					} else {
						List<PersistentDataContainer> containers = new ArrayList<>();
						for (Object object : delta) {
							containers.add(SkriptDataType.get().toPrimitive(object, container.getAdapterContext()));
						}
						container.set(key, PersistentDataType.LIST.dataContainers(), containers);
					}
				} else if (mode == Changer.ChangeMode.DELETE) {
					container.remove(key);
				}
			});
		}
	}

	/**
	 * Helper to easily edit PDCs.
	 * @param holder The holder of the PDC.
	 * @param consumer The method to run to edit the PDC.
	 */
	private void editPersistentDataContainer(Object holder, Consumer<PersistentDataContainer> consumer) {
		if (holder instanceof PersistentDataHolder dataHolder)
			consumer.accept(dataHolder.getPersistentDataContainer());
		else if (holder instanceof ItemType itemType) {
			var meta = itemType.getItemMeta();
			consumer.accept(meta.getPersistentDataContainer());
			itemType.setItemMeta(meta);
		} else if (holder instanceof ItemStack itemStack) {
			if (!itemStack.hasItemMeta()) return;
			itemStack.editPersistentDataContainer(consumer);
		} else if (holder instanceof Slot slot) {
			var item =  slot.getItem();
			if (item == null || !item.hasItemMeta()) return;
			item.editPersistentDataContainer(consumer);
			slot.setItem(item);
		} else if (holder instanceof Block block && block.getState() instanceof TileState tileState) {
			consumer.accept(tileState.getPersistentDataContainer());
			tileState.update();
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
		return "pdc tag " + tag.toString(event, debug) + " of " + getExpr().toString(event, debug);
	}

}
