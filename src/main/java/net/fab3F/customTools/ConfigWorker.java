package net.fab3F.customTools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

public class ConfigWorker {

    /**
     * Ändert ein Feld im JSON-File, wobei 'fieldPath' verschachtelte Felder mit '.' trennt.
     * Beispiel: "window.width"
     *
     * @param filePath  Config-Datei
     * @param field Pfad zum Feld (z.B. "appName" oder "window.width")
     * @param newValue  Neuer Wert als String (wird als Text eingefügt)
     * @throws IOException bei Lese-/Schreibfehlern
     */

    public static void updateField(File filePath, String field, String newValue) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(filePath);

        if (!(rootNode instanceof ObjectNode)) {
            throw new IllegalArgumentException("Root-Element muss ein JSON-Objekt sein.");
        }

        ObjectNode currentNode = (ObjectNode) rootNode;

        String[] pathParts = field.split("\\.");
        for (int i = 0; i < pathParts.length - 1; i++) {
            String part = pathParts[i];
            JsonNode nextNode = currentNode.get(part);
            if (nextNode == null || !nextNode.isObject()) {
                // Falls der Pfad noch nicht existiert oder kein Objekt ist, lege ein neues Objekt an
                ObjectNode newNode = mapper.createObjectNode();
                currentNode.set(part, newNode);
                currentNode = newNode;
            } else {
                currentNode = (ObjectNode) nextNode;
            }
        }

        // Letztes Feld im Pfad
        String lastPart = pathParts[pathParts.length - 1];

        // Hier kannst du noch erweitern, ob newValue als int, boolean, etc. interpretiert werden soll.
        // Für jetzt: Immer als Text speichern
        currentNode.put(lastPart, newValue);

        // Schreibe zurück in die Datei (mit schöner Formatierung)
        mapper.writerWithDefaultPrettyPrinter().writeValue(filePath, rootNode);
    }

    public static void main(String[] args) throws IOException {
        File configFile = new File("config.json");

        // Beispiele:
        updateField(configFile, "appName", "NeueApp");
        updateField(configFile, "window.width", "1440");
        updateField(configFile, "debug", "false");
    }
}
