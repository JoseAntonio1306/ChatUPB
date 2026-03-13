package edu.upb.chatupb_v2.model.analizador;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PalabrasVulgares implements IAnalizadorTexto {

    private static final List<String> palabrasProhibidas = Arrays.asList(
            "said",
            "ajo",
            "adri",
            "pendejo",
            "santiago"
    );

    @Override
    public boolean aplica(String text) {
        if (text == null || text.isBlank()) return false;

        for (String word : palabrasProhibidas) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String proceso(String text) {
        if (text == null) return null;

        String result = text;

        for (String word : palabrasProhibidas) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(result);

            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String found = matcher.group();
                matcher.appendReplacement(sb, remplazar(found.length()));
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }

        return result;
    }

    private String remplazar(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("*");
        }
        return sb.toString();
    }
}