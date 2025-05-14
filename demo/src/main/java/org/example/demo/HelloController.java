package org.example.demo;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.RotateTransition;
import javafx.scene.transform.Rotate;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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

    @FXML
    private Label timerLabel;

    @FXML
    private ComboBox<Integer> pairsComboBox;

    private MemoryGame game;
    private DatabaseService dbService;

    private Timer flipBackTimer;
    private Timeline timer;
    private IntegerProperty elapsedTime;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        game = new MemoryGame();
        dbService = new DatabaseService();
        setupBindings();
        setupTimer();
        setupCardListView();
        attachGameListeners();
        setupPlayerListView();
        loadPlayersFromDatabase();
        addKeyEventHandlers();
        // initialize pairs selection
        pairsComboBox.getItems().addAll(2, 4, 6, 8);
        pairsComboBox.setValue(8);
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

    private void setupTimer() {
        elapsedTime = new SimpleIntegerProperty(0);
        // Bind timer label to elapsed seconds
        timerLabel.textProperty().bind(elapsedTime.asString("Time: %ds"));
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> elapsedTime.set(elapsedTime.get() + 1)));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void setupCardListView() {
        cardListView.setItems(game.getCards());
        cardListView.setCellFactory(lv -> new ListCell<Card>() {
            private final StackPane pane = new StackPane();
            private final ImageView frontImageView = new ImageView();
            private final ImageView backImageView = new ImageView(
                new Image(getClass().getResourceAsStream("/images/backside.png"))
            );

            {
                pane.getChildren().addAll(backImageView, frontImageView);
                pane.setPrefSize(80, 80);
                pane.setAlignment(Pos.CENTER);
                frontImageView.setFitWidth(80);
                frontImageView.setFitHeight(80);
                backImageView.setFitWidth(80);
                backImageView.setFitHeight(80);
                pane.setRotationAxis(Rotate.Y_AXIS);
                pane.setStyle("-fx-border-color: lightgray; -fx-background-color: white; -fx-border-radius: 5;");
            }

            @Override
            protected void updateItem(Card card, boolean empty) {
                super.updateItem(card, empty);
                if (empty || card == null) {
                    setGraphic(null);
                    return;
                }
                String value = card.getValue().toLowerCase();
                Image frontImage = new Image(
                    getClass().getResourceAsStream("/images/" + value + ".png")
                );
                frontImageView.setImage(frontImage);

                boolean showFront = card.isFlipped() || card.isMatched();
                frontImageView.setVisible(showFront);
                backImageView.setVisible(!showFront);
                setGraphic(pane);

                card.flippedProperty().addListener((obs, oldVal, newVal) -> 
                    performFlipAnimation(pane, frontImageView, backImageView, newVal)
                );
                setOnMouseClicked(e -> handleCardClick(getIndex()));
            }

            private void performFlipAnimation(StackPane pane, ImageView front, ImageView back, boolean flipped) {
                RotateTransition firstHalf = new RotateTransition(Duration.millis(150), pane);
                firstHalf.setFromAngle(0);
                firstHalf.setToAngle(90);
                RotateTransition secondHalf = new RotateTransition(Duration.millis(150), pane);
                secondHalf.setFromAngle(90);
                secondHalf.setToAngle(0);
                firstHalf.setOnFinished(evt -> {
                    front.setVisible(flipped);
                    back.setVisible(!flipped);
                    secondHalf.play();
                });
                firstHalf.play();
            }
        });
    }

    private void attachGameListeners() {
        game.getCards().addListener((javafx.collections.ListChangeListener<Card>) change -> setupCardListView());
    }

    private void setupPlayerListView() {
        playersListView.setItems(game.getPlayers());
        playersListView.setCellFactory(lv -> new ListCell<Player>() {
            @Override
            protected void updateItem(Player player, boolean empty) {
                super.updateItem(player, empty);
                if (empty || player == null) {
                    textProperty().unbind();
                    setText(null);
                } else {
                    // Bind text to name and score so it updates automatically
                    textProperty().bind(Bindings.createStringBinding(() ->
                        player.getName() + " - Score: " + player.getScore(),
                        player.nameProperty(), player.scoreProperty()));
                }
            }
        });
    }

    private void addKeyEventHandlers() {
        cardListView.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
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
        int pairs = pairsComboBox.getValue() != null ? pairsComboBox.getValue() : 8;
        game.initializeCards(pairs);
        setupCardListView();
        // reset timer
        if (elapsedTime != null) elapsedTime.set(0);
    }

    @FXML
    protected void exitGame() {
        Platform.exit();
    }

    @FXML
    protected void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Memory Game");
        alert.setContentText("Ein einfaches Memory-Spiel in JavaFX.");
        alert.showAndWait();
    }

    private void handleCardClick(int index) {
        if (game.isWaitingForFlipBack()) return;
        game.flipCard(index);
        setupCardListView();
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
                    setupCardListView();
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