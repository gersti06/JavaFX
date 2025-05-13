package org.example.demo.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Player {
    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty score = new SimpleIntegerProperty(0);
    private int id; // Used for database persistence
    
    public Player(String name) {
        this.name.set(name);
    }
    
    public Player(int id, String name, int score) {
        this.id = id;
        this.name.set(name);
        this.score.set(score);
    }
    
    public String getName() {
        return name.get();
    }
    
    public void setName(String name) {
        this.name.set(name);
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public int getScore() {
        return score.get();
    }
    
    public void setScore(int score) {
        this.score.set(score);
    }
    
    public IntegerProperty scoreProperty() {
        return score;
    }
    
    public void incrementScore() {
        score.set(score.get() + 1);
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        return getName() + " - Score: " + getScore();
    }
}