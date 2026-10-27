package org.skriptlang.skript.bukkit.block.blockdata;

import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Container for holding the key, raw value, and typed value object of a {@link BlockData} tag.
 * @see BlockDataValueType for typed value objects other than a string.
 */
public class BlockDataTag {

	private static final Pattern BLOCKDATA_PATTERN = Pattern.compile(".*\\[(?<tags>(?:.+=.+,?)+)?]");

	/**
	 * Gets a {@link BlockDataTag} matching {@code key} from {@code blockData}.
	 * @param blockData The {@link BlockData} to get the {@link BlockDataTag} from.
	 * @param key The key of the tag to retrieve.
	 * @return The resulting {@link BlockDataTag} if found, otherwise {@code null}.
	 */
	public static @Nullable BlockDataTag of(BlockData blockData, String key) {
		BlockDataTag[] tags = of(blockData);
		if (tags == null)
			return null;
		for (BlockDataTag tag : tags) {
			if (tag.key.equals(key))
				return tag;
		}
		return null;
	}

	/**
	 * Gets all {@link BlockDataTag}s matching any of the {@code keys} from {@code blockData}.
	 * @param blockData The {@link BlockData} to get the {@link BlockDataTag}s from.
	 * @param keys The keys of the tags to retrieve.
	 * @return The resulting {@link BlockDataTag}s if any are found, otherwise {@code null}.
	 */
	public static BlockDataTag @Nullable [] of(BlockData blockData, Collection<String> keys) {
		return of(blockData, keys.toArray(String[]::new));
	}

	/**
	 * Gets all {@link BlockDataTag}s matching any of the {@code keys} from {@code blockData}.
	 * @param blockData The {@link BlockData} to get the {@link BlockDataTag}s from.
	 * @param keys The keys of the tags to retrieve.
	 * @return The resulting {@link BlockDataTag}s if any are found, otherwise {@code null}.
	 */
	public static BlockDataTag @Nullable [] of(BlockData blockData, String... keys) {
		BlockDataTag[] tags = of(blockData);
		if (tags == null)
			return null;
		return Arrays.stream(tags)
			.filter(tag -> Arrays.stream(keys).anyMatch(key -> key.equalsIgnoreCase(tag.getKey())))
			.toArray(BlockDataTag[]::new);
	}

	/**
	 * Gets all {@link BlockDataTag}s from {@code blockData}.
	 * @param blockData The {@link BlockData} to get the {@link BlockDataTag}s from.
	 * @return The resulting {@link BlockDataTag}s if any exist, otherwise {@code null}.
	 */
	public static BlockDataTag @Nullable [] of(BlockData blockData) {
		String[] tags = getTags(blockData);
		if (tags == null || tags.length == 0)
			return null;
		List<BlockDataTag> dataTags = new ArrayList<>();
		for (String tag : tags) {
			String[] split = tag.split("=");
			assert split.length >= 2;
			dataTags.add(new BlockDataTag(split[0], split[1]));
		}
		return dataTags.toArray(BlockDataTag[]::new);
	}

	/**
	 * Gets all the tags of {@code blockData}.
	 * @param blockData The {@link BlockData} to get the tags from.
	 * @return The resulting {@link String} array of the tags in the form of "key=value".
	 */
	private static String @Nullable [] getTags(BlockData blockData) {
		String dataString = blockData.getAsString(false);
		Matcher matcher = BLOCKDATA_PATTERN.matcher(dataString);
		if (!matcher.matches())
			return null;
		String tagGroup = matcher.group("tags");
		if (tagGroup == null || tagGroup.isBlank())
			return null;
		return tagGroup.split(",");
	}

	private final String key;
	private String rawValue;
	private Object value;
	private BlockDataValueType valueType = BlockDataValueType.STRING;

	/**
	 * Construct a new {@link BlockDataTag} with the data from a {@link BlockData}'s tag.
	 * @param key The key of a {@link BlockData} tag.
	 * @param rawValue The raw/string value of a {@link BlockData} tag.
	 */
	public BlockDataTag(String key, String rawValue) {
		this.key = key.toLowerCase(Locale.ENGLISH);
		this.rawValue = rawValue;
		this.value = rawValue;
		setValueType();
	}

	/**
	 * Checks if the {@link #rawValue} can be parsed as one of the {@link BlockDataValueType}s.
	 */
	private void setValueType() {
		if (rawValue == null)
			return;
		for (BlockDataValueType<?> type : BlockDataValueType.TYPES) {
			if (type == BlockDataValueType.STRING)
				continue;
			Object newValue = type.parse(rawValue);
			if (newValue != null) {
				value = newValue;
				valueType = type;
				return;
			}
		}
	}

	/**
	 * @return The key of the {@link BlockData} tag used to construct {@code this}.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return The raw/string value used for {@link #key}.
	 */
	public String getRawValue() {
		return rawValue;
	}

	/**
	 * @return The value for {@link #key}. Can be a {@link String} or any of the types in {@link BlockDataValueType}.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return The {@link BlockDataValueType} if the {@link #rawValue} successfully parsed, otherwise {@code null}.
	 */
	public @Nullable BlockDataValueType<?> getValueType() {
		return valueType;
	}

	/**
	 * Attempts to change the value of {@code this} by ensuring {@code value} can be parsed with {@link #valueType} if not {@code null}.
	 * @param value The value to change to.
	 * @return {@code true} if the change was successful, otherwise {@code false}.
	 */
	public boolean attemptValueChange(@Nullable Object value) {
		// Regardless of type, value can be changed to null
		if (value == null) {
			this.value = null;
			this.rawValue = null;
			return true;
		}
		// Parses the value as the intended type
		Object newValue = valueType.parse(value);
		if (newValue != null) {
			this.value = newValue;
			this.rawValue = newValue.toString();
			return true;
		}
		return false;
	}

	/**
	 * Whether the {@link #valueType} of {@code this} can be checked to ensure a value is valid.
	 * @return {@code true} if can be checked, otherwise {@code false}.
	 */
	public boolean hasValidityCheck() {
		return valueType.hasValidityCheck();
	}

	/**
	 * Checks the validity of {@link #value} to ensure {@code blockData} supports it.
	 * @param blockData The {@link BlockData} to check if it supports {@link #value}.
	 * @return {@code true} if it's supported, otherwise {@code false}.
	 */
	public boolean checkValidity(BlockData blockData) {
		String dataString = blockData.getMaterial().getKey() + "[" + this + "]";
		try {
			Bukkit.createBlockData(dataString);
			return true;
		} catch (Exception ignored) {}
		return false;
	}

	/**
	 * @return {@link #rawValue} or string representation of the converted value defined by {@link BlockDataValueType#toStringConversion(Object)}.
	 */
	public String getConversionString() {
		if (rawValue == null)
			return "";
		if (valueType.requiresConversion()) {
			//noinspection unchecked
			return valueType.toStringConversion(value);
		}
		return rawValue;
	}

	@Override
	public int hashCode() {
		return valueType.hashCode() + key.hashCode() + value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BlockDataTag other))
			return false;
		return valueType == other.valueType && key.equalsIgnoreCase(other.key) && value.equals(other.value);
	}

	@Override
	public String toString() {
		if (rawValue == null)
			return "";
		return key + "=" + getConversionString();
	}

}
