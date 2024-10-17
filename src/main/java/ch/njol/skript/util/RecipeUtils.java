package ch.njol.skript.util;

import ch.njol.skript.Skript;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecipeUtils {

	public enum RecipeType {
		SHAPED(ShapedRecipe.class, RegisterRecipeEvent.CraftingRecipeEvent.ShapedRecipeEvent.class),
		SHAPELESS(ShapelessRecipe.class, RegisterRecipeEvent.CraftingRecipeEvent.ShapelessRecipeEvent.class),
		CRAFTING(getCraftingRecipeClass(), RegisterRecipeEvent.CraftingRecipeEvent.class),
		BLASTING(BlastingRecipe.class, RegisterRecipeEvent.CookingRecipeEvent.BlastingRecipeEvent.class),
		FURNACE(FurnaceRecipe.class, RegisterRecipeEvent.CookingRecipeEvent.FurnaceRecipeEvent.class),
		CAMPFIRE(CampfireRecipe.class, RegisterRecipeEvent.CookingRecipeEvent.CampfireRecipeEvent.class),
		SMOKING(SmokingRecipe.class, RegisterRecipeEvent.CookingRecipeEvent.SmokingRecipeEvent.class),
		COOKING(CookingRecipe.class, RegisterRecipeEvent.CookingRecipeEvent.class), // Having 'COOKING' under the subclasses allows for proper ExprRecipeType
		SMITHING_TRANSFORM(SmithingTransformRecipe.class, RegisterRecipeEvent.SmithingRecipeEvent.SmithingTransformRecipeEvent.class),
		SMITHING_TRIM(SmithingTrimRecipe.class, RegisterRecipeEvent.SmithingRecipeEvent.SmithingTrimRecipeEvent.class),
		SMITHING(SmithingRecipe.class, RegisterRecipeEvent.SmithingRecipeEvent.class), // Having 'SMITHING' under the subclasses allows for proper ExprRecipeType
		STONECUTTING(StonecuttingRecipe.class, RegisterRecipeEvent.StonecuttingRecipeEvent.class),
		COMPLEX(ComplexRecipe.class, null);

		private final @Nullable Class<? extends Recipe> recipeClass;
		private final @Nullable Class<? extends Event> eventClass;

		RecipeType(@Nullable Class<? extends Recipe> recipeClass, @Nullable Class<? extends Event> eventClass) {
			this.recipeClass = recipeClass;
			this.eventClass = eventClass;
		}

		public @Nullable Class<? extends Recipe> getRecipeClass() {
			return recipeClass;
		}

		public @Nullable Class<? extends Event> getEventClass() {
			return eventClass;
		}

		// Due to 1.19 not having 'CraftingRecipe.class'
		private static @Nullable Class<? extends Recipe> getCraftingRecipeClass() {
			if (Skript.classExists("org.bukkit.inventory.CraftingRecipe"))
				return CraftingRecipe.class;
			return null;
		}
	}

	public static RecipeType getRecipeTypeFromRecipeClass(Class<? extends Recipe> providedClass) {
		for (RecipeType type : RecipeType.values()) {
			if (type.recipeClass != null && type.recipeClass.isAssignableFrom(providedClass)) {
				return type;
			}
		}
		return null;
	}

	public static RecipeType getRecipeTypeFromRecipe(Recipe providedRecipe) {
		return getRecipeTypeFromRecipeClass(providedRecipe.getClass());
	}

	// Custom Events used for SecRegisterRecipe
	public static class RegisterRecipeEvent extends Event {
		private ItemStack resultItem;
		private boolean errorInEffect = false;
		private RecipeType recipeType;

		public RegisterRecipeEvent(RecipeType recipeType) {
			this.recipeType = recipeType;
		}

		public void setResultItem(ItemStack resultItem) {
			this.resultItem = resultItem;
		}

		public void setErrorInEffect() {
			this.errorInEffect = true;
		}

		public ItemStack getResultItem() {
			return resultItem;
		}

		public boolean getErrorInEffect() {
			return errorInEffect;
		}

		public RecipeType getRecipeType() {
			return recipeType;
		}

		public static class CraftingRecipeEvent extends RegisterRecipeEvent {
			private RecipeChoice[] ingredients = new RecipeChoice[9];
			private CraftingBookCategory category = CraftingBookCategory.MISC;
			private String group;

			public CraftingRecipeEvent(RecipeType recipeType) {
				super(recipeType);
			};

			public void setIngredients(int placement, RecipeChoice item) {
				ingredients[placement] = item;
			}

			public void setCategory(CraftingBookCategory category) {
				this.category = category;
			}

			public void setGroup(String group) {
				this.group = group;
			}

			public RecipeChoice[] getIngredients() {
				return ingredients;
			}

			public CraftingBookCategory getCategory() {
				return category;
			}

			public String getGroup() {
				return group;
			}

			public static class ShapedRecipeEvent extends CraftingRecipeEvent {
				public ShapedRecipeEvent(RecipeType recipeType) {
					super(recipeType);
				};
			}

			public static class ShapelessRecipeEvent extends CraftingRecipeEvent {
				public ShapelessRecipeEvent(RecipeType recipeType) {
					super(recipeType);
				};
			}
		}

		public static class CookingRecipeEvent extends RegisterRecipeEvent {
			private RecipeChoice input;
			private CookingBookCategory category = CookingBookCategory.MISC;
			private String group;
			private int cookingTime = 10;
			private float experience = 0;

			public CookingRecipeEvent(RecipeType recipeType) {
				super(recipeType);
			};

			public void setInput(RecipeChoice input) {
				this.input = input;
			}

			public void setCategory(CookingBookCategory category) {
				this.category = category;
			}

			public void setGroup(String group) {
				this.group = group;
			}

			public void setCookingTime(int cookingTime) {
				this.cookingTime = cookingTime;
			}

			public void setExperience(float experience) {
				this.experience = experience;
			}

			public CookingBookCategory getCategory() {
				return category;
			}

			public String getGroup() {
				return group;
			}

			public RecipeChoice getInput() {
				return input;
			}

			public int getCookingTime() {
				return cookingTime;
			}

			public float getExperience() {
				return experience;
			}

			public static class BlastingRecipeEvent extends CookingRecipeEvent {
				public BlastingRecipeEvent(RecipeType recipeType) {
					super(recipeType);
				}
			}

			public static class CampfireRecipeEvent extends CookingRecipeEvent {
				public CampfireRecipeEvent(RecipeType recipeType) {
					super(recipeType);
				}
			}

			public static class FurnaceRecipeEvent extends CookingRecipeEvent {
				public FurnaceRecipeEvent(RecipeType recipeType) {
					super(recipeType);
				}
			}

			public static class SmokingRecipeEvent extends CookingRecipeEvent {
				public SmokingRecipeEvent(RecipeType recipeType) {
					super(recipeType);
				}
			}
		}

		public static class SmithingRecipeEvent extends RegisterRecipeEvent {

			private RecipeChoice base;
			private RecipeChoice template;
			private RecipeChoice addition;

			public SmithingRecipeEvent(RecipeType recipeType) {
				super(recipeType);
			}

			public void setBase(RecipeChoice base) {
				this.base = base;
			}

			public void setAddition(RecipeChoice addition) {
				this.addition = addition;
			}

			public void setTemplate(RecipeChoice template) {
				this.template = template;
			}

			public RecipeChoice getBase() {
				return base;
			}

			public RecipeChoice getAddition() {
				return addition;
			}

			public RecipeChoice getTemplate() {
				return template;
			}

			public static class SmithingTransformRecipeEvent extends SmithingRecipeEvent {
				public SmithingTransformRecipeEvent(RecipeType recipeType) {
					super(recipeType);
				}
			}

			public static class SmithingTrimRecipeEvent extends SmithingRecipeEvent {
				public SmithingTrimRecipeEvent(RecipeType recipeType) {
					super(recipeType);
				}
			}
		}

		public static class StonecuttingRecipeEvent extends RegisterRecipeEvent {

			private RecipeChoice input;

			public StonecuttingRecipeEvent(RecipeType recipeType) {
				super(recipeType);
			}

			public void setInput(RecipeChoice input) {
				this.input = input;
			}

			public RecipeChoice getInput() {
				return input;
			}
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

}
