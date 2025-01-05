package org.skriptlang.skript.bukkit.recipes;

import ch.njol.skript.Skript;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent.*;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent.CookingRecipeEvent.BlastingRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent.CookingRecipeEvent.CampfireRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent.CookingRecipeEvent.FurnaceRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent.CookingRecipeEvent.SmokingRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent.CraftingRecipeEvent.ShapedRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent.CraftingRecipeEvent.ShapelessRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent.SmithingRecipeEvent.SmithingTransformRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.CreateRecipeEvent.SmithingRecipeEvent.SmithingTrimRecipeEvent;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCookingRecipe.MutableBlastingRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCookingRecipe.MutableCampfireRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCookingRecipe.MutableFurnaceRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCookingRecipe.MutableSmokingRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCraftingRecipe.MutableShapedRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableCraftingRecipe.MutableShapelessRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableSmithingRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableSmithingRecipe.MutableSmithingTransformRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableSmithingRecipe.MutableSmithingTrimRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableStonecuttingRecipe;
import org.skriptlang.skript.bukkit.recipes.MutableRecipe.MutableTransmuteRecipe;
import org.skriptlang.skript.bukkit.recipes.elements.ExprSecCreateRecipe;

import java.util.HashMap;
import java.util.Map;

/**
 * Utils used for getting data from {@link RecipeType}
 */
public class RecipeUtils {

	/**
	 * Enum for storing all types of Recipes (until Bukkit makes an enum or registry)
	 */
	public enum RecipeType {
		SHAPED(ShapedRecipe.class, ShapedRecipeEvent.class) {
			@Override
			public CreateRecipeEvent createRecipeEvent(NamespacedKey key) {
				return new ShapedRecipeEvent(key);
			}

			@Override
			public MutableRecipe createMutableRecipe(NamespacedKey key) {
				return new MutableShapedRecipe(key);
			}

		},
		SHAPELESS(ShapelessRecipe.class, ShapelessRecipeEvent.class) {
			@Override
			public CreateRecipeEvent createRecipeEvent(NamespacedKey key) {
				return new ShapelessRecipeEvent(key);
			}

			@Override
			public MutableRecipe createMutableRecipe(NamespacedKey key) {
				return new MutableShapelessRecipe(key);
			}

		},
		// TODO: Remove method and apply class directly when MC version is raised to 1.21.2+
		TRANSMUTE(getTransmuteRecipeClass(), TransmuteRecipeEvent.class) {
			@Override
			public CreateRecipeEvent createRecipeEvent(NamespacedKey key) {
				return new TransmuteRecipeEvent(key);
			}

			@Override
			public MutableRecipe createMutableRecipe(NamespacedKey key) {
				return new MutableTransmuteRecipe(key);
			}

		},
		// TODO: Remove method and apply class directly when MC version is raised to 1.20.1+
		// Having 'CRAFTING' under the subclasses allows for proper ExprRecipeType
		CRAFTING(getCraftingRecipeClass(), CraftingRecipeEvent.class) {
			// A 'CraftingRecipeEvent' should never be created
			@Override
			public @Nullable CreateRecipeEvent createRecipeEvent(NamespacedKey key) {
				return null;
			}
			// A 'MutableCraftingRecipe' should never be created
			@Override
			public @Nullable MutableRecipe createMutableRecipe(NamespacedKey key) {
				return null;
			}

		},
		BLASTING(BlastingRecipe.class, BlastingRecipeEvent.class) {
			@Override
			public CreateRecipeEvent createRecipeEvent(NamespacedKey key) {
				return new BlastingRecipeEvent(key);
			}

			@Override
			public MutableRecipe createMutableRecipe(NamespacedKey key) {
				return new MutableBlastingRecipe(key);
			}

		},
		FURNACE(FurnaceRecipe.class, FurnaceRecipeEvent.class) {
			@Override
			public CreateRecipeEvent createRecipeEvent(NamespacedKey key) {
				return new FurnaceRecipeEvent(key);
			}

			@Override
			public MutableRecipe createMutableRecipe(NamespacedKey key) {
				return new MutableFurnaceRecipe(key);
			}

		},
		CAMPFIRE(CampfireRecipe.class, CampfireRecipeEvent.class) {
			@Override
			public CreateRecipeEvent createRecipeEvent(NamespacedKey key) {
				return new CampfireRecipeEvent(key);
			}

			@Override
			public MutableRecipe createMutableRecipe(NamespacedKey key) {
				return new MutableCampfireRecipe(key);
			}

		},
		SMOKING(SmokingRecipe.class, SmokingRecipeEvent.class) {
			@Override
			public CreateRecipeEvent createRecipeEvent(NamespacedKey key) {
				return new SmokingRecipeEvent(key);
			}

			@Override
			public MutableRecipe createMutableRecipe(NamespacedKey key) {
				return new MutableSmokingRecipe(key);
			}

		},
		// Having 'COOKING' under the subclasses allows for proper ExprRecipeType
		COOKING(CookingRecipe.class, CookingRecipeEvent.class) {
			// A 'CookingRecipeEvent' should never be created
			@Override
			public @Nullable CreateRecipeEvent createRecipeEvent(NamespacedKey key) {
				return null;
			}
			// A 'MutableCookingRecipe' should never be created
			@Override
			public @Nullable MutableRecipe createMutableRecipe(NamespacedKey key) {
				return null;
			}

		},
		SMITHING_TRANSFORM(SmithingTransformRecipe.class, SmithingTransformRecipeEvent.class) {
			@Override
			public CreateRecipeEvent createRecipeEvent(NamespacedKey key) {
				return new SmithingTransformRecipeEvent(key);
			}

			@Override
			public MutableRecipe createMutableRecipe(NamespacedKey key) {
				return new MutableSmithingTransformRecipe(key);
			}

		},
		SMITHING_TRIM(SmithingTrimRecipe.class, SmithingTrimRecipeEvent.class) {
			@Override
			public CreateRecipeEvent createRecipeEvent(NamespacedKey key) {
				return new SmithingTrimRecipeEvent(key);
			}

			@Override
			public MutableRecipe createMutableRecipe(NamespacedKey key) {
				return new MutableSmithingTrimRecipe(key);
			}

		},
		// Having 'SMITHING' under the subclasses allows for proper ExprRecipeType
		SMITHING(SmithingRecipe.class, SmithingRecipeEvent.class) {
			// TODO: return null after minimum support version is raised to 1.20+
			@Override
			public CreateRecipeEvent createRecipeEvent(NamespacedKey key) {
				return new SmithingRecipeEvent(key, this);
			}
			// TODO: return null after minimum support version is raised to 1.20+
			@Override
			public MutableRecipe createMutableRecipe(NamespacedKey key) {
				return new MutableSmithingRecipe(key, this);
			}

		},
		STONECUTTING(StonecuttingRecipe.class, StonecuttingRecipeEvent.class) {
			@Override
			public CreateRecipeEvent createRecipeEvent(NamespacedKey key) {
				return new StonecuttingRecipeEvent(key);
			}

			@Override
			public MutableRecipe createMutableRecipe(NamespacedKey key) {
				return new MutableStonecuttingRecipe(key);
			}
		},
		COMPLEX(ComplexRecipe.class, null) {
			// A 'CompleRecipeEvent' does not exist
			@Override
			public @Nullable CreateRecipeEvent createRecipeEvent(NamespacedKey key) {
				return null;
			}
			// A 'MutableComplexRecipe' does not exist
			@Override
			public @Nullable MutableRecipe createMutableRecipe(NamespacedKey key) {
				return null;
			}
		};

