
# 🎴 MemoryFX – Das ultimative Memory-Spiel in JavaFX

Willkommen bei **MemoryFX**, einem modernen und unterhaltsamen Memory-Spiel, das mit **JavaFX** entwickelt wurde.  
Teste dein Gedächtnis, fordere deine Freunde heraus und genieße ein fesselndes Spielerlebnis mit diesem liebevoll gestalteten Desktop-Spiel.

---

## 🌟 Highlights

- **Intuitives Gameplay**: Perfekt für Spieler jeden Alters, mit einfacher Steuerung und klaren Regeln.  
- **Attraktive Benutzeroberfläche**: Wunderschönes Design mit flüssiger Animation und anpassbaren Themes.  
- **Verschiedene Schwierigkeitsstufen**: Vom Anfänger bis zum Memory-Profi – es ist für jeden etwas dabei!  
- **Highscore-System**: Verfolge deine Leistungen und versuche, deine Bestzeit zu schlagen.  
- **Multiplayer-Modus**: Spiele gegen Freunde und finde heraus, wer das beste Gedächtnis hat.

---

## 🎮 Spielregeln

### Ziel des Spiels
Finde alle zusammengehörigen Kartenpaare.

### Wie es funktioniert
1. Klicke auf eine Karte, um sie umzudrehen.  
2. Merke dir das Symbol und finde das passende Gegenstück.  
3. Drehe alle Paare um, um das Spiel zu gewinnen!

### Highscore
- Je schneller du bist, desto höher dein Score!
- Weniger Fehlversuche bedeuten mehr Punkte.

---

## 🏗️ Projektstruktur

```

MemoryFX/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/memoryfx/
│   │   │       ├── Main.java           # Einstiegspunkt der Anwendung
│   │   │       ├── GameController.java # Spiel-Logik und Events
│   │   │       ├── Card.java           # Karten-Klasse für das Spiel
│   │   │       └── Utils.java          # Hilfsfunktionen (z.B. Timer)
│   │   ├── resources/
│   │       ├── fxml/                   # FXML-Dateien für die UI
│   │       ├── css/                    # CSS-Dateien für das Styling
│   │       └── images/                 # Karten- und Hintergrundbilder
├── README.md                           # Projektbeschreibung
└── build.gradle                        # Gradle-Build-Skript

````

---

## 🚀 Installation und Ausführung

### Voraussetzungen
- **Java Development Kit (JDK)**: Version 11 oder höher  
- **Gradle**: Für den Build-Prozess (optional, falls nicht im Projekt enthalten)

### Schritte

1. Repository klonen:
    ```bash
    git clone https://github.com/gersti06/JavaFX.git
    cd JavaFX
    ```

2. Abhängigkeiten herunterladen:
    ```bash
    gradle build
    ```

3. Spiel starten:
    ```bash
    gradle run
    ```

---

## 🖼️ Screenshots

- Hauptmenü  
- Spielansicht  
- Highscore-Ansicht

---

## 🌈 Anpassungsmöglichkeiten

### Themes
Passe das Aussehen des Spiels individuell an, z. B.:

- Klassisches Kartendesign  
- Tiere, Früchte oder Tech-Symbole  
- Dunkel- und Hell-Modus

### Schwierigkeitsstufen

- **Einfach**: 4×4 Kartenraster  
- **Mittel**: 6×6 Kartenraster  
- **Schwer**: 8×8 Kartenraster

### Multiplayer-Modus
Spiele mit bis zu **4 Personen** auf demselben Gerät.

---

## 📖 Beispiele und Code-Schnipsel

### Kartenklasse (`Card.java`)
```java
public class Card {
    private final String id;
    private boolean isMatched;
    private boolean isFlipped;

    public Card(String id) {
        this.id = id;
        this.isMatched = false;
        this.isFlipped = false;
    }

    public void flip() {
        this.isFlipped = !this.isFlipped;
    }

    public boolean matches(Card other) {
        return this.id.equals(other.id);
    }

    // Getter und Setter
}
````

### Spielstart (`GameController.java`)

```java
public void startGame() {
    shuffleCards();
    timer.start();
    updateUI();
    System.out.println("Das Spiel hat begonnen!");
}
```

---

## 📚 Dokumentation und Ressourcen

* [JavaFX Dokumentation](https://openjfx.io/)
* [Memory Game Tutorial (externe Quelle)](https://www.baeldung.com/java-memory-game)

---

## 🛡️ Lizenz

Dieses Projekt steht unter der **MIT-Lizenz**. Weitere Informationen findest du in der Datei `LICENSE`.

---

## 🧑‍💻 Mitwirken

Wir freuen uns über Beiträge! So kannst du mitmachen:

1. Forke das Repository
2. Erstelle einen neuen Branch:

   ```bash
   git checkout -b feature/neues-feature
   ```
3. Füge deine Änderungen hinzu und erstelle einen Pull Request.

---

## 🎉 Viel Spaß beim Erinnern!

Wir hoffen, **MemoryFX** bereitet dir genauso viel Freude beim Spielen wie uns bei der Entwicklung.
Bei Fragen oder Ideen kannst du uns jederzeit kontaktieren.

```

---


