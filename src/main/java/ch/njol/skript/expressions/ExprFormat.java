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
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
 
import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;

public class ExprFormat extends SimpleExpression<String> {
    static {
            Skript.registerExpression(ExprFormat.class, String.class, ExpressionType.COMBINED, "%string% [then] formatt(ing|ed) [as] %objects%");
    }

    public Expression<String> msg;
    public Expression<Object> formats;
 

    public String formatString(String str, ArrayList<Object> objs) {
        for (int i=0; i<objs.size();i++) {
            String replacement = (String) objs.get(i).toString();
            if (replacement == "^s") {replacement="%^s_%";}
            str = StringUtils.replaceOnce(str, "^s", replacement);
        } str=str.replaceAll("%^s_%","_^s"); return str;
    }
    @Override
    protected @Nullable String[] get(Event event) {
        String str = msg.getSingle(event);
        Object[] format = formats.getArray(event);
        if (!str.contains("^s")) {Skript.error("Your formatted text needs to contain a ^s"); return new String[]{str};}
        return new String[]{formatString(str,new ArrayList<Object>(Arrays.asList(format)))};
    }
    @Override
    public boolean isSingle() {
        return true;
    }
    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.msg = (Expression<String>) expressions[0];
        this.formats = (Expression<Object>) LiteralUtils.defendExpression(expressions[1]);
        return true;
    }
    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return null;
    }

}
