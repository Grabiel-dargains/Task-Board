package com.taskboard.model;

public class Board {
    private int id;
    private String name;

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "Board [ID=" + id + ", Nome=" + name + "]";
    }
}