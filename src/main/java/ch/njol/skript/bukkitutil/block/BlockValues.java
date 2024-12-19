package ch.njol.skript.bukkitutil.block;

import ch.njol.skript.aliases.MatchQuality;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import org.jetbrains.annotations.Nullable;

/**
 * Contains all data block has that is needed for comparisions.
 */
public abstract class BlockValues implements YggdrasilExtendedSerializable {
	
	public abstract boolean isDefault();
	
	public abstract MatchQuality match(BlockValues other);
	
	@Override
	public abstract boolean equals(@Nullable Object other);
	
	@Override
	public abstract int hashCode();
	
}
