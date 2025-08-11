package com.taskboard.model;

import java.time.LocalDateTime;

public class Card {
    private int id;
    private String title;
    private String description;
    private LocalDateTime creationDate;
    private boolean isBlocked;
    private int columnId;

    
public int getId(){
    return id;
}

public String getTitle(){
    return title;
}

public String getDescription(){
    return description;
}

public LocalDateTime getCreationDate(){
    return creationDate;
}

public boolean getIsBlocked(){
    return isBlocked;
}

public int getColumnId(){
    return columnId;
}

public void setTitle(String newtitle){
    this.title = newtitle;
}

public void setDescription(String newDescription){
    this.description = newDescription;
}

public void setBlock(boolean block){
    this.isBlocked = block;
}

public void setID(int id){
    this.id = id;
}

}