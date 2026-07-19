package org.skriptlang.skript.bukkit.world.elements.expressions;

import ch.njol.skript.Skript;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Difficulty")
@Description("The difficulty of a world.")
@Example("set the difficulty of \"world\" to hard")
@Since("2.3")
public class ExprDifficulty extends SimplePropertyExpression<World, Difficulty> {

	private final static boolean USE_DEPRECATED = Skript.fieldExists(org.bukkit.World.class, "setSpawnFlags");

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprDifficulty.class,
				Difficulty.class,
				"difficult(y|ies)",
				"worlds",
				false
			)
				.supplier(ExprDifficulty::new)
				.build()
		);
	}

	@Override
	public @Nullable Difficulty convert(World world) {
		return world.getDifficulty();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Difficulty.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta == null)
			return;

		Difficulty difficulty = (Difficulty) delta[0];
		if (difficulty == null)
			return;

		for (World world : getExpr().getArray(event)) {
			world.setDifficulty(difficulty);
			if (difficulty != Difficulty.PEACEFUL) {
				// Force enable spawn monsters as changing difficulty won't change this by itself
				if (USE_DEPRECATED) {
					// This is deprecated since 26.2 and marked for removal
					world.setSpawnFlags(true, world.getAllowAnimals());
				} else {
					world.setAllowMonsterSpawning(true);
				}
			}
		}
	}

	@Override
	protected String getPropertyName() {
		return "difficulty";
	}

	@Override
	public Class<Difficulty> getReturnType() {
		return Difficulty.class;
	}

}
