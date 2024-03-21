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
package ch.njol.skript.expressions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Permissions")
@Description({
	"All permissions of the provided permissible(s). A permissible is anything that can have permissions (like the player).",
	"A couple of notes: Plugins like Skript cannot modify the default permissions such as bukkit.* permissions.",
	"Skript also doesn't save the permissions after server restart.",
	"You will need a permissions plugin or to save them yourself in variables."
})
@Examples("set {_permissions::*} to all permissions of the player")
@Since("2.2-dev33, INSERT VERSION (changers)")
public class ExprPermissions extends PropertyExpression<Entity, String> {

	static {
		Skript.registerExpression(ExprPermissions.class, String.class, ExpressionType.PROPERTY,
				"[all [[of] the]|the] permissions (from|of) %entities%",
				"[all [of] the] %entities%'[s] permissions"
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<Entity>) exprs[0]);
		return true;
	}

	@Override
	protected String[] get(Event event, Entity[] source) {
		return getExpr().stream(event)
				.flatMap(permissible -> permissible.getEffectivePermissions().stream())
				.map(permission -> permission.getPermission())
				.toArray(String[]::new);
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.ADD || mode == ChangeMode.REMOVE || mode == ChangeMode.DELETE)
			return CollectionUtils.array(String[].class);
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		List<String> permissions = new ArrayList<>();
		if (mode != ChangeMode.DELETE) {
			if (delta == null)
				return;
			for (Object permission : delta) {
				if (permission == null)
					continue;
				permissions.add((String) permission);
			}
		}
		if (mode == ChangeMode.DELETE || mode == ChangeMode.REMOVE) {
			for (Entity entity : getExpr().getArray(event)) {
				for (PermissionAttachmentInfo info : entity.getEffectivePermissions()) {
					PermissionAttachment attachment = info.getAttachment();
					if (attachment == null)
						continue;
					for (String permission : attachment.getPermissions().keySet()) {
						if (mode == ChangeMode.DELETE) {
							attachment.unsetPermission(permission);
						} else if (permissions.contains(permission)) {
							attachment.unsetPermission(permission);
						}
					}
				}
			}
			return;
		}
		for (Entity entity : getExpr().getAll(event)) {
			PermissionAttachment attachment = getPermission(entity);
			for (String permission : permissions)
				attachment.setPermission(permission, true);
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "permissions of " + getExpr().toString(event, debug);
	}

	/**
	 * Grabs the PermissionAttachment added by Skript.
	 * 
	 * @param entity The entity to grab the attachment from
	 * @return PermissionAttachment with Skript as the plugin
	 */
	private PermissionAttachment getPermission(Entity entity) {
		Skript instance = Skript.getInstance();
		for (PermissionAttachmentInfo info : entity.getEffectivePermissions()) {
			PermissionAttachment attachment = info.getAttachment();
			if (attachment == null)
				continue;
			if (attachment.getPlugin().equals(instance))
				return attachment;
		}
		return entity.addAttachment(instance);
	}

}
