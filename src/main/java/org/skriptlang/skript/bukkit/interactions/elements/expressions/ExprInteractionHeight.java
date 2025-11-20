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


@Name("Interaction Height")
@Description({
	"Returns the interaction height of an interaction entity"
})
@Examples({
	"set interaction height of last spawned interaction to 5.3"
})
@Since("INSERT VERSION")
public class ExprInteractionHeight extends SimplePropertyExpression<Entity, Number> {

	static {
		register(ExprInteractionHeight.class, Number.class, "interaction height[s]", "entities");
	}

	@Override
	public @Nullable Number convert(Entity interaction) {
		if (interaction instanceof Interaction i) {
			return i.getInteractionHeight();
		}

		return null;
	}

	@Override
	protected String getPropertyName() {
		return "interaction height";
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if ((mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.RESET)
			&& getExpr().isSingle())
			return new Class[] {Number.class};
		return null;
	}

	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		assert !(getExpr().getSingle(e) instanceof Interaction) || delta != null;
		final Interaction i = (Interaction) getExpr().getSingle(e);
		if (i == null)
			return;
		float n = ((Number) delta[0]).floatValue();
		switch (mode) {
			case REMOVE:
				i.setInteractionHeight(i.getInteractionHeight() - n);
				break;
			case ADD:
				i.setInteractionHeight(i.getInteractionHeight() + n);
				break;
			case SET:
				i.setInteractionHeight(n);
				break;
			case RESET:
				i.setInteractionHeight(1.0f);
		}
	}

}