package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.analizador.ExpresionMatematica;
import edu.upb.chatupb_v2.model.analizador.PalabrasVulgares;
import edu.upb.chatupb_v2.model.analizador.IAnalizadorTexto;

import java.util.ArrayList;
import java.util.List;

public class AnalizadorController {

    private final List<IAnalizadorTexto> strategies = new ArrayList<>();

    public AnalizadorController() {
        strategies.add(new ExpresionMatematica());
        strategies.add(new PalabrasVulgares());
    }

    public String analyze(String text) {
        for (IAnalizadorTexto strategy : strategies) {
            if (strategy.aplica(text)) {
                return strategy.proceso(text);
            }
        }
        return text;
    }
}