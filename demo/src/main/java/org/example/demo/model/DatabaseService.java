package org.example.demo.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {
    private static final String DB_URL = "jdbc:h2:./memory-game";
    private static final String USER = "sa";
    private static final String PASS = "";

    private Connection connection;

    public DatabaseService() {
        initDatabase();
    }

    private void initDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            createTablesIfNotExist();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTablesIfNotExist() {
        try (Statement stmt = connection.createStatement()) {
            // KI-Assist: Automatisch generiertes Datenbankschema für Spielstand-Persistenz
            stmt.execute("CREATE TABLE IF NOT EXISTS players (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "score INT DEFAULT 0" +
                    ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePlayer(Player player) {
        String sql;
        if (player.getId() == 0) {
            // New player
            sql = "INSERT INTO players (name, score) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, player.getName());
                stmt.setInt(2, player.getScore());
                stmt.executeUpdate();

                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    player.setId(rs.getInt(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Update existing player
            sql = "UPDATE players SET name = ?, score = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, player.getName());
                stmt.setInt(2, player.getScore());
                stmt.setInt(3, player.getId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Player> loadPlayers() {
        List<Player> players = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, score FROM players ORDER BY score DESC")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int score = rs.getInt("score");
                players.add(new Player(id, name, score));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return players;
    }

    public void resetAllScores() {
        String sql = "UPDATE players SET score = 0";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePlayer(int id) {
        String sql = "DELETE FROM players WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}