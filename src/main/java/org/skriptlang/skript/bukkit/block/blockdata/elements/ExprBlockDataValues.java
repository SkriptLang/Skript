package org.skriptlang.skript.bukkit.block.blockdata.elements;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.block.blockdata.BlockDataHolder;
import org.skriptlang.skript.bukkit.block.blockdata.BlockDataTag;
import org.skriptlang.skript.bukkit.block.blockdata.BlockDataValueType;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Name("Block Data Values")
@Description("""
	The value of a blockdata tag on a block or block related object.
	Note that specific blockdata tags can return, and be changed to, objects other than strings.
	Examples:
		- tag "waterlogged" will always return a boolean value, and can be changed to a boolean value or string of a boolean: "true"
		- tag "facing" will always return a direction value, and can be changed to a direction value or string of a direction: "north"
		- tag "pickles" will always return an integer value, and can be changed to an integer value or string of an integer: "1"
	""")
@Example("""
	set blockdata  "waterlogged" of {_campfire} to false
	set blockdata "waterlogged" of {_campfire} to "true"
	if blockdata "waterlogged" of {_campfire} is "true":
		# FAILS
	else if blockdata "waterlogged" of {_campfire} is true:
		# PASSES
	""")
@Example("""
	set blockdata "facing" of {_oakStairs} to north
	set blockdata "facing" of {_oakStairs} to "west"
	if blockdata "facing" of {_oakStairs} is "west":
		# FAILS
	else if blockdata "facing" of {_oakStairs} is west:
		# PASSES
	""")
@Example("""
	set blockdata "pickles" of {_seaPickle} to 1
	set blockdata "pickles" of {_seaPickle} to "5"
	if blockdata value "pickles" of {_seaPickle} is "5":
		# FAILS
	else if blockdata tag value "pickles" of {_seaPickle} is 5:
		# PASSES
	""")
@Example("""
	set blockdata "half" of {_oakStairs} to "top"
	if blockdata "half" of {_oakStairs} is "top":
		# PASSES
	""")
@Example("""
	reset blockdata "waterlogged", "facing", "half" and "shape" of {_oakStairs}
	""")
