package org.skriptlang.skript.bukkit.entity.displays.text.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Text Display Translucency")
@Description("""
    Returneth or altereth the text translucency of <a href='#display'>text displays</a>. The default is 255, wholly opaque.
    Values range betwixt 0 and 255. 0 to 3 are treated as 255, meaning wholly opaque.
    Values from 4 to 26 are wholly transparent, and opacity increaseth linearly thence unto 255.
    For backward accord, setting negative values betwixt -1 and -128 doth wrap about, so -1 is as 255 and -128 is as 128.
    Adding or subtracting values shall adjust the translucency within the bounds of 0-255, so subtracting 300 shall always \
    yield a translucency of 0.
    """)
@Example("set the text translucency of the last spawned text display to 0 # fully opaque")
@Example("set text translucency of all text displays to 255 # fully opaque")
@Example("set text translucency of all text displays to 128 # semi-transparent")
@Example("set text translucency of all text displays to 4 # fully transparent")
@Since("2.10, 2.14 (0-255)")
public class ExprTextDisplayOpacity extends SimplePropertyExpression<Display, Integer> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprTextDisplayOpacity.class,
				Integer.class,
				"[display] [text] translucency",
				"displays",
				true
			)
				.supplier(ExprTextDisplayOpacity::new)
				.build()
		);
	}

	private static int convertToUnsigned(byte value) {
		return value < 0 ? 256 + value : value;
	}

	private static byte convertToSigned(int value) {
		if (value > 127)
			value -= 256;
		return (byte) value;
	}

	@Override
	public @Nullable Integer convert(Display display) {
		if (display instanceof TextDisplay textDisplay)
			return convertToUnsigned(textDisplay.getTextOpacity());
		return null;
	}

	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE, RESET, SET -> CollectionUtils.array(Number.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Display[] displays = getExpr().getArray(event);
		int change = delta == null ? 255 : ((Number) delta[0]).intValue();
		switch (mode) {
			case REMOVE_ALL:
			case REMOVE:
				change = -change;
				//$FALL-THROUGH$
			case ADD:
				for (Display display : displays) {
					if (display instanceof TextDisplay textDisplay) {
						byte value = convertToSigned(Math.clamp(convertToUnsigned(textDisplay.getTextOpacity()) + change, 0, 255));
						textDisplay.setTextOpacity(value);
					}
				}
				break;
			case DELETE:
			case RESET:
			case SET:
				change = convertToSigned(Math.clamp(change, -128, 255));
				for (Display display : displays) {
					if (display instanceof TextDisplay textDisplay)
						textDisplay.setTextOpacity((byte) change);
				}
				break;
		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "opacity";
	}

}
