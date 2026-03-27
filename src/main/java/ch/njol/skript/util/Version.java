package ch.njol.skript.util;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Peter Güttinger
 */
public class Version implements Serializable, Comparable<Version> {
	private final static long serialVersionUID = 8687040355286333293L;
	
	private final Integer[] version = new Integer[3];
	/**
	 * Everything after the version, e.g. "alpha", "b", "rc 1", "build 2314", "-SNAPSHOT" etc. or null if nothing.
	 */
	@Nullable
	private final String postfix;

	public Version(int... version) {
		if (version.length < 1 || version.length > 3)
			throw new IllegalArgumentException("Versions must have a minimum of 2 and a maximum of 3 numbers (" + version.length + " numbers given)");
		for (int i = 0; i < version.length; i++)
			this.version[i] = version[i];
		postfix = null;
	}

	public Version(int major, int minor, @Nullable String postfix) {
		version[0] = major;
		version[1] = minor;
		this.postfix = postfix == null || postfix.isEmpty() ? null : postfix;
	}

	public final static Pattern versionPattern = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?(?:-(.*))?");

	public Version(String version) {
		final Matcher m = versionPattern.matcher(version.trim());
		if (!m.matches())
			throw new IllegalArgumentException("'" + version + "' is not a valid version string");
		for (int i = 0; i < 3; i++) {
			if (m.group(i + 1) != null)
				this.version[i] = Utils.parseInt("" + m.group(i + 1));
		}
		postfix = m.group(4);
	}

	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Version))
			return false;
		return compareTo((Version) obj) == 0;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(version) * 31 + (postfix == null ? 0 : postfix.hashCode());
	}
	
	@Override
	public int compareTo(@Nullable Version other) {
		if (other == null)
			return 1;

		for (int i = 0; i < version.length; i++) {
			if (get(i) > other.get(i))
				return 1;
			if (get(i) < other.get(i))
				return -1;
		}
		return comparePostfixes(postfix, other.postfix);
	}

	private static int comparePostfixes(@Nullable String postfixA, @Nullable String postfixB) {
		// lowercase
		postfixA = postfixA == null ? null : postfixA.toLowerCase();
		postfixB = postfixB == null ? null : postfixB.toLowerCase();

		// X.x.x vs X.X.X-postfix or vice versa
		// pre, beta, and alpha are considered "smaller" than no postfix
		// nightly is considered "larger" than no postfix, as it tends to be a cutting-edge build built on top of the no postfix version
		if (postfixA == null && postfixB == null)
			return 0;
		if (postfixA == null) {
			if (postfixB.startsWith("nightly"))
				return -1;
			return 1;
		} else if (postfixB == null) {
			if (postfixA.startsWith("nightly"))
				return 1;
			return -1;
		}

		// X.X.X-postfixA vs X.X.X-postfixB
		// ordering: alphaX < betaX < preX < nightly
		// X is an optional number, e.g. alpha1, beta2, pre3, which is compared numerically

		// check for nightly
		if (postfixA.startsWith("nightly")) {
			return postfixB.startsWith("nightly") ? 0 : 1;
		} else if (postfixB.startsWith("nightly")) {
			return -1;
		}

		// check for alpha, beta, pre
		String[] prefixes = { "pre", "beta", "alpha" };
		for (String prefix : prefixes) {
			boolean aStarts = postfixA.startsWith(prefix);
			boolean bStarts = postfixB.startsWith(prefix);
			if (aStarts || bStarts) {
				if (aStarts && bStarts) {
					// both have the same prefix, compare the numbers after it
					String aNumberStr = postfixA.substring(prefix.length()).trim();
					String bNumberStr = postfixB.substring(prefix.length()).trim();
					int aNumber = Math.abs(Utils.parseInt(aNumberStr));
					int bNumber = Math.abs(Utils.parseInt(bNumberStr));
					return Integer.compare(aNumber, bNumber);
				} else {
					// one has the prefix, the other doesn't
					return aStarts ? 1 : -1;
				}
			}
		}
		// cannot determine order, consider equal
		return 0;
	}

	/**
	 * @param other An array containing the major, minor, and revision (ex: 1,19,3)
	 * @return a negative integer, zero, or a positive integer as this object is
	 * less than, equal to, or greater than the specified object.
	 */
	public int compareTo(int... other) {
		assert other.length >= 2 && other.length <= 3;
		for (int i = 0; i < version.length; i++) {
			if (get(i) > (i >= other.length ? 0 : other[i]))
				return 1;
			if (get(i) < (i >= other.length ? 0 : other[i]))
				return -1;
		}
		return 0;
	}

	private int get(int i) {
		return version[i] == null ? 0 : version[i];
	}
	
	public boolean isSmallerThan(final Version other) {
		return compareTo(other) < 0;
	}
	
	public boolean isLargerThan(final Version other) {
		return compareTo(other) > 0;
	}
	
	/**
	 * @return Whether this is a stable version, i.e. a simple version number without any additional details (like alpha/beta/etc.)
	 */
	public boolean isStable() {
		return postfix == null;
	}
	
	public int getMajor() {
		return version[0];
	}
	
	public int getMinor() {
		return version[1];
	}
	
	public int getRevision() {
		return version[2] == null ? 0 : version[2];
	}
	
	@Override
	public String toString() {
		return version[0] + "." + version[1] + (version[2] == null ? "" : "." + version[2]) + (postfix == null ? "" : "-" + postfix);
	}
	
	public static int compare(final String v1, final String v2) {
		return new Version(v1).compareTo(new Version(v2));
	}
}
