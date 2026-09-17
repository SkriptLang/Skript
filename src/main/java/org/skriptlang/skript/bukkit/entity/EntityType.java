package org.skriptlang.skript.bukkit.entity;

import ch.njol.skript.localization.Language;
import ch.njol.skript.util.Utils;
import ch.njol.yggdrasil.YggdrasilSerializable;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that represents x amount of an {@link EntityData}
 */
public class EntityType
	extends ch.njol.skript.entity.EntityType
	implements Cloneable, YggdrasilSerializable {

	private static final Pattern PARSE_PATTERN = Pattern.compile("(?i)((?<article>an?)|(?<amount>\\d+))? *(?<data>.+)");

	/**
	 * Parses the provided {@code string} into a {@link EntityData} and stores in a new {@link EntityType}.
	 * <p>
	 *     The {@code string} allows formats of "number + entity" and "article + entity",
	 *     but never "article + number + entity" or "number + article + entity".
	 *     Allowed:
	 *     		- 1 zombie, 3 pigs, 5 endermen
	 *     		- a zombie, a pig, an endermen
	 *     Not-Allowed:
	 *     		- a 1 zombie, a 3 pigs, an 5 endermen
	 *     		- 1 a zombie, 3 a pigs, 5 an endermen
	 * </p>
	 * @param string The {@link String} to parse.
	 * @return {@link EntityType} containing the parsed {@link EntityData} and provided amount if present in {@code string}
	 * 			or -1 if not present. Otherwise {@code null} if the parse failed.
	 */
	public static @Nullable EntityType parse(String string) {
		assert string != null && !string.isEmpty();
		Matcher matcher = PARSE_PATTERN.matcher(string);
		if (!matcher.matches()) {
			// Should always match group 'data'
			return null;
		}
		int amount = -1;
		String amountGroup = matcher.group("amount");
		if (amountGroup != null)
			amount = Utils.parseInt(amountGroup);
		string = matcher.group("data").trim();
		EntityData<?> data = EntityData.parseWithoutIndefiniteArticle(string);
		if (data == null)
			return null;
		return new EntityType(data, amount);
	}

	private int amount = -1;

	private EntityData<?> data;

	/**
	 * Only used for deserialisation
	 */
	@SuppressWarnings({"unused", "null"})
	public EntityType() {
		super();
		data = null;
		amount = 1;
	}

	/**
	 * Constructs a new {@link EntityType}.
	 * @param data The corresponding {@link EntityData}.
	 * @param amount The amount of {@code data}.
	 */
	public EntityType(EntityData<?> data, int amount) {
		super();
		assert data != null;
		this.data = data;
		this.amount = amount;
	}

	/**
	 * Constructs a new {@link EntityType}.
	 * @param other The other {@link EntityType} to copy from.
	 */
	private EntityType(EntityType other) {
		amount = other.amount;
		data = other.data;
	}

	/**
	 * Whether {@code entity} is instance of {@code this}.
	 * @param entity The {@link Entity} to check.
	 * @return {@code true} if instance of, otherwise {@code false}.
	 * @see EntityData#isInstance(Entity)
	 */
	public boolean isInstance(Entity entity) {
		return data.isInstance(entity);
	}

	@Override
	public String toString() {
		if (getAmount() == 1)
			return data.toString(0);
		return amount + " " + data.toString(Language.F_PLURAL);
	}

	/**
	 * Returns the string representation of {@code this}.
	 * @param flags The flags to determine singular or plurality.
	 * @return The string representation.
	 */
	public String toString(int flags) {
		if (getAmount() == 1)
			return data.toString(flags);
		return amount + " " + data.toString(flags | Language.F_PLURAL);
	}

	/**
	 * @return The amount of {@code this} corresponds to with {@link #data}.
	 */
	public int getAmount() {
		return amount == -1 ? 1 : amount;
	}

	/**
	 * Sets the amount {@code this} corresponds to with {@link #data}.
	 * @param amount The new amount.
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}

	/**
	 * @return The {@link EntityData} {@code this} corresponds to.
	 */
	public EntityData<?> getData() {
		return data;
	}

	/**
	 * Whether {@code other} has the same {@link EntityData}.
	 * @param other The other {@link EntityType}.
	 * @return {@code true} if the {@link EntityData}s are the same, otherwise {@code false}.
	 * @see EntityData#equals(Object)
	 */
	public boolean sameType(EntityType other) {
		return data.equals(other.data);
	}

	@Override
	public EntityType clone() {
		return new EntityType(this);
	}
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + amount;
		result = prime * result + data.hashCode();
		return result;
	}
	
	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof EntityType other))
			return false;
		return amount == other.amount && data.equals(other.data);
	}
	
}
