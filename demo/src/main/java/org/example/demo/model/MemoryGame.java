package org.example.demo.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

public class MemoryGame {
    private final ObservableList<Card> cards = FXCollections.observableArrayList();
    private final ObservableList<Player> players = FXCollections.observableArrayList();
    private final IntegerProperty currentPlayerIndex = new SimpleIntegerProperty(0);
    private final IntegerProperty tries = new SimpleIntegerProperty(0);
    private final IntegerProperty matchedPairs = new SimpleIntegerProperty(0);
    private final BooleanProperty gameOver = new SimpleBooleanProperty(false);
    
    private Card firstCard = null;
    private int firstCardIndex = -1;
    private Card secondCard = null;
    private int secondCardIndex = -1;
    private boolean waitingForFlipBack = false;
    
    public MemoryGame() {
        // Initialize with default values
        players.add(new Player("Player 1"));
        initializeCards(8); // 8 pairs = 16 cards
    }
    
    public void initializeCards(int pairs) {
        cards.clear();
        
        List<String> cardValues = generateCardValues(pairs);
        
        for (String value : cardValues) {
            cards.add(new Card(value));
        }
        
        // Reset game state
        matchedPairs.set(0);
        tries.set(0);
        gameOver.set(false);
        firstCard = null;
        firstCardIndex = -1;
        secondCard = null;
        secondCardIndex = -1;
        waitingForFlipBack = false;
    }
    
    private List<String> generateCardValues(int pairs) {
        // Simple implementation: using letters as card values
        List<String> values = new ArrayList<>();
        for (int i = 0; i < pairs; i++) {
            char c = (char) ('A' + i);
            values.add(Character.toString(c));
            values.add(Character.toString(c));
        }
        
        // Shuffle the cards
        Collections.shuffle(values);
        return values;
    }
    
    public void flipCard(int index) {
        if (index < 0 || index >= cards.size()) {
            return;
        }
        
        Card card = cards.get(index);
        
        // Can't flip a card that's already matched or flipped
        if (card.isMatched() || card.isFlipped() || waitingForFlipBack) {
            return;
        }
        
        // Flip the card
        card.setFlipped(true);
        
        // Check if this is the first or second card being flipped
        if (firstCard == null) {
            // First card flipped
            firstCard = card;
            firstCardIndex = index;
        } else {
            // Second card flipped
            secondCard = card;
            secondCardIndex = index;
            tries.set(tries.get() + 1);
            
            // Check for a match
            if (firstCard.getValue().equals(card.getValue())) {
                // Match found
                firstCard.setMatched(true);
                card.setMatched(true);
                getCurrentPlayer().incrementScore();
                matchedPairs.set(matchedPairs.get() + 1);
                
                // Check if game is over
                if (matchedPairs.get() == cards.size() / 2) {
                    gameOver.set(true);
                }
                
                // Reset cards for next turn
                resetFlippedCards();
            } else {
                // No match, mark for flip back and switch player
                waitingForFlipBack = true;
                nextPlayer();
            }
        }
    }
    
    public void flipCardsBack() {
        if (firstCard != null && !firstCard.isMatched()) {
            firstCard.setFlipped(false);
        }
        
        if (secondCard != null && !secondCard.isMatched()) {
            secondCard.setFlipped(false);
        }
        
        resetFlippedCards();
    }
    
    private void resetFlippedCards() {
        firstCard = null;
        firstCardIndex = -1;
        secondCard = null;
        secondCardIndex = -1;
        waitingForFlipBack = false;
    }
    
    public boolean isWaitingForFlipBack() {
        return waitingForFlipBack;
    }
    
    public int[] getFlippedCardIndices() {
        if (firstCardIndex >= 0 && secondCardIndex >= 0) {
            return new int[] { firstCardIndex, secondCardIndex };
        } else if (firstCardIndex >= 0) {
            return new int[] { firstCardIndex };
        }
        return new int[0];
    }
    
    public void flipCardBack(int index) {
        if (index >= 0 && index < cards.size()) {
            Card card = cards.get(index);
            if (!card.isMatched()) {
                card.setFlipped(false);
            }
        }
    }
    
    public Player getCurrentPlayer() {
        if (players.isEmpty()) {
            return null;
        }
        return players.get(currentPlayerIndex.get());
    }
    
    public void nextPlayer() {
        if (players.size() > 1) {
            currentPlayerIndex.set((currentPlayerIndex.get() + 1) % players.size());
        }
    }
    
    public void addPlayer(Player player) {
        players.add(player);
    }
    
    public ObservableList<Card> getCards() {
        return cards;
    }
    
    public ObservableList<Player> getPlayers() {
        return players;
    }
    
    public IntegerProperty currentPlayerIndexProperty() {
        return currentPlayerIndex;
    }
    
    public IntegerProperty triesProperty() {
        return tries;
    }
    
    public IntegerProperty matchedPairsProperty() {
        return matchedPairs;
    }
    
    public BooleanProperty gameOverProperty() {
        return gameOver;
    }
}