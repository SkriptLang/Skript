package org.skriptlang.skript.common.types;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

@ApiStatus.Internal
public class QuaternionClassInfo extends ClassInfo<Quaternionf> {

	public QuaternionClassInfo() {
		super(Quaternionf.class, "quaternion");
		this.user("quaternionf?s?")
			.name("Quaternion")
			.description("Quaternions are four dimensional vectors, often used for representing rotations.")
			.since("2.10")
			.parser(new QuaternionParser())
			.defaultExpression(new EventValueExpression<>(Quaternionf.class))
			.cloner(quaternion -> {
				try {
					// Implements cloneable, but doesn't return a Quaternionf.
					// org.joml improper override. Returns Object.
					return (Quaternionf) quaternion.clone();
				} catch (CloneNotSupportedException e) {
					return null;
				}
			})
			.property(Property.WXYZ,
				"W, X, Y, or Z component of the quaternion.",
				Skript.instance(),
				new QuaternionWXYZHandler());
	}

	private static class QuaternionWXYZHandler extends WXYZHandler<Quaternionf, Float> {
		//<editor-fold desc="quaternion wxyz handler" defaultstate="collapsed">
		@Override
		public PropertyHandler<Quaternionf> newInstance() {
			var instance =  new QuaternionWXYZHandler();
			instance.axis(this.axis);
			return instance;
		}

		@Override
		public @NotNull Float convert(Quaternionf propertyHolder) {
			return switch (axis) {
				case W -> propertyHolder.w;
				case X -> propertyHolder.x;
				case Y -> propertyHolder.y;
				case Z -> propertyHolder.z;
			};
		}

		@Override
		public boolean supportsAxis(Axis axis) {
			return true;
		}

		@Override
		public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			return switch (mode) {
				case ADD, SET, REMOVE -> new Class[]{Float.class};
				default -> null;
			};
		}

		@Override
		public void change(Quaternionf propertyHolder, Object @Nullable [] delta, ChangeMode mode) {
			assert delta != null;
			float value = ((Float) delta[0]);
			float x = propertyHolder.x();
			float y = propertyHolder.y();
			float z = propertyHolder.z();
			float w = propertyHolder.w();
			switch (mode) {
				case REMOVE:
					value = -value;
					//$FALL-THROUGH$
				case ADD:
					switch (axis) {
						case W -> w += value;
						case X -> x += value;
						case Y -> y += value;
						case Z -> z += value;
					}
					break;
				case SET:
					switch (axis) {
						case W -> w = value;
						case X -> x = value;
						case Y -> y = value;
						case Z -> z = value;
					}
					break;
			}
			propertyHolder.set(x, y, z, w);
		}

		@Override
		public @NotNull Class<Float> returnType() {
			return Float.class;
		}

		@Override
		public boolean requiresSourceExprChange() {
			return true;
		}
		//</editor-fold>
	}

	private static class QuaternionParser extends Parser<Quaternionf> {
		//<editor-fold desc="quaternion parser" defaultstate="collapsed">
		public boolean canParse(ParseContext context) {
			return false;
		}

		@Override
		public String toString(Quaternionf quaternion, int flags) {
			return "w:" + Skript.toString(quaternion.w()) + ", x:" + Skript.toString(quaternion.x()) + ", y:" + Skript.toString(quaternion.y()) + ", z:" + Skript.toString(quaternion.z());
		}

		@Override
		public String toVariableNameString(Quaternionf quaternion) {
			return quaternion.w() + "," + quaternion.x() + "," + quaternion.y() + "," + quaternion.z();
		}
		//</editor-fold>
	}

}
