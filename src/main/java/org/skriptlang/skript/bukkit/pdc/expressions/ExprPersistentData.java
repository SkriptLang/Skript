package org.skriptlang.skript.bukkit.pdc.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
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
import java.util.List;
import java.util.function.Consumer;

public class ExprPersistentData extends PropertyExpression<Object, Object> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprPersistentData.class, Object.class,
				"[persistent] [%-*classinfo%] data (value|tag) %string%", "chunks/worlds/entities/blocks/itemtypes/offlineplayers",
				false
			).build());
	}

	private @Nullable Expression<ClassInfoReference> type;
	private Expression<String> tag;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		tag = (Expression<String>) expressions[matchedPattern + 1];
		var classInfoExpression = (Expression<ClassInfo<?>>) expressions[matchedPattern];
		if (classInfoExpression != null) {
			type = ClassInfoReference.wrap(classInfoExpression);
		}
		setExpr(expressions[matchedPattern == 0 ? 2 : 0]);
		return true;
	}

	@Override
	protected Object[] get(Event event, Object[] source) {
		String tagName = tag.getSingle(event);
		if (tagName == null)
			return new Object[0];
		NamespacedKey key = NamespacedKey.fromString(tagName);
		ClassInfoReference expectedType = type != null ? type.getSingle(event) : null;

		List<Object> values = new ArrayList<>();
		for (Object holder : source) {
			if (holder == null)
				continue;
			editPersistentDataContainer(holder, container -> {
				assert key != null;
				Object value = null;
				// If an expected type is provided, we check if the stored value matches the expected type
				if (expectedType != null) {
					ClassInfo<?> classInfo = expectedType.getClassInfo();
					var tagType = PDCSerializer.getPDCType(classInfo);
					if (container.has(key, tagType)) {
						value = container.get(key, tagType);
						if (value != null && !classInfo.getC().isInstance(value)) {
							error("The data in tag '" + tagName + "' is was of type "
									+ Classes.toString(Classes.getSuperClassInfo(value.getClass()))
									+ ", not the expected type "
									+ Classes.toString(classInfo) + ".");
							value = null; // Type mismatch
						}
					}
				} else {
					// Try all registered PDC types
					for (var tagType : PDCSerializer.getRepresentablePDCTypes()) {
						if (container.has(key, tagType)) {
							value = container.get(key, tagType);
							break;
						}
					}
					// Finally, try to get the value as a SkriptDataType (only works for compound tags)
					// handles all tags that consist of a compound tag: "key: {}"
					if (container.has(key, SkriptDataType.get())) {
						value = container.get(key, SkriptDataType.get());
					}
				}
				if (value != null) {
					values.add(value);
				}
			});
		}
		return values.toArray(new Object[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE -> new Class<?>[]{Object.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
		var tagName = tag.getSingle(event);
		if (tagName == null)
			return;
		var key = NamespacedKey.fromString(tagName);
		if (key == null)
			return; // Invalid key, cannot proceed

		for (Object holder : getExpr().getArray(event)) {
			editPersistentDataContainer(holder, container -> {
				if (mode == Changer.ChangeMode.SET) {
					assert delta != null;
					ClassInfo<?> classInfo = Classes.getSuperClassInfo(delta[0].getClass());
					//noinspection unchecked
					PersistentDataType<?, Object> tagType = (PersistentDataType<?, Object>) PDCSerializer.getPDCType(classInfo);
					container.set(key, tagType, delta[0]);
				} else if (mode == Changer.ChangeMode.DELETE) {
					container.remove(key);
				}
			});
		}
	}

	private void editPersistentDataContainer(Object holder, Consumer<PersistentDataContainer> consumer) {
		if (holder instanceof PersistentDataHolder dataHolder)
			consumer.accept(dataHolder.getPersistentDataContainer());
		else if (holder instanceof ItemType itemType) {
			var meta = itemType.getItemMeta();
			consumer.accept(meta.getPersistentDataContainer());
			itemType.setItemMeta(meta);
		} else if (holder instanceof ItemStack itemStack) {
			if (!itemStack.hasItemMeta()) return;
			var meta = itemStack.getItemMeta();
			consumer.accept(meta.getPersistentDataContainer());
			itemStack.setItemMeta(meta);
		} else if (holder instanceof Slot slot) {
			var item =  slot.getItem();
			if (item == null || !item.hasItemMeta()) return;
			var meta = item.getItemMeta();
			consumer.accept(meta.getPersistentDataContainer());
			item.setItemMeta(meta);
			slot.setItem(item);
		} else if (holder instanceof Block block && block.getState() instanceof TileState tileState) {
			consumer.accept(tileState.getPersistentDataContainer());
			tileState.update();
		}
	}

	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "pdc tag " + tag.toString(event, debug) + " of " + getExpr().toString(event, debug);
	}

}