@Since("INSERT VERSION")
public class ExprBlockDataValues extends SimpleExpression<Object> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.simple(
			ExprBlockDataValues.class,
			ExprBlockDataValues::new,
			Object.class,
			"[the] block[ ]data [tag[s]] [value[s]] %strings% of %" + BlockDataHolder.PLURAL_PATTERN_TYPES + "%"
		));
	}

	private Expression<String> strings;
	private Expression<?> objects;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		strings = (Expression<String>) exprs[0];
		objects = exprs[1];
		return true;
	}

	@Override
	protected Object @Nullable [] get(Event event) {
		String[] strings = this.strings.getArray(event);
		List<Object> values = new ArrayList<>();
		objects.stream(event).map(object -> {
				BlockDataHolder holder = BlockDataHolder.getHolder(object);
				if (holder == null)
					return null;
				//noinspection unchecked
				return BlockDataTag.of(holder.getBlockData(object), strings);
			})
			.filter(Objects::nonNull)
			.forEach(tags -> values.addAll(Arrays.stream(tags).map(BlockDataTag::getValue).toList()));
		return values.toArray();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET) {
			return BlockDataValueType.TYPE_CLASSES;
		} else if (mode == ChangeMode.RESET) {
			return CollectionUtils.array();
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Object change = delta != null ? delta[0] : null;
		Set<String> strings = this.strings.stream(event).map(String::toLowerCase).collect(Collectors.toSet());
		Map<BlockDataValueType<?>, List<String>> rejected = new HashMap<>();
		Map<BlockDataValueType<?>, Map<BlockDataTag, List<Material>>> invalid = new HashMap<>();

		for (Object object : objects.getArray(event)) {
			BlockDataHolder holder = BlockDataHolder.getHolder(object);
			if (holder == null)
				continue;
			//noinspection unchecked
			BlockData blockData = holder.getBlockData(object);
			BlockDataTag[] tags;
			if (mode == ChangeMode.SET) {
				tags = BlockDataTag.of(blockData, strings);
			} else {
				tags = BlockDataTag.of(blockData);
			}
			if (tags == null)
				continue;
			for (BlockDataTag tag : tags) {
				if (!strings.contains(tag.getKey()))
					continue;
				if (!tag.attemptValueChange(change)) {
					BlockDataValueType<?> valueType = tag.getValueType();
					assert valueType != null;
					rejected.computeIfAbsent(valueType, list -> new ArrayList<>()).add(tag.getKey());
					strings.remove(tag.getKey());
				}
			}
			String dataString = blockData.getMaterial().getKey() + "["
				+ StringUtils.join(Arrays.stream(tags).filter(tag -> tag.getRawValue() != null).toList(), ",") + "]";
			try {
				BlockData newData = Bukkit.createBlockData(dataString);
				//noinspection unchecked
				holder.setBlockData(object, mode == ChangeMode.SET ? blockData.merge(newData) : newData);
			} catch (Exception exception) {
				for (BlockDataTag tag : tags) {
					if (!tag.hasValidityCheck())
						continue;
					if (tag.checkValidity(blockData))
						continue;
					invalid.computeIfAbsent(tag.getValueType(), map -> new HashMap<>())
						.computeIfAbsent(tag, list -> new ArrayList<>())
						.add(blockData.getMaterial());
				}
			}
		}
		if (!rejected.isEmpty()) {
			List<String> messages = new ArrayList<>();
			for (Entry<BlockDataValueType<?>, List<String>> entry : rejected.entrySet()) {
				BlockDataValueType<?> valueType = entry.getKey();
				List<String> tags = entry.getValue();
				if (tags == null || tags.isEmpty())
					continue;
				String message = "The blockdata tag";
				if (tags.size() > 1)
					message += "s";
				String classInfoName = Classes.getSuperClassInfo(valueType.getTypeClass()).getName().toString();
				message += " '" + StringUtils.join(tags, ", ", ", and ") + "' can only be changed to " + Utils.a(classInfoName)
					+ " value.";
				messages.add(message);
			}
			if (!messages.isEmpty())
				error(StringUtils.join(messages, "\n\t "));
		}
		if (!invalid.isEmpty()) {
			List<String> messages = new ArrayList<>();
			for (Entry<BlockDataValueType<?>, Map<BlockDataTag, List<Material>>> entry : invalid.entrySet()) {
				BlockDataValueType<?> valueType = entry.getKey();
				Map<BlockDataTag, List<Material>> map = entry.getValue();
				String classInfoName = Classes.getSuperClassInfo(valueType.getTypeClass()).getName().toString();
				for (Entry<BlockDataTag, List<Material>> tagEntry : map.entrySet()) {
					BlockDataTag tag = tagEntry.getKey();
					List<Material> materials = tagEntry.getValue();
					String message = "The blockdata tag '" + tag.getKey() + "' does not support the " + classInfoName
						+ " value '" + tag.getConversionString() + "' for the block type" + (materials.size() > 1 ? "s" : "") + ": "
						+ StringUtils.join(materials.stream()
							.map(ItemType::new)
							.toList(), ", ", ", and ");
					messages.add(message);
				}
			}
			if (!messages.isEmpty())
				error(StringUtils.join(messages, "\n\t "));
		}
	}

	@Override
	public boolean isSingle() {
		return strings.isSingle() && objects.isSingle();
	}

	@Override
	public Class<?>[] possibleReturnTypes() {
		return BlockDataValueType.TYPE_CLASSES;
	}

	@Override
	public Class<?> getReturnType() {
		return Object.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("the blockdata tag", strings, "of", objects)
			.toString();
	}

}
