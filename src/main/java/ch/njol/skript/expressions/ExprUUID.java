package ch.njol.skript.expressions;

import ch.njol.skript.lang.util.common.AnyUUID;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

import java.util.UUID;

@Name("UUID")
@Description("The UUID of a player, entity or world.")
@Examples({
	"# prevents people from joining the server if they use the name of a player",
	"# who has played on this server at least once since this script has been added",
	"on login:",
		"\tif {uuid::%name of player%} exists:",
			"\t\t{uuid::%name of player%} is not uuid of player",
			"\t\tkick player due to \"Someone with your name has played on this server before\"",
		"\telse:",
			"\t\tset {uuid::%name of player%} to uuid of player",
	"",
	"command /what-is-my-uuid:",
		"\ttrigger:",
			"\t\tset {_uuid} to uuid of player",
			"\t\tsend \"Your UUID is '%string within {_uuid}%'\"",
})
@Since("2.1.2, 2.2 (offline players' uuids), 2.2-dev24 (other entities' uuids)")
public class ExprUUID extends SimplePropertyExpression<AnyUUID, UUID> {

	static {
		register(ExprUUID.class, UUID.class, "UUID", "identifiable");
	}

	@Override
	public @Nullable UUID convert(AnyUUID identifiable) {
		if (identifiable.isOfflinePlayer()) {
			try {
				return identifiable.uuid();
			} catch (UnsupportedOperationException e) {
				// Some plugins (ProtocolLib) try to emulate offline players, but fail miserably
				// They will throw this exception... and somehow server may freeze when this happens
				Skript.warning("A script tried to get uuid of an offline player, which was faked by another plugin (probably ProtocolLib).");
				e.printStackTrace();
				return null;
			}
		}

		return identifiable.uuid();
	}

	@Override
	public Class<? extends UUID> getReturnType() {
		return UUID.class;
	}

	@Override
	protected String getPropertyName() {
		return "UUID";
	}

}
