package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.yggdrasil.Fields;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

import java.io.StreamCorruptedException;

import static ch.njol.skript.classes.Changer.*;

@ApiStatus.Internal
public class VectorClassInfo extends ClassInfo<Vector> {

	public VectorClassInfo() {
		super(Vector.class, "vector");
		this.user("vectors?")
			.name("Vector")
			.description("Vector is a collection of numbers. In Minecraft, 3D vectors are used to express velocities of entities.")
			.usage("vector(x, y, z)")
			.examples("")
			.since("2.2-dev23")
			.defaultExpression(new EventValueExpression<>(Vector.class))
			.parser(new VectorParser())
			.serializer(new VectorSerializer())
			.cloner(Vector::clone)
			.property(Property.WXYZ,
				"X, Y, or Z component of the vector.",
				Skript.instance(),
				new VectorWXYZHandler());
	}

	private static class VectorWXYZHandler extends WXYZHandler<Vector, Double> {
		//<editor-fold desc="vector wxyz handler" defaultstate="collapsed">
		@Override
		public PropertyHandler<Vector> newInstance() {
			var instance = new VectorWXYZHandler();
			instance.axis(axis);
			return instance;
		}

		@Override
		public @Nullable Double convert(Vector propertyHolder) {
			return switch (axis) {
				case X -> propertyHolder.getX();
				case Y -> propertyHolder.getY();
				case Z -> propertyHolder.getZ();
				default -> null;
			};
		}

		@Override
		public boolean supportsAxis(Axis axis) {
			return axis != Axis.W;
		}

		@Override
		public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			return switch (mode) {
				case ADD, SET, REMOVE -> new Class[]{Float.class};
				default -> null;
			};
		}

		@Override
		public void change(Vector vector, Object @Nullable [] delta, ChangeMode mode) {
			assert delta != null;
			float value = ((Float) delta[0]);
			if (axis == Axis.W)
				return;
			switch (mode) {
				case REMOVE:
					value = -value;
					//$FALL-THROUGH$
				case ADD:
					switch (axis) {
						case X -> vector.setX(vector.getX() + value);
						case Y -> vector.setY(vector.getY() + value);
						case Z -> vector.setZ(vector.getZ() + value);
					}
					break;
				case SET:
					switch (axis) {
						case X -> vector.setX(value);
						case Y -> vector.setY(value);
						case Z -> vector.setZ(value);
					}
					break;
				default:
					assert false;
			}
		}

		@Override
		public @NotNull Class<Double> returnType() {
			return Double.class;
		}

		@Override
		public boolean requiresSourceExprChange() {
			return true;
		}
		//</editor-fold>
	}

	private static class VectorParser extends Parser<Vector> {
		//<editor-fold desc="vector parser" defaultstate="collapsed">
		@Override
		public boolean canParse(ParseContext context) {
			return false;
		}

		@Override
		public String toString(Vector vec, int flags) {
			return "x: " + Skript.toString(vec.getX()) + ", y: " + Skript.toString(vec.getY()) + ", z: " + Skript.toString(vec.getZ());
		}

		@Override
		public String toVariableNameString(Vector vec) {
			return "vector:" + vec.getX() + "," + vec.getY() + "," + vec.getZ();
		}

		@Override
		public String getDebugMessage(Vector vec) {
			return "(" + vec.getX() + "," + vec.getY() + "," + vec.getZ() + ")";
		}
		//</editor-fold>
	}

	private static class VectorSerializer extends Serializer<Vector> {
		//<editor-fold desc="vector serializer" defaultstate="collapsed">
		@Override
		public Fields serialize(Vector o) {
			Fields f = new Fields();
			f.putPrimitive("x", o.getX());
			f.putPrimitive("y", o.getY());
			f.putPrimitive("z", o.getZ());
			return f;
		}

		@Override
		public void deserialize(Vector o, Fields f) {
			assert false;
		}

		@Override
		public Vector deserialize(Fields f) throws StreamCorruptedException {
			return new Vector(f.getPrimitive("x", double.class), f.getPrimitive("y", double.class), f.getPrimitive("z", double.class));
		}

		@Override
		public boolean mustSyncDeserialization() {
			return false;
		}

		@Override
		protected boolean canBeInstantiated() {
			return false;
		}
		//</editor-fold>
	}

}
