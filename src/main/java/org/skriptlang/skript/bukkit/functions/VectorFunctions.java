package org.skriptlang.skript.bukkit.functions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.function.Functions;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.BukkitModule;
import org.skriptlang.skript.common.function.DefaultFunction;

/**
 * Contains all functions using Bukkit vectors.
 */
public class VectorFunctions {

	public VectorFunctions(BukkitModule module, SkriptAddon addon) {
		SkriptAddon skript = module.origin(addon).addon();

		Functions.register(DefaultFunction.builder(skript, "vector", Vector.class)
				.description("Creates a vector from a single argument. Equivalent to vector(n, n, n).")
				.examples("vector(1) # = vector(1, 1, 1)")
				.since("2.15")
				.parameter("n", Number.class)
				.build(args -> {
					double value = args.<Number>get("n").doubleValue();
					return new Vector(value, value, value);
				}));

		Functions.register(DefaultFunction.builder(skript, "vector", Vector.class)
				.description("Creates a new vector, which can be used with various expressions, effects and functions.")
				.examples("vector(0, 0, 0)")
				.since("2.2-dev23")
				.parameter("x", Number.class)
				.parameter("y", Number.class)
				.parameter("z", Number.class)
				.build(args -> new Vector(
						args.<Number>get("x").doubleValue(),
						args.<Number>get("y").doubleValue(),
						args.<Number>get("z").doubleValue()
				)));

		if (Skript.classExists("org.joml.Quaternionf")) {
			Functions.register(DefaultFunction.builder(skript, "quaternion", Quaternionf.class)
					.description("Returns a quaternion from the given W, X, Y and Z parameters. ")
					.examples("quaternion(1, 5.6, 45.21, 10)")
					.since("2.10")
					.parameter("w", Number.class)
					.parameter("x", Number.class)
					.parameter("y", Number.class)
					.parameter("z", Number.class)
					.build(args -> {
						double w = args.<Number>get("w").doubleValue();
						double x = args.<Number>get("x").doubleValue();
						double y = args.<Number>get("y").doubleValue();
						double z = args.<Number>get("z").doubleValue();
						return new Quaternionf(x, y, z, w);
					}));
		}

		if (Skript.classExists("org.joml.AxisAngle4f")) {
			Functions.register(DefaultFunction.builder(skript, "axisAngle", Quaternionf.class)
					.description("Returns a quaternion from the given angle (in degrees) and axis (as a vector). This represents a rotation around the given axis by the given angle.")
					.examples("axisAngle(90, (vector from player's facing))")
					.since("2.10")
					.parameter("angle", Number.class)
					.parameter("axis", Vector.class)
					.build(args -> {
						float angle = (float) (args.<Number>get("angle").floatValue() / 180 * Math.PI);
						Vector v = args.get("axis");
						if (v.isZero() || !Double.isFinite(v.getX()) || !Double.isFinite(v.getY()) || !Double.isFinite(v.getZ()))
							return null;
						Vector3f axis = v.toVector3f();
						return new Quaternionf(new AxisAngle4f(angle, axis));
					}));
		}
	}
}
