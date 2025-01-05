package org.skriptlang.skript.bukkit.recipes;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.*;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCookingRecipe.MutableBlastingRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCookingRecipe.MutableCampfireRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCookingRecipe.MutableFurnaceRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCookingRecipe.MutableSmokingRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCraftingRecipe.MutableShapedRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCraftingRecipe.MutableShapelessRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableSmithingRecipe.MutableSmithingTransformRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableSmithingRecipe.MutableSmithingTrimRecipe;
import org.skriptlang.skript.bukkit.recipes.RecipeUtils.RecipeType;

/**
 * Event class used with ExprSecCreateRecipe to allow the creation of MutableRecipe
 * Allows elements used to determine if the recipe being created is of the required type
 */
public class CreateRecipeEvent extends Event {
	private boolean errorInSection = false;
	private final RecipeType recipeType;
	private final MutableRecipe mutableRecipe;

	public CreateRecipeEvent(NamespacedKey key, RecipeType recipeType) {
		this.recipeType = recipeType;
		this.mutableRecipe = recipeType.createMutableRecipe(key);
	}

	public void setErrorInSection() {
		this.errorInSection = true;
	}

	public boolean getErrorInSection() {
		return errorInSection;
	}

	public RecipeType getRecipeType() {
		return recipeType;
	}

	public MutableRecipe getMutableRecipe() {
		return mutableRecipe;
	}

	/**
	 * Event correlating to creating a {@link MutableCraftingRecipe}
	 */
	public static class CraftingRecipeEvent extends CreateRecipeEvent {

		public CraftingRecipeEvent(NamespacedKey key, RecipeType recipeType) {
			super(key, recipeType);
		};

		/**
		 * Event correlating to creating a {@link MutableShapedRecipe}
		 */
		public static class ShapedRecipeEvent extends CraftingRecipeEvent {
			public ShapedRecipeEvent(NamespacedKey key) {
				super(key, RecipeType.SHAPED);
			};
		}

		/**
		 * Event correlating to creating a {@link MutableShapelessRecipe}
		 */
		public static class ShapelessRecipeEvent extends CraftingRecipeEvent {
			public ShapelessRecipeEvent(NamespacedKey key) {
				super(key, RecipeType.SHAPELESS);
			};
		}
	}

	/**
	 * Event correlating to creating a {@link MutableCookingRecipe}
	 */
	public static class CookingRecipeEvent extends CreateRecipeEvent {

		public CookingRecipeEvent(NamespacedKey key, RecipeType recipeType) {
			super(key, recipeType);
		};

		/**
		 * Event correlating to creating a {@link MutableBlastingRecipe}
		 */
		public static class BlastingRecipeEvent extends CookingRecipeEvent {
			public BlastingRecipeEvent(NamespacedKey key) {
				super(key, RecipeType.BLASTING);
			}
		}

		/**
		 * Event correlating to creating a {@link MutableCampfireRecipe}
		 */
		public static class CampfireRecipeEvent extends CookingRecipeEvent {
			public CampfireRecipeEvent(NamespacedKey key) {
				super(key, RecipeType.CAMPFIRE);
			}
		}

		/**
		 * Event correlating to creating a {@link MutableFurnaceRecipe}
		 */
		public static class FurnaceRecipeEvent extends CookingRecipeEvent {
			public FurnaceRecipeEvent(NamespacedKey key) {
				super(key, RecipeType.FURNACE);
			}
		}

		/**
		 * Event correlating to creating a {@link MutableSmokingRecipe}
		 */
		public static class SmokingRecipeEvent extends CookingRecipeEvent {
			public SmokingRecipeEvent(NamespacedKey key) {
				super(key, RecipeType.SMOKING);
			}
		}
	}

	/**
	 * Event correlating to creating a {@link MutableSmithingRecipe}
	 */
	public static class SmithingRecipeEvent extends CreateRecipeEvent {
		public SmithingRecipeEvent(NamespacedKey key, RecipeType recipeType) {
			super(key, recipeType);
		}

		/**
		 * Event correlating to creating a {@link MutableSmithingTransformRecipe}
		 */
		public static class SmithingTransformRecipeEvent extends SmithingRecipeEvent {
			public SmithingTransformRecipeEvent(NamespacedKey key) {
				super(key, RecipeType.SMITHING_TRANSFORM);
			}
		}

		/**
		 * Event correlating to creating a {@link MutableSmithingTrimRecipe}
		 */
		public static class SmithingTrimRecipeEvent extends SmithingRecipeEvent {
			public SmithingTrimRecipeEvent(NamespacedKey key) {
				super(key, RecipeType.SMITHING_TRIM);
			}
		}
	}

	/**
	 * Event correlating to creating a {@link MutableStonecuttingRecipe}
	 */
	public static class StonecuttingRecipeEvent extends CreateRecipeEvent {
		public StonecuttingRecipeEvent(NamespacedKey key) {
			super(key, RecipeType.STONECUTTING);
		}
	}

	/**
	 * Event correlating to creating a {@link MutableTransmuteRecipe}
	 */
	public static class TransmuteRecipeEvent extends CreateRecipeEvent {
		public TransmuteRecipeEvent(NamespacedKey key) {
			super(key, RecipeType.TRANSMUTE);
		}
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		throw new IllegalStateException();
	}

}
