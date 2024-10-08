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

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

@Name("Entity Bucket")
@Description("Gets the bucket that the Entity will be put into such as 'puffer fish bucket'.")
@Examples({
	"on bucket capture entity:",
		"\tif entity bucket is salmon bucket:",
			"\t\tsend \"Congratulations you now have a salmon bucket!\" to player"
})
@Events("Bucket Capture Entity")
@Since("INSERT VERSION")
public class ExprEntityBucket extends SimpleExpression<ItemStack> {

	static {
		if (Skript.classExists("org.bukkit.event.player.PlayerBucketEntityEvent"))
			Skript.registerExpression(ExprEntityBucket.class, ItemStack.class, ExpressionType.SIMPLE, "[the] entity bucket");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerBucketEntityEvent.class)) {
			Skript.error("The 'entity bucket' expression can only be used in the bucket capture entity event");
			return false;
		}
		return true;
	}

	@Override
	protected @Nullable ItemStack[] get(Event event) {
		if (!(event instanceof PlayerBucketEntityEvent))
			return null;
		return new ItemStack[]{((PlayerBucketEntityEvent) event).getEntityBucket()};
	}

	@Override
	public Class<? extends ItemStack> getReturnType() {
		return ItemStack.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the entity bucket";
	}

}
