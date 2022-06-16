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

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;
 
import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
public class ExprGetAllArmor extends SimpleExpression<ItemStack> {
 
   static {
       Skript.registerExpression(ExprGetAllArmor.class, ItemStack.class, ExpressionType.COMBINED, "%player%'s armor");
   }
 
   private Expression<Player> player;
 
   @Override
   public Class<? extends ItemStack> getReturnType() {
       return ItemStack.class;
   }
 
   @Override
   public boolean isSingle() {
       return true;
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
       player = (Expression<Player>) exprs[0];
       return true;
   }
 
   @Override
   public String toString(@Nullable Event event, boolean debug) {
       return "Example expression with expression player: " + player.toString(event, debug);
   }
 
   @Override
   protected @Nullable ItemStack[] get(Event event) {
       Player p = player.getSingle(event);
       if (p != null) {
            org.bukkit.inventory.PlayerInventory inv = p.getInventory();
            ItemStack[] armor={inv.getHelmet(),inv.getChestplate(),inv.getLeggings(),inv.getBoots()};
            return armor;
       }
       return null;
   }
}
