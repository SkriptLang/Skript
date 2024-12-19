package org.skriptlang.skript.bukkit.recipes;

import ch.njol.skript.Skript;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.bukkit.recipes.RecipeUtils.RecipeType;

import java.util.ArrayList;
import java.util.List;

/**
 * Mutable Recipe class allowing data to be set before creation of a recipe.
 * Wraps all types of recipe.
 */
public abstract class MutableRecipe implements Recipe {

	private final RecipeType recipeType;
	private final NamespacedKey key;
	private ItemStack result = new ItemStack(Material.AIR);
	private final List<String> errors = new ArrayList<>();

	public MutableRecipe(NamespacedKey key, RecipeType recipeType) {
		this.key = key;
		this.recipeType = recipeType;
	}

	public void setResult(ItemStack result) {
		this.result = result;
	}

	@Override
	public @NotNull ItemStack getResult() {
		return result;
	}

	public NamespacedKey getKey() {
		return this.key;
	}

	public RecipeType getRecipeType() {
		return recipeType;
	}

	public void addError(String error) {
		errors.add(error);
	}

	public List<String> getErrors() {
		return errors;
	}

	public abstract Recipe create();

	public abstract static class MutableCraftingRecipe extends MutableRecipe {
		private final RecipeChoice[] ingredients = new RecipeChoice[9];
		private String group;
		private CraftingBookCategory category;

		public MutableCraftingRecipe(NamespacedKey key, RecipeType recipeType) {
			super(key, recipeType);
		}

		@Override
		public abstract Recipe create();

		public void setIngredients(int placement, RecipeChoice item) {
			ingredients[placement] = item;
		}

		public void setGroup(String group) {
			this.group = group;
		}

		public void setCategory(CraftingBookCategory category) {
			this.category = category;
		}

		public RecipeChoice[] getIngredients() {
			return ingredients;
		}

		public String getGroup() {
			return this.group;
		}

		public CraftingBookCategory getCategory() {
			return this.category;
		}

		public static class MutableShapedRecipe extends MutableCraftingRecipe {

			public MutableShapedRecipe(NamespacedKey key) {
				super(key, RecipeType.SHAPED);
			}

			private static final char[] characters = new char[]{'a','b','c','d','e','f','g','h','i'};

			@Override
			public ShapedRecipe create() {
				if (getResult().getType() == Material.AIR) {
					addError("You must provide a result item when creating a recipe");
					return null;
				}
				RecipeChoice[] ingredients = getIngredients();
				if (ingredients.length == 0) {
					addError("You must have at least 1 ingredient when creating a '" + getRecipeType() + "' recipe.");
					return null;
				}
				ShapedRecipe shapedRecipe = new ShapedRecipe(getKey(), getResult());
				shapedRecipe.shape("abc","def","ghi");
				for (int i = 0; i < ingredients.length; i++) {
					RecipeChoice thisChoice = ingredients[i];
					if (thisChoice != null)
						shapedRecipe.setIngredient(characters[i], thisChoice);
				}
				if (getGroup() != null)
					shapedRecipe.setGroup(getGroup());
				if (getCategory() != null)
					shapedRecipe.setCategory(getCategory());
				return shapedRecipe;
			}

		}

		public static class MutableShapelessRecipe extends MutableCraftingRecipe {

			public MutableShapelessRecipe(NamespacedKey key) {
				super(key, RecipeType.SHAPELESS);
			}

			@Override
			public ShapelessRecipe create() {
				if (getResult().getType() == Material.AIR) {
					addError("You must provide a result item when creating a recipe");
					return null;
				}
				RecipeChoice[] ingredients = getIngredients();
				if (ingredients.length == 0) {
					addError("You must have at least 1 ingredient when creating a '" + getRecipeType() + "' recipe.");
					return null;
				}
				ShapelessRecipe shapelessRecipe = new ShapelessRecipe(getKey(), getResult());
				for (RecipeChoice thisChoice : ingredients) {
					if (thisChoice != null)
						shapelessRecipe.addIngredient(thisChoice);
				}
				if (getGroup() != null)
					shapelessRecipe.setGroup(getGroup());
				if (getCategory() != null)
					shapelessRecipe.setCategory(getCategory());
				return shapelessRecipe;
			}
		}
	}

	public static class MutableCookingRecipe extends MutableRecipe {
		private RecipeChoice input;
		private String group;
		private CookingBookCategory category;
		private int cookingTime = 10;
		private float experience = 0;

		public MutableCookingRecipe(NamespacedKey key, RecipeType recipeType) {
			super(key, recipeType);
		}

