package ch.njol.skript.bukkitutil;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BlockDataUtils {

	/**
	 * Returns a valid block Material based on a given ItemStack, or null if one doesn't exist.
	 *
	 * @param item the item to convert
	 * @return the corresponding block Material, or null if one doesn't exist
	 */
	public static @Nullable Material toBlock(ItemStack item) {
		Material material = item.getType();
		if (material.isBlock()) // already a block
			return material;

		return switch (material) {
			case WHEAT_SEEDS -> Material.WHEAT;
			case POTATO -> Material.POTATOES;
			case CARROT -> Material.CARROTS;
			case BEETROOT_SEEDS -> Material.BEETROOTS;
			case PUMPKIN_SEEDS -> Material.PUMPKIN_STEM;
			case MELON_SEEDS -> Material.MELON_STEM;
			case SWEET_BERRIES -> Material.SWEET_BERRY_BUSH;
			default -> null;
		};
	}

	/**
	 * Gets the tags and values of a given blockdata as an array of strings, in the form of "tag=value", or null
	 * if the blockdata has none.
	 *
	 * @param data the blockdata to get the tags and values of
	 * @return the tags and values, as an array of strings, or null if none exist
	 */
	public static String @Nullable [] getTagsAndValues(BlockData data) {
		String string = data.getAsString();
		int start = string.indexOf("[");
		int end = string.indexOf("]");

		return (start == -1 || end == -1 || start >= end)
			? null
			: string.substring(start + 1, end).split(",");
	}

	/**
	 * Gets just the tags of a given blockdata as an array of strings, or null if the blockdata has none.
	 *
	 * @param data the blockdata to get the tags of
	 * @return the tags, as an array of strings, or null if none exist
	 */
	public static String @Nullable [] getTags(BlockData data) {
		// should this replace "_" in the tags for a more skripty feel?
		// and then obviously adjust logic elsewhere to replace " " back to "_"?
		String[] tagsAndValues = getTagsAndValues(data);
		if (tagsAndValues == null)
			return null;

		String[] tags = new String[tagsAndValues.length];
		for (int i = 0; i < tagsAndValues.length; i++) {
			tags[i] = tagsAndValues[i].split("=")[0];
		}
		return tags;
	}

	/**
	 * Gets the value of a specific tag on a given blockdata, or null if the tag isn't present.
	 * This will return a(n):
	 * 		boolean if the value is 'true' or 'false'
	 * 		integer if the value is parseable via Integer#parseInt(String)
	 * 		string 	if the value is not parsed as a boolean or integer
	 *
	 * @param data 	the blockdata to get the value of the tag from
	 * @param tag 	the tag to get the value of
	 * @return		the potentially parsed value of the tag, or null if it isn't present
	 */
	public static @Nullable Object getValue(BlockData data, String tag) {
		String[] tagsAndValues = getTagsAndValues(data);
		if (tagsAndValues == null)
			return null;

		for (String tagAndValue : tagsAndValues) {
			String[] split = tagAndValue.split("=");
			if (split.length == 2 && split[0].equals(tag)) {
				String value = split[1];
				if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
					return Boolean.parseBoolean(value);

				if (value.matches("\\d+")) {
					try {
						return Integer.parseInt(value);
					} catch (NumberFormatException ignored) {}
				}

				return value;
			}
		}

		return null;
	}

	public static BlockData setValue(BlockData data, String tag, Object value) {
		if (value.toString().matches("[a-zA-Z0-9]+") || !data.getAsString().contains("["))
			return data;

		String newBlockData = data.getMaterial().getKey() + "[" + value + "]";
		try {
			return data.merge(Bukkit.createBlockData(newBlockData)); //potentially invalid value
		} catch (IllegalArgumentException ignored) {
			return data;
		}
	}

}
