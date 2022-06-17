package ch.njol.skript.expressions;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.Nullable;
 
import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;

public class ExprFormat extends SimplePropertyExpression<String, String> {
   static {
        Skript.registerExpression(ExprFormat.class, String.class, ExpressionType.COMBINED, "%string% formatted as %strings%");
   }
 
    @Override
    protected String getPropertyName() {
        return "format";
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    public void formatString(String str, ArrayList<Object> objs) {
        // %s / replacement        


        for (int index = 0; index < ((CharSequence) objs).length(); index++) {

            String replacement = (String) objs.get(index);
                    
            int count = str.length() - str.replace("%s", "%" + replacement + "%").length();

        }
        


    }

    @Override
    public @Nullable String convert(String f) {
        // Check if formatted text contains a %s
        if (!f.contains("%s")) {Skript.error("Your formatted text needs to contain a %s");}
        
        return null;
    }

}
