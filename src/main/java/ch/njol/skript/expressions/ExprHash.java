package ch.njol.skript.expressions;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;


@Name("Hash")
@Description({"Hashes the given text using the MD5 or SHA-256 algorithms. Each algorithm is suitable for different use cases.<p>",
		"MD5 is provided mostly for backwards compatibility, as it is outdated and not secure. ",
		"SHA-256 is more secure, and can used to hash somewhat confidental data like IP addresses and even passwords. ",
		"It is not <i>that</i> secure out of the box, so please consider using salt when dealing with passwords! ",
		"When hashing data, you <strong>must</strong> specify algorithms that will be used for security reasons! ",
		"<p>Please note that a hash cannot be reversed under normal circumstanses. You will not be able to get original value from a hash with Skript."})
@Examples({
		"command /setpass &lt;text&gt;:",
		"\ttrigger:",
		"\t\tset {password::%uuid of player%} to text-argument hashed with SHA-256",
		"command /login &lt;text&gt;:",
		"\ttrigger:",
		"\t\tif text-argument hashed with SHA-256 is {password::%uuid of player%}:",
		"\t\t\tmessage \"Login successful.\"",
		"\t\telse:",
		"\t\t\tmessage \"Wrong password!\""})
@Since("2.0, 2.2-dev32 (SHA-256 algorithm)")
public class ExprHash extends PropertyExpression<String, String> {
	static {
		Skript.registerExpression(ExprHash.class, String.class, ExpressionType.SIMPLE,
				"%strings% hash[ed] with (0¦MD5|1¦SHA-256)");
	}
	
	@SuppressWarnings("null")
	private final static Charset UTF_8 = Charset.forName("UTF-8");
	
	@Nullable
	static MessageDigest md5;
	@Nullable
	static MessageDigest sha256;
	
	static {
		try {
			md5 = MessageDigest.getInstance("MD5");
			sha256 = MessageDigest.getInstance("SHA-256");
		} catch (final NoSuchAlgorithmException e) {
			throw new InternalError("JVM does not adhere to Java specifications");
		}
	}
	
	private int algorithm;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends String>) exprs[0]);
		algorithm = parseResult.mark;
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected String[] get(final Event e, final String[] source) {
		// These can't be null
		assert md5 != null;
		assert sha256 != null;
		
		// Get correct digest
		MessageDigest digest = null;
		if (algorithm == 0)
			digest = md5;
		else if (algorithm == 1)
			digest = sha256;
		else
			assert false;

		// Apply it to all strings
		final String[] r = new String[source.length];
		for (int i = 0; i < r.length; i++)
			r[i] = toHex(digest.digest(source[i].getBytes(UTF_8)));
		
		
		return r;
	}
	
	private static String toHex(final byte[] b) {
		final char[] r = new char[2 * b.length];
		for (int i = 0; i < b.length; i++) {
			r[2 * i] = Character.forDigit((b[i] & 0xF0) >> 4, 16);
			r[2 * i + 1] = Character.forDigit(b[i] & 0x0F, 16);
		}
		return new String(r);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "hash of " + getExpr();
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
}