		public void setInput(RecipeChoice input) {
			this.input = input;
		}

		public void setGroup(String group) {
			this.group = group;
		}

		public void setCategory(CookingBookCategory category) {
			this.category = category;
		}

		public void setCookingTime(int cookingTime) {
			this.cookingTime = cookingTime;
		}

		public void setExperience(float experience) {
			this.experience = experience;
		}

		public RecipeChoice getInput() {
			return input;
		}

		public String getGroup() {
			return group;
		}

		public CookingBookCategory getCategory() {
			return category;
		}

		public int getCookingTime() {
			return cookingTime;
		}

		public float getExperience() {
			return experience;
		}

		@Override
		public CookingRecipe<?> create() {
			if (getResult().getType() == Material.AIR) {
				addError("You must provide a result item when creating a recipe");
				return null;
			}
			if (getInput() == null) {
				addError("You must provide an input item when creating a " + getRecipeType() + " recipe.");
				return null;
			}
			var recipe = switch (getRecipeType()) {
				case BLASTING -> new BlastingRecipe(getKey(), getResult(), getInput(), getExperience(), getCookingTime());
				case FURNACE -> new FurnaceRecipe(getKey(), getResult(), getInput(), getExperience(), getCookingTime());
				case SMOKING -> new SmokingRecipe(getKey(), getResult(), getInput(), getExperience(), getCookingTime());
				case CAMPFIRE -> new CampfireRecipe(getKey(), getResult(), getInput(), getExperience(), getCookingTime());
				default -> throw new IllegalStateException("Unexpected value: " + getRecipeType());
			};
			if (getGroup() != null)
				recipe.setGroup(getGroup());
			if (getCategory() != null)
				recipe.setCategory(getCategory());
			return recipe;
		}

		public static class MutableBlastingRecipe extends MutableCookingRecipe {
			public MutableBlastingRecipe(NamespacedKey key) {
				super(key, RecipeType.BLASTING);
			}

			@Override
			public BlastingRecipe create() {
				return (BlastingRecipe) super.create();
			}
		}

		public static class MutableFurnaceRecipe extends MutableCookingRecipe {
			public MutableFurnaceRecipe(NamespacedKey key) {
				super(key, RecipeType.FURNACE);
			}

			@Override
			public FurnaceRecipe create() {
				return (FurnaceRecipe) super.create();
			}
		}

		public static class MutableSmokingRecipe extends MutableCookingRecipe {
			public MutableSmokingRecipe(NamespacedKey key) {
				super(key, RecipeType.SMOKING);
			}

			@Override
			public SmokingRecipe create() {
				return (SmokingRecipe) super.create();
			}
		}

		public static class MutableCampfireRecipe extends MutableCookingRecipe {
			public MutableCampfireRecipe(NamespacedKey key) {
				super(key, RecipeType.CAMPFIRE);
			}

			@Override
			public CampfireRecipe create() {
				return (CampfireRecipe) super.create();
			}
		}

	}

	public static class MutableSmithingRecipe extends MutableRecipe {

		private static final boolean SUPPORTS_COPY_NBT = Skript.methodExists(SmithingRecipe.class, "willCopyNbt");
		private static final boolean SUPPORTS_COPY_DATA = Skript.methodExists(SmithingRecipe.class, "willCopyDataComponents");
		private static final boolean RUNNING_1_20_0 = Skript.isRunningMinecraft(1, 20, 0);

		private RecipeChoice base;
		private RecipeChoice template;
		private RecipeChoice addition;
		private Boolean copyData = null;

		public MutableSmithingRecipe(NamespacedKey key, RecipeType recipeType) {
			super(key, recipeType);
		}

		public void setBase(RecipeChoice base) {
			this.base = base;
		}

		public void setTemplate(RecipeChoice template)  {
			this.template = template;
		}

		public void setAddition(RecipeChoice addition) {
			this.addition = addition;
		}

		public void setCopyData(Boolean copyData) {
			this.copyData = copyData;
		}

		public RecipeChoice getBase() {
			return base;
		}

		public RecipeChoice getTemplate() {
			return template;
		}

		public RecipeChoice getAddition() {
			return addition;
		}


		public Boolean willCopyData() {
			return copyData;
		}

		@Override
		public SmithingRecipe create() {
			if (getResult().getType() == Material.AIR) {
				addError("You must provide a result item when creating a recipe");
				return null;
			}
			RecipeChoice base = getBase(), addition = getAddition();
			if (base == null) {
				addError("You must provide a base item when creating a smithing recipe.");
				return null;
			}
			if (addition == null) {
				addError("You must provide an additional item when creating a smithing recipe.");
				return null;
			}
			if ((SUPPORTS_COPY_NBT || SUPPORTS_COPY_DATA) && copyData != null)
				return new SmithingRecipe(getKey(), getResult(), getBase(), getAddition(), copyData);
			return new SmithingRecipe(getKey(), getResult(), getBase(), getAddition());
		}

