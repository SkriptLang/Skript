package org.skriptlang.skript.bukkit.potion.providers;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Utils;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.ApiStatus;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprPotionDuration;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * A wrapper for working with objects that provide potion effects.
 * @param <T> The type providing potion effects.
 * @see #of(Object, Consumer)
 */
@ApiStatus.Internal
public abstract class PotionEffectProvider<T> {

	/**
	 * @param object The object to obtain a provider from.
	 * @param errorProducer A consumer to use for printing errors.
	 * @return A provider wrapping {@code object}.
	 *  If {@code object} is not a known potion provider, {@code errorProducer} is invoked
	 *   and a dummy provider is returned.
	 */
	public static PotionEffectProvider<?> of(Object object, Consumer<String> errorProducer) {
		return switch (object) {
			case AreaEffectCloud areaEffectCloud -> new EntityProvider(areaEffectCloud);
			case Arrow arrow -> new EntityProvider(arrow);
			case ItemType itemType -> new ItemTypeProvider(itemType);
			case LivingEntity livingEntity -> new LivingEntityProvider(livingEntity);
			default -> {
				errorProducer.accept(Utils.A(Classes.toString(object)) + " does not have potion effects");
				yield new NullProvider();
			}
		};
	}

	/**
	 * Describes the type of potion effects being retrieved in a retrieval operation.
	 * This is used for distinguishing between active and hidden potion effects, which only apply to {@link LivingEntity}.
	 */
	public enum RetrievalState {

		/**
		 * Unspecified retrieval state.
		 * Behavior will differ depending on retrieval context.
		 */
		UNSET,

		/**
		 * Obtaining only active effects
		 */
		ACTIVE,

		/**
		 * Obtaining only hidden effects
		 */
		HIDDEN,

		/**
		 * Obtaining both active and hidden effects.
		 * A combination of {@link #ACTIVE} and {@link #HIDDEN}.
		 */
		BOTH;

		/**
		 * Utility method for standardized mapping of a parse tag to a retrieval state.
		 * @param tag Parse tag representing a retrieval state.
		 * @return A retrieval state.
		 */
		public static RetrievalState fromParseTag(String tag) {
			return switch (tag) {
				case "active" -> ACTIVE; // explicitly active
				case "hidden" -> HIDDEN; // explicitly hidden
				case "both" -> BOTH; // explicitly active and hidden
				default -> UNSET; // implicitly active for get, implicitly active and hidden for delete/reset
			};
		}

		/**
		 * @return Whether this state includes active potion effects.
		 */
		public boolean includesActive() {
			return this != RetrievalState.HIDDEN;
		}

		/**
		 * @return Whether this state includes hidden potion effects.
		 */
		public boolean includesHidden() {
			return this == RetrievalState.HIDDEN || this == RetrievalState.BOTH;
		}

		/**
		 * @return A user-friendly string describing this retrieval state.
		 */
		public String displayName() {
			return switch (this) {
				case UNSET -> "";
				case ACTIVE -> "active";
				case HIDDEN -> "hidden";
				case BOTH -> "active and hidden";
			};
		}
	}

	/**
	 * Source object providing potion effects.
	 */
	protected final T source;

	/**
	 * @param source The source object providing potion effects.
	 */
	public PotionEffectProvider(T source) {
		this.source = source;
	}

	/**
	 * Obtains specific types of potion effects from this provider.
	 * @param potionEffectTypes The type of potion effects to obtain.
	 * @param state The type of retrieval to perform.
	 * @return All potion effects of {@code potionEffectTypes} present on this provider.
	 */
	public abstract Collection<SkriptPotionEffect> get(PotionEffectType[] potionEffectTypes, RetrievalState state);

	/**
	 * Obtains all potion effects from this provider.
	 * @param state The type of retrieval to perform.
	 * @return All potion effects present on this provider.
	 */
	public abstract Collection<SkriptPotionEffect> getAll(RetrievalState state);

	/**
	 * Adds a potion effect to this provider.
	 * @param potionEffect The potion effect to add.
	 */
	public abstract void add(PotionEffect potionEffect);

	/**
	 * Removes a potion effect from this provider.
	 * A potion effect only needs to match the qualities of {@code potionEffect} to be removed.
	 * Thus, an effect will be removed even if it has other, distinguishing qualities.
	 * @param potionEffect The potion effect to remove.
	 * @param state State determining whether certain types of potion effects should be ignored.
	 */
	public abstract void remove(SkriptPotionEffect potionEffect, RetrievalState state);

	/**
	 * Removes all potion effects of a specific type from this provider.
	 * @param potionEffectType The type of potion effect to remove.
	 * @param state State determining whether certain types of potion effects should be preserved.
	 */
	public abstract void removeAll(PotionEffectType potionEffectType, RetrievalState state);

	/**
	 * Clears all potion effects of {@code potionEffectTypes} from this provider.
	 * @param potionEffectTypes The types of potion effects to clear.
	 * @param state State determining whether certain types of potion effects should be ignored.
	 */
	public abstract void clear(PotionEffectType[] potionEffectTypes, RetrievalState state);

	/**
	 * Clears all potion effects from this provider.
	 * @param state State determining whether certain types of potion effects should be preserved.
	 */
	public abstract void clearAll(RetrievalState state);

	/**
	 * Modifies properties of existing potion effects on this provider.
	 * @param types The types of potion effects to modify.
	 * @param state State determining whether certain types of potion effects should be ignored.
	 * @param delta The change values.
	 * @param mode The type of modification to perform.
	 */
	public void modify(PotionEffectType[] types, RetrievalState state, Object[] delta, ChangeMode mode) {
		if (delta[0] instanceof Timespan timespan) {
			get(types, state).forEach(potionEffect ->
				ExprPotionDuration.changeSafe(potionEffect, timespan, mode));
		}
	}

	/**
	 * Used for mirroring modifications of a potion effect actively applied to this provider.
	 * @param potionEffect The potion effect being modified.
	 * @param runnable A runnable that applies the modification(s) to {@code potionEffect} when invoked.
	 */
	public abstract void mirrorEffectChanges(SkriptPotionEffect potionEffect, Runnable runnable);

}
