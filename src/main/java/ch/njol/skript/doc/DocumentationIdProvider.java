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
package ch.njol.skript.doc;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.function.Function;

import java.util.HashMap;
import java.util.Map;

public class DocumentationIdProvider {

	private Map<String, Integer> idCollisionCounter = new HashMap<>();


	private String addCollisionSuffix(String id) {
		Integer collisionCount = idCollisionCounter.get(id);
		idCollisionCounter.merge(id, 2, Integer::sum);
		if (collisionCount == null) {
			return id;
		}
		return id + "-" + collisionCount;
	}

	public String getId(Class<?> clazz) {
		DocumentationId documentationIdAnnotation = clazz.getAnnotation(DocumentationId.class);
		if (documentationIdAnnotation == null) {
			return addCollisionSuffix(clazz.getSimpleName());
		}
		return addCollisionSuffix(documentationIdAnnotation.value());
	}

	public String getId(Function<?> function) {
		return addCollisionSuffix(function.getName());
	}

	public String getId(ClassInfo<?> classInfo) {
		String explicitlyDefinedId = classInfo.getDocumentationID();
		if (explicitlyDefinedId != null) {
			return addCollisionSuffix(explicitlyDefinedId);
		}
		return addCollisionSuffix(classInfo.getCodeName());
	}

	public String getId(SkriptEventInfo<?> eventInfo) {
		String explicitlyDefinedId = eventInfo.getDocumentationID();
		if (explicitlyDefinedId != null) {
			return addCollisionSuffix(explicitlyDefinedId);
		}
		return addCollisionSuffix(eventInfo.getId());
	}

}
