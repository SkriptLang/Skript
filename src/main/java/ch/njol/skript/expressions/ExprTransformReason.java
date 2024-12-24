package ch.njol.skript.expressions;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTransformEvent.TransformReason;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ExpressionType;

@Name("Transform Reason")
@Description("The <a href='classes.html#transformreason'>transform reason</a> within an entity <a href='events.html#entity transform'>entity transform</a> event.")
@Examples({
	"on entity transform:",
		"\ttransform reason is infection, drowned or frozen"
})
@Since("2.8.0")
public class ExprTransformReason extends EventValueExpression<TransformReason> {

	static {
		Skript.registerExpression(ExprTransformReason.class, TransformReason.class, ExpressionType.SIMPLE, "[the] transform[ing] (cause|reason|type)");
	}

	public ExprTransformReason() {
		super(TransformReason.class);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "transform reason";
	}

}
