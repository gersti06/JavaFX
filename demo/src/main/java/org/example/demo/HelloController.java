package org.example.demo;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.example.demo.model.Card;
import org.example.demo.model.DatabaseService;
import org.example.demo.model.MemoryGame;
import org.example.demo.model.Player;

import java.net.URL;
import java.util.*;

public class HelloController implements Initializable {
    @FXML
    private Label welcomeText;
    
    @FXML
    private GridPane cardGrid;
    
    @FXML
    private ListView<Card> cardListView;
    
    @FXML
    private ListView<Player> playersListView;
    
    @FXML
    private Label currentPlayerLabel;
    
    @FXML
    private Label triesLabel;
    
    @FXML
    private Label matchedPairsLabel;
    
    @FXML
    private TextField playerNameField;
    
    @FXML
    private Button addPlayerButton;
    
    @FXML
    private Button newGameButton;
    
    private MemoryGame game;
    private DatabaseService dbService;
    
    private Timer flipBackTimer;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the game model
        game = new MemoryGame();
        
        // Initialize the database service
        dbService = new DatabaseService();
        
        // Set up bindings
        setupBindings();
        
        // Set up ListView for cards
        setupCardListView();
        // Attach listeners to card properties for UI refresh
        attachCardListeners();
        
        // Set up ListView for players
        setupPlayerListView();
        
        // Load players from database
        loadPlayersFromDatabase();
        
        // Add key event handler for keyboard navigation
        addKeyEventHandlers();
    }
    
    private void setupBindings() {
        // Current player binding
        currentPlayerLabel.textProperty().bind(
            Bindings.createStringBinding(() -> {
                Player player = game.getCurrentPlayer();
                return player != null ? "Current Player: " + player.getName() : "No player";
            }, game.currentPlayerIndexProperty())
        );
        
        // Tries binding
        triesLabel.textProperty().bind(
            Bindings.createStringBinding(() -> 
                "Tries: " + game.triesProperty().get(),
                game.triesProperty())
        );
        
        // Matched pairs binding
        matchedPairsLabel.textProperty().bind(
            Bindings.createStringBinding(() -> 
                "Matched Pairs: " + game.matchedPairsProperty().get() + "/" + (game.getCards().size() / 2),
                game.matchedPairsProperty())
        );
        
        // Game over binding
        game.gameOverProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                showGameOverDialog();
                savePlayersToDatabase();
            }
        });
    }
    
    private void setupCardListView() {
        cardListView.setItems(game.getCards());
        cardListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Card card, boolean empty) {
                super.updateItem(card, empty);
                if (empty || card == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(card.toString());
                    if (card.isMatched()) {
                        setStyle("-fx-background-color: lightgreen;");
                    } else if (card.isFlipped()) {
                        setStyle("-fx-background-color: lightblue;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Mouse and key handlers remain unchanged
        cardListView.setOnMouseClicked(event -> {
            int index = cardListView.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                handleCardClick(index);
            }
        });
    }
    
    private void setupPlayerListView() {
        playersListView.setItems(game.getPlayers());
    }
    
    private void addKeyEventHandlers() {
        // Add keyboard handler to the card list view
        cardListView.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                int selectedIndex = cardListView.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0) {
                    handleCardClick(selectedIndex);
                }
                event.consume();
            }
        });
        
        // Add keyboard handler to the player name field
        playerNameField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addPlayer();
                event.consume();
            }
        });
    }
    
    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
    
    @FXML
    protected void addPlayer() {
        String name = playerNameField.getText().trim();
        if (!name.isEmpty()) {
            game.addPlayer(new Player(name));
            playerNameField.clear();
        }
    }
    
    @FXML
    protected void newGame() {
        // Cancel any pending flip back timer
        if (flipBackTimer != null) {
            flipBackTimer.cancel();
            flipBackTimer = null;
        }
        
        game.initializeCards(8); // 8 pairs = 16 cards
        attachCardListeners();
        cardListView.refresh();
    }
    
    private void handleCardClick(int index) {
        // Kann die Karte aufgedeckt werden?
        if (game.isWaitingForFlipBack()) {
            // Warte bis die Karten umgedreht werden
            return;
        }
        
        game.flipCard(index);
        // Update UI immediately
        cardListView.refresh();
        
        // Wenn zwei Karten aufgedeckt wurden und nicht übereinstimmen,
        // drehe sie nach einer Verzögerung wieder um
        if (game.isWaitingForFlipBack()) {
            scheduleFlipBack();
        }
    }
    
    private void scheduleFlipBack() {
        if (flipBackTimer != null) {
            flipBackTimer.cancel();
        }
        
        flipBackTimer = new Timer();
        flipBackTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    game.flipCardsBack();
                    cardListView.refresh();
                });
            }
        }, 1000); // 1 Sekunde Verzögerung
    }
    
    private void showGameOverDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Game Over");
        
        // Find the winner
        Player winner = null;
        int maxScore = -1;
        
        for (Player player : game.getPlayers()) {
            if (player.getScore() > maxScore) {
                maxScore = player.getScore();
                winner = player;
            }
        }
        
        String resultMessage;
        if (winner != null) {
            resultMessage = "Winner: " + winner.getName() + " with " + winner.getScore() + " pairs!";
        } else {
            resultMessage = "Game Over!";
        }
        
        alert.setContentText(resultMessage + "\nTotal tries: " + game.triesProperty().get());
        
        alert.showAndWait();
    }
    
    private void loadPlayersFromDatabase() {
        List<Player> players = dbService.loadPlayers();
        
        // Add loaded players only if they don't already exist
        for (Player player : players) {
            boolean exists = false;
            for (Player existingPlayer : game.getPlayers()) {
                if (existingPlayer.getName().equals(player.getName())) {
                    exists = true;
                    break;
                }
            }
            
            if (!exists) {
                game.addPlayer(player);
            }
        }
    }
    
    private void savePlayersToDatabase() {
        for (Player player : game.getPlayers()) {
            dbService.savePlayer(player);
        }
    }
    
    public void shutdown() {
        // Clean up resources, save state, etc.
        if (flipBackTimer != null) {
            flipBackTimer.cancel();
        }
        
        savePlayersToDatabase();
        dbService.close();
    }
    
    // Attach listeners to all cards for automatic ListView refresh
    private void attachCardListeners() {
        // Clear any existing listeners by recreating UI (cards list is new on newGame)
        game.getCards().forEach(card -> {
            card.flippedProperty().addListener((obs, oldVal, newVal) -> cardListView.refresh());
            card.matchedProperty().addListener((obs, oldVal, newVal) -> cardListView.refresh());
        });
        // Listen to list changes to attach listeners to new cards
        game.getCards().addListener((javafx.collections.ListChangeListener<Card>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Card card : change.getAddedSubList()) {
                        card.flippedProperty().addListener((obs, oldVal, newVal) -> cardListView.refresh());
                        card.matchedProperty().addListener((obs, oldVal, newVal) -> cardListView.refresh());
                    }
                }
            }
        });
    }
}