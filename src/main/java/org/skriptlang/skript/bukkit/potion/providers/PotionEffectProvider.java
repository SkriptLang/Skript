package org.skriptlang.skript.bukkit.potion.providers;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.util.Timespan;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.ApiStatus;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprPotionDuration;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;

import java.util.Collection;

@ApiStatus.Internal
public abstract class PotionEffectProvider<T> {

	public static PotionEffectProvider<?> of(Object object) {
		return switch (object) {
			case ItemType itemType -> new ItemTypeProvider(itemType);
			case LivingEntity livingEntity -> new LivingEntityProvider(livingEntity);
			default -> throw new IllegalArgumentException("Unsupported type: " + object.getClass());
		};
	}

	public enum RetrievalState {

		UNSET,
		ACTIVE,
		HIDDEN,
		BOTH;

		public static RetrievalState fromParseTag(String tag) {
			return switch (tag) {
				case "active" -> ACTIVE; // explicitly active
				case "hidden" -> HIDDEN; // explicitly hidden
				case "both" -> BOTH; // explicitly active and hidden
				default -> UNSET; // implicitly active for get, implicitly active and hidden for delete/reset
			};
		}

		public boolean includesActive() {
			return this != RetrievalState.HIDDEN;
		}

		public boolean includesHidden() {
			return this == RetrievalState.HIDDEN || this == RetrievalState.BOTH;
		}

		public String displayName() {
			return switch (this) {
				case UNSET -> "";
				case ACTIVE -> "active";
				case HIDDEN -> "hidden";
				case BOTH -> "active and hidden";
			};
		}
	}

	protected final T source;

	public PotionEffectProvider(T source) {
		this.source = source;
	}

	public final T source() {
		return source;
	}

	public abstract Collection<SkriptPotionEffect> get(PotionEffectType[] potionEffectTypes, RetrievalState state);

	public abstract Collection<SkriptPotionEffect> getAll(RetrievalState state);

	public abstract void add(PotionEffect potionEffect);

	public abstract void remove(SkriptPotionEffect potionEffect, RetrievalState state);

	public abstract void removeAll(PotionEffectType potionEffectType);

	public abstract void clear(PotionEffectType[] potionEffectTypes, RetrievalState state);

	public abstract void clearAll(RetrievalState state);

	public void modify(PotionEffectType[] types, RetrievalState state, Object[] delta, ChangeMode mode) {
		if (delta[0] instanceof Timespan timespan) {
			get(types, state).forEach(potionEffect ->
				ExprPotionDuration.changeSafe(potionEffect, timespan, mode));
		}
	}

}