		/**
		 * Create a {@link CreateRecipeEvent} used for {@link ExprSecCreateRecipe}
		 * @param key The key to create a recipe
		 * @return
		 */
		public abstract @Nullable CreateRecipeEvent createRecipeEvent(NamespacedKey key);

		/**
		 * Create a {@link MutableRecipe} designed to set data before final creation.
		 * Used for {@link ExprSecCreateRecipe}.
		 * @param key
		 * @return
		 */
		public abstract @Nullable MutableRecipe createMutableRecipe(NamespacedKey key);

		private final @Nullable Class<? extends Recipe> recipeClass;
		private final @Nullable Class<? extends Event> eventClass;

		RecipeType(@Nullable Class<? extends Recipe> recipeClass, @Nullable Class<? extends Event> eventClass) {
			this.recipeClass = recipeClass;
			this.eventClass = eventClass;
		}

		/**
		 * Gets the Bukkit recipe type class.
		 * @return Bukkit recipe class
		 */
		public @Nullable Class<? extends Recipe> getRecipeClass() {
			return recipeClass;
		}

		/**
		 * Gets the custom event used when creating a new recipe.
		 * @return Custom event class
		 */
		public @Nullable Class<? extends Event> getEventClass() {
			return eventClass;
		}

		// Due to 1.19 not having 'CraftingRecipe.class'
		private static @Nullable Class<? extends Recipe> getCraftingRecipeClass() {
			if (Skript.classExists("org.bukkit.inventory.CraftingRecipe"))
				return CraftingRecipe.class;
			return null;
		}

		private static @Nullable Class<? extends Recipe> getTransmuteRecipeClass() {
			if (Skript.classExists("org.bukkit.inventory.TransmuteRecipe"))
				return TransmuteRecipe.class;
			return null;
		}

	}

	private static final Map<Class<? extends Recipe>, RecipeType> recipeClassConverter = new HashMap<>();

	static {
		for (RecipeType recipeType : RecipeType.values()) {
			if (recipeType.recipeClass != null)
				recipeClassConverter.put(recipeType.recipeClass, recipeType);
		}
	}

	/**
	 * Gets {@link RecipeType} from provided recipe class.
	 *
	 * Note: MC versions below 1.20 converts Smithing Transform and Smithing Trim Recipes to a Smithing Recipe, causing the returned RecipeType to be Smithing.
	 * @param providedClass Bukkit recipe class
	 * @return Recipe Type
	 */
	public static @Nullable RecipeType getRecipeType(@NotNull Class<? extends Recipe> providedClass) {
		RecipeType recipeType = recipeClassConverter.get(providedClass);
		if (recipeType == null)
			recipeType = recipeClassConverter.get(providedClass.getSuperclass());
		return recipeType;
	}

	/**
	 * Gets {@link RecipeType} from provided recipe.
	 * @param providedRecipe Recipe
	 * @return Recipe Type
	 */
	public static @Nullable RecipeType getRecipeType(@NotNull Recipe providedRecipe) {
		return getRecipeType(providedRecipe.getClass());
	}

}
