package org.skriptlang.skript.bukkit.interactions.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Interaction Responsiveness")
@Description({
	"Returns the responsiveness of an interaction entity"
})
@Examples({
	"set interaction responsiveness of last spawned interaction to false"
})
@Since("INSERT VERSION")
public class ExprInteractionResponsiveness extends SimplePropertyExpression<Entity, Boolean> {

	static {
		register(ExprInteractionResponsiveness.class, Boolean.class, "interaction Responsiveness[es]", "entities");
	}

	@Override
	public @Nullable Boolean convert(Entity interaction) {
		if (interaction instanceof Interaction i) {
			return i.isResponsive();
		}

		return null;
	}

	@Override
	protected String getPropertyName() {
		return "interaction responsiveness";
	}

	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if ((mode == ChangeMode.SET || mode == ChangeMode.RESET)
			&& getExpr().isSingle())
			return new Class[] {Boolean.class};
		return null;
	}

	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		assert !(getExpr().getSingle(e) instanceof Interaction) || delta != null;
		final Interaction i = (Interaction) getExpr().getSingle(e);
		if (i == null)
			return;
		Boolean b = ((Boolean) delta[0]);
		switch (mode) {
			case SET:
				i.setResponsive(b);
				break;
			case RESET:
				i.setResponsive(true);
		}
	}

}