package com.vocabapp.domain.model;

public class Tag {
    public final long id;
    public final String name;
    public final String colorHex;

    public Tag(long id, String name, String colorHex) {
        this.id = id;
        this.name = name;
        this.colorHex = colorHex;
    }
}
