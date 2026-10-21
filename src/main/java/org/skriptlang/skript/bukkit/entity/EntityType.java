package org.skriptlang.skript.bukkit.entity;

import ch.njol.skript.localization.Language;
import ch.njol.skript.util.Utils;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that represents x amount of an {@link EntityData}
 */
public class EntityType
	extends ch.njol.skript.entity.EntityType
	implements Cloneable, YggdrasilExtendedSerializable {

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

	private EntityData<?> entityData;
	private int entityAmount;

	/**
	 * Used for deserialization.
	 */
	@ApiStatus.Internal
	public EntityType() { }

	/**
	 * Constructs a new {@link EntityType}.
	 * @param entityData The corresponding {@link EntityData}.
	 * @param amount The amount of {@code data}.
	 */
	public EntityType(EntityData<?> entityData, int amount) {
		assert entityData != null;
		this.entityData = entityData;
		this.entityAmount = amount;
		copyToLegacyFields();
	}

	/**
	 * Constructs a new {@link EntityType}.
	 * @param other The other {@link EntityType} to copy from.
	 */
	private EntityType(EntityType other) {
		entityAmount = other.entityAmount;
		entityData = other.entityData;
		copyToLegacyFields();
	}

	/**
	 * Whether {@code entity} is instance of {@code this}.
	 * @param entity The {@link Entity} to check.
	 * @return {@code true} if instance of, otherwise {@code false}.
	 * @see EntityData#isInstance(Entity)
	 */
	public boolean isInstance(Entity entity) {
		return entityData.isInstance(entity);
	}

	@Override
	public String toString() {
		if (getAmount() == 1)
			return entityData.toString(0);
		return entityAmount + " " + entityData.toString(Language.F_PLURAL);
	}

	/**
	 * Returns the string representation of {@code this}.
	 * @param flags The flags to determine singular or plurality.
	 * @return The string representation.
	 */
	public String toString(int flags) {
		if (getAmount() == 1)
			return entityData.toString(flags);
		return entityAmount + " " + entityData.toString(flags | Language.F_PLURAL);
	}

	/**
	 * @return The amount of {@code this} corresponds to with {@link #getData()}.
	 */
	public int getAmount() {
		return entityAmount == -1 ? 1 : entityAmount;
	}

	/**
	 * Sets the amount {@code this} corresponds to with {@link #getData()}.
	 * @param entityAmount The new amount.
	 */
	public void setAmount(int entityAmount) {
		this.entityAmount = entityAmount;
		copyToLegacyFields();
	}

	/**
	 * @return The {@link EntityData} {@code this} corresponds to.
	 */
	public EntityData<?> getData() {
		return entityData;
	}

	/**
	 * Whether {@code other} has the same {@link EntityData}.
	 * @param other The other {@link EntityType}.
	 * @return {@code true} if the {@link EntityData}s are the same, otherwise {@code false}.
	 * @see EntityData#equals(Object)
	 */
	public boolean sameType(EntityType other) {
		return entityData.equals(other.entityData);
	}

	@Override
	public EntityType clone() {
		return new EntityType(this);
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + entityAmount;
		result = prime * result + entityData.hashCode();
		return result;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof EntityType other))
			return false;
		return entityAmount == other.entityAmount && entityData.equals(other.entityData);
	}

	@Override
	public Fields serialize() throws NotSerializableException {
		Fields fields = new Fields();
		fields.putObject("entityData", entityData);
		fields.putPrimitive("entityAmount", entityAmount);
		return fields;
	}

	@Override
	public void deserialize(@NotNull Fields fields) throws StreamCorruptedException, NotSerializableException {
		// legacy handling
		if (fields.hasField("data")) {
			fields.putObject("entityData", fields.getObject("data"));
		}
		if (fields.hasField("amount")) {
			fields.putPrimitive("entityAmount", fields.getPrimitive("amount"));
		}

		this.entityData = fields.getObject("entityData", EntityData.class);
		this.entityAmount = fields.getPrimitive("entityAmount", int.class);
		copyToLegacyFields();
	}

	@SuppressWarnings("removal")
	private void copyToLegacyFields() {
		super.data = this.entityData;
		super.amount = this.entityAmount;
	}

}
