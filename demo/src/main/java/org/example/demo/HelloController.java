package org.example.demo;
        
        import javafx.application.Platform;
        import javafx.fxml.FXML;
        import javafx.fxml.Initializable;
        import javafx.scene.control.Alert;
        import javafx.scene.control.ListView;
        import javafx.scene.input.KeyCode;
        import javafx.scene.input.KeyEvent;
        import javafx.scene.layout.TilePane;
        import javafx.scene.layout.StackPane;
        import javafx.scene.control.Label;
        import javafx.scene.control.TextField;
        import javafx.scene.control.Button;
        import javafx.beans.binding.Bindings;
        import javafx.beans.value.ChangeListener;
        import javafx.geometry.Pos;
        
        import org.example.demo.model.Card;
        import org.example.demo.model.DatabaseService;
        import org.example.demo.model.MemoryGame;
        import org.example.demo.model.Player;
        
        import java.net.URL;
        import java.util.ResourceBundle;
        import java.util.Timer;
        import java.util.TimerTask;
        import java.util.List;
        
        public class HelloController implements Initializable {
            @FXML
            private Label welcomeText;
        
            @FXML
            private TilePane cardTilePane;
        
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
                game = new MemoryGame();
                dbService = new DatabaseService();
                setupBindings();
                setupCardTilePane();
                attachGameListeners();
                setupPlayerListView();
                loadPlayersFromDatabase();
                addKeyEventHandlers();
            }
        
            private void setupBindings() {
                currentPlayerLabel.textProperty().bind(
                    Bindings.createStringBinding(() -> {
                        Player player = game.getCurrentPlayer();
                        return player != null ? "Current Player: " + player.getName() : "No player";
                    }, game.currentPlayerIndexProperty())
                );
        
                triesLabel.textProperty().bind(
                    Bindings.createStringBinding(() ->
                        "Tries: " + game.triesProperty().get(),
                        game.triesProperty())
                );
        
                matchedPairsLabel.textProperty().bind(
                    Bindings.createStringBinding(() ->
                        "Matched Pairs: " + game.matchedPairsProperty().get() + "/" + (game.getCards().size() / 2),
                        game.matchedPairsProperty())
                );
        
                game.gameOverProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        showGameOverDialog();
                        savePlayersToDatabase();
                    }
                });
            }
        
            private void setupCardTilePane() {
                cardTilePane.getChildren().clear();
                int index = 0;
                for (Card card : game.getCards()) {
                    StackPane pane = createCardPane(card, index++);
                    cardTilePane.getChildren().add(pane);
                }
            }
        
            private void attachGameListeners() {
                game.getCards().addListener((javafx.collections.ListChangeListener<Card>) change -> {
                    setupCardTilePane();
                });
            }
        
            private StackPane createCardPane(Card card, int index) {
                StackPane pane = new StackPane();
                pane.setPrefSize(100, 150);
                pane.setAlignment(Pos.CENTER);
                Label label = new Label(card.toString());
                label.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: black;");
                pane.getChildren().add(label);
                updateCardStyle(pane, card);
                ChangeListener<Boolean> styleListener = (obs, o, n) -> updateCardStyle(pane, card);
                card.flippedProperty().addListener(styleListener);
                card.matchedProperty().addListener(styleListener);
                ChangeListener<Boolean> textListener = (obs, o, n) -> label.setText(card.toString());
                card.flippedProperty().addListener(textListener);
                card.matchedProperty().addListener(textListener);
                pane.setOnMouseClicked(e -> handleCardClick(index));
                return pane;
            }
        
            private void updateCardStyle(StackPane pane, Card card) {
                if (card.isMatched()) {
                    pane.setStyle("-fx-background-color: lightgreen; -fx-border-color: black;");
                } else if (card.isFlipped()) {
                    pane.setStyle("-fx-background-color: white; -fx-border-color: black;");
                } else {
                    pane.setStyle("-fx-background-color: darkblue; -fx-border-color: black;");
                }
            }
        
            private void setupPlayerListView() {
                playersListView.setItems(game.getPlayers());
            }
        
            private void addKeyEventHandlers() {
                cardTilePane.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                        // Handle keyboard navigation if needed
                        event.consume();
                    }
                });
        
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
                if (flipBackTimer != null) {
                    flipBackTimer.cancel();
                    flipBackTimer = null;
                }
                game.initializeCards(8);
                setupCardTilePane();
            }
        
            private void handleCardClick(int index) {
                if (game.isWaitingForFlipBack()) return;
                game.flipCard(index);
                setupCardTilePane();
                if (game.isWaitingForFlipBack()) {
                    scheduleFlipBack();
                }
            }
        
            private void scheduleFlipBack() {
                if (flipBackTimer != null) flipBackTimer.cancel();
                flipBackTimer = new Timer();
                flipBackTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            game.flipCardsBack();
                            setupCardTilePane();
                        });
                    }
                }, 1000);
            }
        
            private void showGameOverDialog() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Game Over");
                alert.setHeaderText("Game Over");
        
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
                if (flipBackTimer != null) {
                    flipBackTimer.cancel();
                }
        
                savePlayersToDatabase();
                dbService.close();
            }
        }