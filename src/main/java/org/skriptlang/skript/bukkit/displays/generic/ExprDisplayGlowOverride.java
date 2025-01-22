package org.skriptlang.skript.bukkit.displays.generic;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Display Glow Color Override")
@Description({
	"Returns or changes the glowing color override of <a href='classes.html#display'>displays</a>.",
	"This overrides whatever color is already set for the scoreboard team of the displays."
})
@Examples("set glow color override of the last spawned text display to blue")
@Since("2.10")
public class ExprDisplayGlowOverride extends SimplePropertyExpression<Display, Color> {

	static {
		registerDefault(ExprDisplayGlowOverride.class, Color.class, "glow[ing] colo[u]r[s] override[s]", "displays");
	}

	@Override
	@Nullable
	public Color convert(Display display) {
		org.bukkit.Color color = display.getGlowColorOverride();
		return color != null ? ColorRGB.fromBukkitColor(color) : null;
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, RESET, DELETE -> CollectionUtils.array(Color.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		org.bukkit.Color color = delta != null ? ((Color) delta[0]).asBukkitColor() : null;
		for (Display display : getExpr().getArray(event))
			display.setGlowColorOverride(color);
	}

	@Override
	public Class<? extends Color> getReturnType() {
		return Color.class;
	}

	@Override
	protected String getPropertyName() {
		return "glow color override";
	}

}
