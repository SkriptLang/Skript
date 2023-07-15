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
package ch.njol.skript.hooks.regions.classes;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.YggdrasilSerializer;
import ch.njol.skript.hooks.regions.RegionsPlugin;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.lang.converter.Converters;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.Iterator;

public abstract class Region implements YggdrasilExtendedSerializable {

	static {
		Classes.registerClass(new ClassInfo<>(Region.class, "region")
				.name("Region")
				.description("A region of a regions plugin. Skript currently supports WorldGuard, Factions, GriefPrevention and PreciousStones.",
						"Please note that some regions plugins do not have named regions, some use numerical ids to identify regions, and some may have regions with the same name in different worlds, "
								+ "thus using regions like \"region name\" in scripts may or may not work.")
				.usage("\"region name\"")
				.examples("")
				.after("string", "world", "offlineplayer", "player")
				.since("2.1")
				.user("regions?")
				.requiredPlugins("Supported regions plugin")
				.parser(new Parser<Region>() {
					@Override
					@Nullable
					public Region parse(String name, ParseContext context) {
						final boolean quoted;
						switch (context) {
							case DEFAULT:
							case EVENT:
							case SCRIPT:
								quoted = true;
								break;
							case COMMAND:
							case CONFIG:
								quoted = false;
								break;
							default:
								assert false;
								return null;
						}
						if (!VariableString.isQuotedCorrectly(name, quoted))
							return null;
						name = VariableString.unquote(name, quoted);
						return Region.parse(name, true);
					}
					
					@Override
					public String toString(Region region, int flags) {
						return region.toString();
					}
					
					@Override
					public String toVariableNameString(Region region) {
						return region.toString();
					}
                })
				.serializer(new YggdrasilSerializer<Region>() {
					@Override
					public boolean mustSyncDeserialization() {
						return true;
					}
				}));
		Converters.registerConverter(String.class, Region.class, name -> Region.parse(name, false));
	}

	@Nullable
	private static Region parse(String name, boolean error) {
		Region region = null;
		for (World world : Bukkit.getWorlds()) {
			Region current = RegionsPlugin.getRegion(world, name);
			if (current == null)
				continue;
			if (region != null) {
				if (error)
					Skript.error("Multiple regions with the name '" + name + "' exist");
				return null;
			}
			region = current;
		}

		if (region == null) {
			if (error)
				Skript.error("Region '" + name + "' could not be found");
			return null;
		}

		return region;
	}

	public abstract boolean contains(Location location);

	public abstract boolean isMember(OfflinePlayer player);

	public abstract Collection<OfflinePlayer> getMembers();

	public abstract boolean isOwner(OfflinePlayer player);

	public abstract Collection<OfflinePlayer> getOwners();

	public abstract Iterator<Block> getBlocks();

	@Override
	public abstract String toString();

	public abstract RegionsPlugin getPlugin();

	@Override
	public abstract boolean equals(@Nullable Object other);

	@Override
	public abstract int hashCode();

}
