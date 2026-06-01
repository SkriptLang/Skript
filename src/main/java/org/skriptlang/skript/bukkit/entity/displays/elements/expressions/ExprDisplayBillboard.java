package org.skriptlang.skript.bukkit.entity.displays.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Display Billboard")
@Description({
	"Returns or changes the <a href='#billboard'>billboard</a> setting of <a href='#display'>displays</a>.",
	"This describes the axes/points around which the display can pivot.",
	"Displays spawn with the 'fixed' billboard by default. Resetting this expression will also set it to 'fixed'."
})
@Example("set billboard of the last spawned text display to center")
@Since("2.10")
public class ExprDisplayBillboard extends SimplePropertyExpression<Display, Billboard> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprDisplayBillboard.class,
				Billboard.class,
				"bill[ |-]board[ing] [setting]",
				"displays",
				true
			)
				.supplier(ExprDisplayBillboard::new)
				.build()
		);
	}

	@Override
	@Nullable
	public Billboard convert(Display display) {
		return display.getBillboard();
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case RESET -> CollectionUtils.array();
			case SET -> CollectionUtils.array(Billboard.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Billboard billboard = delta != null ? (Billboard) delta[0] : Billboard.FIXED;
		for (Display display : getExpr().getArray(event))
			display.setBillboard(billboard);
	}

	@Override
	public Class<? extends Billboard> getReturnType() {
		return Billboard.class;
	}

	@Override
	protected String getPropertyName() {
		return "billboard";
	}

}
