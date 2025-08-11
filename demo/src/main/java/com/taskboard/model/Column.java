package com.taskboard.model;

public class Column {
    private int id;
    private String name;
    private int boardId;
    private int columnOrder;
    private ColumnType type;

    public int getId(){
        return id;
    }

    public int getBoardId() {
        return boardId;
    }

    public int getColumnOrder() {
        return columnOrder;
    }

    public ColumnType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String nomeColuna){
        this.name = nomeColuna;
    }

    public void setID(int id){
        this.id = id;
    }

    public void setColumnOrder(int order){
        this.columnOrder = order;
    }

    public void setBoardId(int id){
        this.boardId = id;
    }

    public void setType(ColumnType type){
        this.type = type;
    }

}