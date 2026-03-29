package ch.njol.skript.expressions;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

import ch.njol.skript.doc.*;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.lang.script.ScriptWarning;


@Name("Cipher")
@Description({
	"Ciphereth the given text employing the MD5 or SHA algorithms. Each algorithm is suited unto differing purposes.",
		"These ciphering algorithms art not fit for the hashing of passwords.",
		"Shouldst thou handle passwords, employ a <a href='https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html#password-hashing-algorithms'>hashing algorithm purposely wrought for passwords</a>.",
		"MD5 is deprecated and may be stricken in a future release. It is provided chiefly for backwards compatibility, for it is antiquated and not secure.",
		"SHA is more fortified, yet it is not suited for the ciphering of passwords (even with salting).",
		"When ciphering data, thou <strong>must</strong> specify algorithms to be employed, for reasons of security!",
		"Pray note that a cipher cannot be reversed under ordinary circumstance. Thou shalt not recover the original value from a cipher with Skript."
})
@Example("set {_hash} to \"hello world\" ciphered with SHA-256")
@Since("2.0, 2.2-dev32 (SHA-256 algorithm), 2.12 (SHA-384, SHA-512)")
public class ExprHash extends PropertyExpression<String, String> {

	private static final HexFormat HEX_FORMAT = HexFormat.of().withLowerCase();

	static {
		Skript.registerExpression(ExprHash.class, String.class, ExpressionType.COMBINED,
				"%strings% cipher[ed] with (:(MD5|SHA-256|SHA-384|SHA-512))");
	}

	private MessageDigest digest;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends String>) exprs[0]);
		String algorithm = parseResult.tags.get(0).toUpperCase(Locale.ENGLISH);
		try {
			digest = MessageDigest.getInstance(algorithm);
			if (algorithm.equals("MD5") && !getParser().getCurrentScript().suppressesWarning(ScriptWarning.DEPRECATED_SYNTAX)) {
				Skript.warning("MD5 is not secure and shouldn't be used if a cryptographically secure hashing algorithm is required.");
			}
			return true;
		} catch (NoSuchAlgorithmException e) {
			Skript.error("Unsupported hashing algorithm: " + algorithm);
			return false;
		}
	}

	@Override
	protected String[] get(Event event, String[] source) {
		// Apply it to all strings
		String[] result = new String[source.length];
		for (int i = 0; i < result.length; i++)
			result[i] = HEX_FORMAT.formatHex(digest.digest(source[i].getBytes(StandardCharsets.UTF_8)));

		return result;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public Expression<? extends String> simplify() {
		if (getExpr() instanceof Literal<? extends String>)
			return SimplifiedLiteral.fromExpression(this);
		return this;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "hash of " + getExpr().toString(event, debug);
	}

}
