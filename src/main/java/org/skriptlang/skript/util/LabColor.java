package org.skriptlang.skript.util;

import org.joml.Matrix3d;
import org.joml.Matrix3dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * Credit to <a href="https://github.com/UnderscoreTud/mc2d/blob/master/common%2Fsrc%2Fmain%2Fjava%2Fme%2Ftud%2Fmc2d%2Futil%2Fcolor%2FLabColor.java">LabColor</a>
 * for this.
 */
public record LabColor(
	double L,
	double a,
	double b
) {

	/**
	 * <a href="http://brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html">Eqn_RGB_XYZ_Matrix</a>
	 * sRGB Working Space, D65 2° Reference White Point
	 */
	private static final Matrix3dc XYZ_TRANSFORMATION = new Matrix3d(
		0.4124564, 0.3575761, 0.1804375,
		0.2126729, 0.7151522, 0.0721750,
		0.0193339, 0.1191920, 0.9503041
	).transpose();

	private static final Matrix3dc INVERSE_XYZ_TRANSFORMATION = new Matrix3d(
		3.2404542, -1.5371385, -0.4985314,
		-0.9692660, 1.8760108, 0.0415560,
		0.0556434, -0.2040259, 1.0572252
	).transpose();

	/**
	 * <a href="https://en.wikipedia.org/wiki/Standard_illuminant#D65_values">D65_values</a>
	 */
	private static final Vector3dc D65 = new Vector3d(0.95047, 1, 1.08883);

	/**
	 * <a href="http://brucelindbloom.com/index.html?LContinuity.html">LContinuity</a>
	 */
	private static final double EPSILON = 0.008856;
	private static final double K_0 = 903.3;
	private static final double K_1 = 7.787;

	private static final double POW25_7 = 6103515625.0;

	public int toRGB() {
		double fy = (L + 16) / 116;
		double fx = a / 500 + fy;
		double fz = fy - b / 200;

		double xr = fInverse(fx);
		double yr = L > K_0 * EPSILON ? fy * fy * fy : L / K_0;
		double zr = fInverse(fz);

		Vector3d rgb = new Vector3d(xr * D65.x(), yr * D65.y(), zr * D65.z());
		rgb.mul(INVERSE_XYZ_TRANSFORMATION);

		double r = inverseGammaCorrection(rgb.x());
		double g = inverseGammaCorrection(rgb.y());
		double b = inverseGammaCorrection(rgb.z());

		int ri = (int) Math.round(Math.clamp(r, 0, 1) * 255);
		int gi = (int) Math.round(Math.clamp(g, 0, 1) * 255);
		int bi = (int) Math.round(Math.clamp(b, 0, 1) * 255);

		return ri << 16 | gi << 8 | bi;
	}

	public double euclideanDistanceSquared(LabColor other) {
		double L = other.L() - L();
		double a = other.a() - a();
		double b = other.b() - b();
		return L * L + a * a + b * b;
	}

	public double euclideanDistance(LabColor other) {
		return Math.sqrt(euclideanDistanceSquared(other));
	}

	private double c() {
		return Math.sqrt(a * a + b * b);
	}

	public static LabColor fromRGB(int rgb) {
		return fromRGB(
			rgb >> 16 & 0xFF,
			rgb >> 8 & 0xFF,
			rgb & 0xFF
		);
	}

	public static LabColor fromRGB(int r, int g, int b) {
		return fromRGB(r / 255.0, g / 255.0, b / 255.0);
	}

	public static LabColor fromRGB(double r, double g, double b) {
		double rLinear = gammaCorrection(r);
		double gLinear = gammaCorrection(g);
		double bLinear = gammaCorrection(b);
		Vector3d xyz = new Vector3d(rLinear, gLinear, bLinear);
		xyz.mul(XYZ_TRANSFORMATION);
		double Y = f(xyz.y() / D65.y());
		double LStar = 116 * Y - 16;
		double aStar = 500 * (f(xyz.x() / D65.x()) - Y);
		double bStar = 200 * (Y - f(xyz.z() / D65.z()));
		return new LabColor(LStar, aStar, bStar);
	}

	private static double gammaCorrection(double value) {
		if (value > 0.04045)
			return Math.pow((value + 0.055) / 1.055, 2.4);
		return value / 12.92;
	}

	private static double inverseGammaCorrection(double value) {
		if (value > 0.0031308)
			return 1.055 * Math.pow(value, 1.0 / 2.4) - 0.055;
		return 12.92 * value;
	}

	private static double f(double t) {
		return t > EPSILON ? Math.cbrt(t) : (K_1 * t) + 16.0 / 116.0;
	}

	private static double fInverse(double t) {
		double n3 = t * t * t;
		return n3 > EPSILON ? n3 : (t - (16 / 116.0)) / K_1;
	}

}
