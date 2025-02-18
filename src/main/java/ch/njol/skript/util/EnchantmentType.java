package ch.njol.skript.util;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.YggdrasilSerializable;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * @author Peter Güttinger
 */
public class EnchantmentType implements YggdrasilSerializable {

	private static @Nullable Parser<Enchantment> ENCHANTMENT_PARSER = null;
	private final Enchantment type;
	private final int level;

	/**
	 * Used for deserialisation only
	 */
	@SuppressWarnings({"unused", "null"})
	private EnchantmentType() {
		type = null;
		level = -1;
	}

	public EnchantmentType(final Enchantment type) {
		assert type != null;
		this.type = type;
		this.level = -1;
	}

	public EnchantmentType(final Enchantment type, final int level) {
		assert type != null;
		this.type = type;
		this.level = level;
	}

	/**
	 * @return level or 1 if level == -1
	 */
	public int getLevel() {
		return level == -1 ? 1 : level;
	}

	/**
	 * @return the internal level, can be -1
	 */
	public int getInternalLevel() {
		return level;
	}

	@Nullable
	public Enchantment getType() {
		return type;
	}

	/**
	 * Checks whether the given item type has this enchantment.
	 *
	 * @param item the item to be checked.
	 * @deprecated Use {@link ItemType#hasEnchantments(Enchantment...)}
	 */
	@Deprecated
	public boolean has(final ItemType item) {
		return item.hasEnchantments(type);
	}

	@Override
	public String toString() {
		return getEnchantmentParser().toString(type, 0) + (level == -1 ? "" : " " + level);
	}

	@SuppressWarnings("null")
	private final static Pattern pattern = Pattern.compile(".+ \\d+");

	/**
	 * Parses an enchantment type from string. This includes an {@link Enchantment}
	 * and its level.
	 *
	 * @param s String to parse.
	 * @return Enchantment type, or null if parsing failed.
	 */
	@Nullable
	public static EnchantmentType parse(final String s) {
		Parser<Enchantment> enchantmentParser = getEnchantmentParser();
		if (pattern.matcher(s).matches()) {
			String name = s.substring(0, s.lastIndexOf(' '));
			assert name != null;
			final Enchantment ench = enchantmentParser.parse(name, ParseContext.DEFAULT);
			if (ench == null)
				return null;
			String level = s.substring(s.lastIndexOf(' ') + 1);
			assert level != null;
			return new EnchantmentType(ench, Utils.parseInt(level));
		}
		final Enchantment ench = enchantmentParser.parse(s, ParseContext.DEFAULT);
		if (ench == null)
			return null;
		return new EnchantmentType(ench, -1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + level;
		result = prime * result + type.hashCode();
		return result;
	}

	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof EnchantmentType))
			return false;
		final EnchantmentType other = (EnchantmentType) obj;
		if (level != other.level)
			return false;
		return type.equals(other.type);
	}

	@SuppressWarnings("unchecked")
	private static Parser<Enchantment> getEnchantmentParser() {
		if (ENCHANTMENT_PARSER == null) {
			ClassInfo<Enchantment> classInfo = Classes.getExactClassInfo(Enchantment.class);
			if (classInfo == null) {
				throw new IllegalStateException("Enchantment ClassInfo not found");
			}
			ENCHANTMENT_PARSER = (Parser<Enchantment>) classInfo.getParser();
			if (ENCHANTMENT_PARSER == null) {
				throw new IllegalStateException("Enchantment parser not found");
			}
		}
		return ENCHANTMENT_PARSER;
	}

}
