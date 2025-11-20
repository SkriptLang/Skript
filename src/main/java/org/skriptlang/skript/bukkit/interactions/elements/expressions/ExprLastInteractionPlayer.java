package org.skriptlang.skript.bukkit.interactions.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.jetbrains.annotations.Nullable;

@Name("Last Interaction Player")
@Description({
	"Returns the last player to attack (left click), or interact (right click) with an interaction entity"
})
@Examples({
	"kill last attack player of last spawned interaction" , "feed last interaction player of {_i}"
})
@Since("INSERT VERSION")
public class ExprLastInteractionPlayer extends SimplePropertyExpression<Entity, OfflinePlayer> {

	static {
		register(ExprLastInteractionPlayer.class, OfflinePlayer.class, "last (attack|1¦interaction) player", "entities");
	}

	private boolean interaction;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		interaction = parseResult.mark == 1;
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}



	@Override
	public @Nullable OfflinePlayer convert(Entity entity) {
		if (entity instanceof Interaction i) {
			if (interaction) {
				return i.getLastInteraction().getPlayer();
			} else {
				return i.getLastAttack().getPlayer();
			}

		}
		return null;
	}

	@Override
	protected String getPropertyName() {
		return "last attack/interaction player";
	}

	@Override
	public Class<? extends OfflinePlayer> getReturnType() {
		return OfflinePlayer.class;
	}
}