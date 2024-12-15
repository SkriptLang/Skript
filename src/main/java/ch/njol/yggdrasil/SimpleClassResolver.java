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
package ch.njol.yggdrasil;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class SimpleClassResolver implements ClassResolver {
	
	private final BiMap<Class<?>, String> classes = HashBiMap.create();
	
	public void registerClass(Class<?> type, String id) {
		String oldId = classes.put(type, id);
		if (oldId != null && !oldId.equals(id))
			throw new YggdrasilException("Changed id of " + type + " from " + oldId + " to " + id);
	}
	
	@Override
	@Nullable
	public Class<?> getClass(String id) {
		return classes.inverse().get(id);
	}
	
	@Override
	@Nullable
	public String getID(Class<?> type) {
		return classes.get(type);
	}
	
}
