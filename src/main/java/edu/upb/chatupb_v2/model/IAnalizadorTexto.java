package edu.upb.chatupb_v2.model;

public interface IAnalizadorTexto {

    boolean aplica(String text);
    String proceso(String text);
}
