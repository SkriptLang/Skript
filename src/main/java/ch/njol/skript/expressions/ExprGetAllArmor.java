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

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;
 
import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;

public class ExprArmor extends SimplePropertyExpression<LivingEntity, ItemStack> {
 
   static {
       Skript.registerExpression(ExprGetAllArmor.class, ItemStack[].class, ExpressionType.COMBINED, "%livingentities%'s armor");
   }
 
    @Override
    protected String getPropertyName() {
        return "armor";
    }

    @Override
    public Class<? extends ItemStack> getReturnType() {
        return ItemStack.class;
    }

    @Override
    @Nullable 
    public ItemStack convert(LivingEntity entity) {
        ItemStack[] armor=f.getEquipment().getArmorContents();
        return armor;
    }
}
