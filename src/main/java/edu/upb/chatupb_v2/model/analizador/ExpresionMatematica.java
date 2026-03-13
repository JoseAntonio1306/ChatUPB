package edu.upb.chatupb_v2.model.analizador;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpresionMatematica implements IAnalizadorTexto{
    private static final Pattern MATH_PATTERN = Pattern.compile("^(\\d)\\+(\\d)=$");

    @Override
    public boolean aplica(String text) {
        if (text == null) return false;
        return MATH_PATTERN.matcher(text).matches();
    }

    @Override
    public String proceso(String text) {
        Matcher matcher = MATH_PATTERN.matcher(text);
        if (!matcher.matches()) {
            return text;
        }

        int a = Integer.parseInt(matcher.group(1));
        int b = Integer.parseInt(matcher.group(2));
        int result = a + b;

        return text + result;
    }
}
