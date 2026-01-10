package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.docs.Origin;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Potion Effect Type Category")
@Description({
	"An expression to obtain the category of a potion effect type.",
	"That is, whether the potion effect type is beneficial, harmful, or neutral."
})
@Example("""
	on entity potion effect modification:
		if the potion effect type category is harmful:
			 message "You have been afflicted with %potion effect type%"
	""")
@RequiredPlugins("Minecraft 1.21+")
@Since("2.14")
public class ExprPotionEffectTypeCategory extends SimplePropertyExpression<PotionEffectType, PotionEffectTypeCategory> {

	public static void register(SyntaxRegistry registry, Origin origin) {
		if (Skript.classExists("org.bukkit.potion.PotionEffectTypeCategory")) {
			registry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprPotionEffectTypeCategory.class, PotionEffectTypeCategory.class,
				"potion [effect [type]] category", "potioneffecttypes", false)
					.supplier(ExprPotionEffectTypeCategory::new)
					.origin(origin)
					.build());
		}
	}

	@Override
	public @Nullable PotionEffectTypeCategory convert(PotionEffectType type) {
		return type.getCategory();
	}

	@Override
	public Class<? extends PotionEffectTypeCategory> getReturnType() {
		return PotionEffectTypeCategory.class;
	}

	@Override
	protected String getPropertyName() {
		return "potion effect type category";
	}

}
