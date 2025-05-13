package org.example.demo.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Card {
    private final StringProperty value = new SimpleStringProperty();
    private final BooleanProperty flipped = new SimpleBooleanProperty(false);
    private final BooleanProperty matched = new SimpleBooleanProperty(false);
    
    public Card(String value) {
        this.value.set(value);
    }
    
    public String getValue() {
        return value.get();
    }
    
    public StringProperty valueProperty() {
        return value;
    }
    
    public boolean isFlipped() {
        return flipped.get();
    }
    
    public void setFlipped(boolean flipped) {
        this.flipped.set(flipped);
    }
    
    public BooleanProperty flippedProperty() {
        return flipped;
    }
    
    public boolean isMatched() {
        return matched.get();
    }
    
    public void setMatched(boolean matched) {
        this.matched.set(matched);
    }
    
    public BooleanProperty matchedProperty() {
        return matched;
    }
    
    @Override
    public String toString() {
        if (isFlipped() || isMatched()) {
            return getValue();
        } else {
            return "?";
        }
    }
}