		public static class MutableSmithingTransformRecipe extends MutableSmithingRecipe {
			public MutableSmithingTransformRecipe(NamespacedKey key) {
				super(key, RecipeType.SMITHING_TRANSFORM);
			}

			@Override
			public SmithingTransformRecipe create() {
				if (getResult().getType() == Material.AIR) {
					addError("You must provide a result item when creating a recipe");
					return null;
				}
				RecipeChoice base = getBase(), template = getTemplate(), addition = getAddition();
				if (base == null) {
					addError("You must provide a base item when creating a smithing recipe.");
					return null;
				}
				if (addition == null) {
					addError("You must provide an additional item when creating a smithing recipe.");
					return null;
				}
				if (template == null) {
					addError("You must provide a template item when creating a smithing recipe.");
					return null;
				}
				if (RUNNING_1_20_0 && (SUPPORTS_COPY_NBT || SUPPORTS_COPY_DATA) && willCopyData() != null)
					return new SmithingTransformRecipe(getKey(), getResult(), getTemplate(), getBase(), getAddition(), willCopyData());
				return new SmithingTransformRecipe(getKey(), getResult(), getTemplate(), getBase(), getAddition());
			}
		}

		public static class MutableSmithingTrimRecipe extends MutableSmithingRecipe {
			public MutableSmithingTrimRecipe(NamespacedKey key) {
				super(key, RecipeType.SMITHING_TRIM);
			}

			@Override
			public SmithingTrimRecipe create() {
				RecipeChoice base = getBase(), template = getTemplate(), addition = getAddition();
				if (base == null) {
					addError("You must provide a base item when creating a smithing recipe.");
					return null;
				}
				if (addition == null) {
					addError("You must provide an additional item when creating a smithing recipe.");
					return null;
				}
				if (template == null) {
					addError("You must provide a template item when creating a smithing recipe.");
					return null;
				}
				if (RUNNING_1_20_0 && (SUPPORTS_COPY_NBT || SUPPORTS_COPY_DATA) && willCopyData() != null)
					return new SmithingTrimRecipe(getKey(), getTemplate(), getBase(), getAddition(), willCopyData());
				return new SmithingTrimRecipe(getKey(), getTemplate(), getBase(), getAddition());
			}
		}
	}

	public static class MutableStonecuttingRecipe extends MutableRecipe {
		private RecipeChoice input;
		private String group;

		public MutableStonecuttingRecipe(NamespacedKey key) {
			super(key, RecipeType.STONECUTTING);
		}

		public void setInput(RecipeChoice input) {
			this.input = input;
		}

		public RecipeChoice getInput() {
			return input;
		}

		public void setGroup(String group) {
			this.group =  group;
		}

		public String getGroup() {
			return group;
		}

		@Override
		public StonecuttingRecipe create() {
			if (getResult().getType() == Material.AIR) {
				addError("You must provide a result item when creating a recipe");
				return null;
			}
			if (getInput() == null) {
				addError("You must provide an input item when creating a stonecutting recipe.");
				return null;
			}
			StonecuttingRecipe recipe = new StonecuttingRecipe(getKey(), getResult(), input);
			if (group != null && !group.isEmpty())
				recipe.setGroup(group);
			return recipe;
		}
	}

	public static class MutableTransmuteRecipe extends MutableRecipe {

		private RecipeChoice input, material;

		public MutableTransmuteRecipe(NamespacedKey key) {
			super(key, RecipeType.TRANSMUTE);
		}

		public void setInput(RecipeChoice input) {
			this.input = input;
		}

		public void setMaterial(RecipeChoice material) {
			this.material = material;
		}

		public RecipeChoice getInput() {
			return input;
		}

		public RecipeChoice getMaterial() {
			return material;
		}

		@Override
		public TransmuteRecipe create() {
			if (getResult().getType() == Material.AIR) {
				addError("You must provide a result item when creating a recipe");
				return null;
			}
			if (getInput() == null) {
				addError("You must provide an input item when creating a transmute recipe.");
				return null;
			}
			if (getMaterial() == null) {
				addError("You must provide a transmute item when creating a transmute recipe.");
				return null;
			}
			return new TransmuteRecipe(getKey(), getResult().getType(), input, material);
		}
	}

}
