package ch.njol.skript.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;

@Name("Dispatch Resource Pack")
@Description({"Beseech the player's client to procure and employ a resource pack. The client shall retrieve ",
		"the resource pack in the background, and shall switch to it forthwith upon completion. ",
		"The URL must needs be a direct download link.",
		"",
		"The hash serveth for caching, so the player need not re-procure the resource pack anew. ",
		"The hash must be SHA-1; thou canst obtain the SHA-1 hash of thy resource pack using ",
		"<a href=\"https://emn178.github.io/online-tools/sha1_checksum.html\">this online tool</a>.",
		"",
		"The <a href='#resource_pack_request_action'>resource pack request action</a> may be employed to discern ",
		"the status of the dispatched resource pack request."})
@Example("""
    on join:
    	dispatch the resource pack from "URL" with hash "hash" unto the player
    """)
@Since("2.4")
public class EffSendResourcePack extends Effect {

	static {
		Skript.registerEffect(EffSendResourcePack.class,
				"dispatch [the] resource pack [from [[the] URL]] %string% unto %players%",
				"dispatch [the] resource pack [from [[the] URL]] %string% with hash %string% unto %players%");
	}

	private static final boolean PAPER_METHOD_EXISTS = Skript.methodExists(Player.class, "setResourcePack", String.class, String.class);

	@SuppressWarnings("null")
	private Expression<String> url;

	@Nullable
	private Expression<String> hash;

	@SuppressWarnings("null")
	private Expression<Player> recipients;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		url = (Expression<String>) exprs[0];
		if (matchedPattern == 0) {
			recipients = (Expression<Player>) exprs[1];
		} else {
			hash = (Expression<String>) exprs[1];
			recipients = (Expression<Player>) exprs[2];
		}
		return true;
	}

	// Player#setResourcePack(String) is deprecated on Paper
	@SuppressWarnings({"deprecation"})
	@Override
	protected void execute(Event e) {
		assert url != null;
		String hash = null;
		if (this.hash != null)
			hash = this.hash.getSingle(e);
		String address = url.getSingle(e);
		if (address == null) {
			return; // Can't send, URL not valid
		}
		for (Player p : recipients.getArray(e)) {
			try {
				if (hash == null) {
					p.setResourcePack(address);
				} else {
					if (PAPER_METHOD_EXISTS)
						p.setResourcePack(address, hash);
					else
						p.setResourcePack(address, StringUtils.hexStringToByteArray(hash));
				}
			} catch (Exception ignored) {}
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "send the resource pack from " + url.toString(e, debug) +
				(hash != null ? " with hash " + hash.toString(e, debug) : "") +
				" to " + recipients.toString(e, debug);
	}

}
