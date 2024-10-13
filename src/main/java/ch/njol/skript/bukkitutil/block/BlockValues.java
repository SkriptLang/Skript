/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
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
