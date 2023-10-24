package org.arqaine.maven.service;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.arqaine.maven.model.Table;
import org.arqaine.maven.util.InputHandler;

import java.io.*;
import java.util.*;

public class TableServiceImpl implements TableService{
    final int asciiCharLength = 3;
    Scanner scanner = new Scanner(System.in);
    InputHandler inputHandler = new InputHandler();
    Table table = new Table();


    // Override methods from the interface
    @Override
    public void reset(int newRows, int newCols) {
        // Generate a new random table with key-value pairs
        table.getTableData().clear(); // Clear the existing data
        for (int i = 0; i < newRows; i++) {
            LinkedHashMap<String, String> newRow = generateRandomKeyValuePairs(newCols);
            table.getTableData().add(newRow);
        }
    }
    @Override
    public void search(String searchChoice) {

        String target = scanner.next();

        boolean found = false;

        for (LinkedHashMap<String, String> row : table.getTableData()) {
            for (Map.Entry<String, String> entry : row.entrySet()) {
                if ((searchChoice.equalsIgnoreCase("K") && entry.getKey().contains(target)) ||
                        (searchChoice.equalsIgnoreCase("V") && entry.getValue().contains(target))) {

                    inputHandler.printCustomMessage("Found '" + target + "' in row " + table.getTableData().indexOf(row) +
                            " with key '" + entry.getKey() + "' and value '" + entry.getValue() + "'.");

                    found = true;
                }
            }
        }

        if (!found) {
            inputHandler.printCustomMessage("No instances of '" + target + "' found.");
        }
    }

    @Override
    public void editCell() {
        try {
            // Prompt user for row index
            int rowIndex = inputHandler.getUserInput(scanner, "Enter the row index you want to edit: ");
            LinkedHashMap<String, String> row = table.getTableData().get(rowIndex);

            // Prompt user for the key
            String keyToEdit = inputHandler.getUserInputString(scanner, "Enter the key you want to edit: ");

            // Check if the key exists in the row
            if (!row.containsKey(keyToEdit)) {
                throw new IllegalArgumentException("Key '" + keyToEdit + "' not found in row " + rowIndex + ".");
            }

            // Prompt user for action (edit key or value)
            String actionChoice = inputHandler.getChoice(scanner, "Do you want to edit the key (K) or the value (V)? ");

            if (actionChoice.equalsIgnoreCase("K")) {
                // Prompt user for the new key
                String newKey = inputHandler.getUserInputString(scanner, "Enter the new key: ");

                // Check if the new key already exists in other rows
                boolean keyExistsInOtherRows = checkKeyExistsInOtherRows(row, newKey);

                if (!row.containsKey(newKey)) {
                    if (!keyExistsInOtherRows) {
                        // Store the value associated with the key
                        String valueToRetain = row.get(keyToEdit);

                        // Update the key
                        List<String> keyOrder = new ArrayList<>(row.keySet());
                        int keyIndex = keyOrder.indexOf(keyToEdit);
                        if (keyIndex != -1) {
                            row.remove(keyToEdit);
                            keyOrder.set(keyIndex, newKey);
                            LinkedHashMap<String, String> updatedRow = new LinkedHashMap<>();
                            for (String key : keyOrder) {
                                updatedRow.put(key, row.get(key));
                            }
                            // Reassign the value to the new key
                            updatedRow.put(newKey, valueToRetain);
                            table.getTableData().set(rowIndex, updatedRow);
                            inputHandler.printCustomMessage("Key updated successfully.");
                        }
                    } else {
                        throw new IllegalArgumentException("Key '" + newKey + "' already exists in other rows.");
                    }
                } else {
                    throw new IllegalArgumentException("Key already exists.");
                }
            } else if (actionChoice.equalsIgnoreCase("V")) {
                // Prompt user for the new value
                String newValue = inputHandler.getUserInputString(scanner, "Enter the new value: ");

                // Update the value
                row.put(keyToEdit, newValue);
                inputHandler.printCustomMessage("Value updated successfully.");
            }
        } catch (IllegalArgumentException e) {
            inputHandler.printCustomMessage(e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            inputHandler.printCustomMessage("Invalid row index. No changes made.");
        }
    }

    @Override
    public void printTable() {
        // Print the loaded table data
        for (LinkedHashMap<String, String> row : table.getTableData()) {
            for (Map.Entry<String, String> entry : row.entrySet()) {
                System.out.print(entry.getKey() + ":" + entry.getValue() + " | ");
            }
            System.out.println(); //for new line
        }

    }

    @Override
    public void addNewRow() {

        // Prompt user for row index
        int rowIndex = inputHandler.getUserInput(scanner, "Enter the row index where you want to insert: ");

        // Insert the new row at the specified index
        if (rowIndex >= 0 && rowIndex <= table.getTableData().size()) {
            // Prompt user for the number of columns
            int numColumns = inputHandler.getUserInput(scanner, "Enter the number of columns to insert: ");

            // Generate random key-value pairs for the new row
            LinkedHashMap<String, String> newRow = generateRandomKeyValuePairs(numColumns);

            table.getTableData().add(rowIndex, newRow);
            inputHandler.printCustomMessage("New row inserted successfully!");
        } else {
            inputHandler.printCustomMessage("Invalid row index. Row not inserted.");
        }
    }

    @Override
    public void sortRow() {
        try {
            // Prompt user for row index
            int rowIndex = inputHandler.getUserInput(scanner, "Enter the row index you want to sort: ");

            // Get the LinkedHashMap for the specified row
            LinkedHashMap<String, String> row = table.getTableData().get(rowIndex);

            // Convert the entry set to a list of key-value pairs
            List<Map.Entry<String, String>> entryList = new ArrayList<>(row.entrySet());

            // Sort the list based on concatenated key-value pairs
            entryList.sort(Comparator.comparing(e -> e.getKey() + e.getValue()));

            // Rebuild sorted LinkedHashMap
            LinkedHashMap<String, String> sortedRow = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : entryList) {
                sortedRow.put(entry.getKey(), entry.getValue());
            }

            // Replace the row with the sorted LinkedHashMap
            table.getTableData().set(rowIndex, sortedRow);

            inputHandler.printCustomMessage("Row " + rowIndex + " sorted successfully.");
        } catch (IndexOutOfBoundsException e) {
            inputHandler.printCustomMessage("Invalid row index. No changes made.");
        }
    }

    @Override
    public ArrayList<LinkedHashMap<String, String>> loadTableFromJar(String filePath, String outputFile) {


        try (InputStream inputStream = getClass().getResourceAsStream(filePath)) {
            if (inputStream != null) {
                OutputStream outputStream = new FileOutputStream(outputFile);
                IOUtils.copy(inputStream, outputStream);
                inputStream.close();
                outputStream.close();

                // BufferedReader should be created after the InputStream is copied
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(outputFile)));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] keyValuePairs = line.split("\\|");

                    LinkedHashMap<String, String> row = new LinkedHashMap<>();

                    for (String pair : keyValuePairs) {
                        String[] parts = pair.split(":");
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String value = parts[1].trim();
                            row.put(key, value);
                        }
                    }
                    table.getTableData().add(row);
                }
            } else {
                inputHandler.printCustomMessage("\nResource not found.");
            }
        } catch (IOException e) {
            inputHandler.printCustomMessage("An error occurred: " + e.getMessage());
        }
        return table.getTableData();
    }

    @Override
    public ArrayList<LinkedHashMap<String, String>> loadTableFromFile(String filePath) {

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] keyValuePairs = line.split("\\|");

                LinkedHashMap<String, String> row = new LinkedHashMap<>();

                for (String pair : keyValuePairs) {
                    String[] parts = pair.split(":");
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        row.put(key, value);
                    }
                }
                table.getTableData().add(row);
            }
        } catch (IOException e) {
            inputHandler.printCustomMessage("An error occurred while reading the file: " + e.getMessage());
        }
        return table.getTableData();
    }

    @Override
    public void saveTableToFile(ArrayList<LinkedHashMap<String, String>> tableData, String filePath)   {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (LinkedHashMap<String, String> row : tableData) {
                String rowString = StringUtils.join(
                        row.entrySet().stream()
                                .map(entry -> entry.getKey() + ":" + entry.getValue())
                                .toArray(),
                        " | "
                );
                writer.write(rowString + "\n");
            }
        } catch (IOException e) {
            inputHandler.printCustomMessage("An error occurred while saving the table: " + e.getMessage());
        }

    }

    @Override
    public ArrayList<LinkedHashMap<String, String>> initializeTable(String[] args, Table table) {
        String jarFilePath = "/default_table.txt";
        String filePath = (args.length > 0) ? args[0] : "MyTable.txt";

        //checks if there is a file outside the jar file
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            // load txt file from outside the jar
            table.setTableData(loadTableFromFile(filePath));
            table.setFilePath(filePath);
        } else {
            // load txt file from jar
            table.setTableData(loadTableFromJar(jarFilePath, filePath));
            table.setFilePath(jarFilePath);
        }


        return table.getTableData();
    }


    private boolean checkKeyExistsInOtherRows(LinkedHashMap<String, String> currentRow, String key) {
        for (LinkedHashMap<String, String> otherRow : table.getTableData()) {
            if (otherRow != currentRow && otherRow.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    public LinkedHashMap<String, String> generateRandomKeyValuePairs(int numColumns) {
        LinkedHashMap<String, String> randomPairs = new LinkedHashMap<>();

        for (int i = 0; i < numColumns; i++) {
            String key = generateRandomAsciiString(asciiCharLength);
            String value = generateRandomAsciiString(asciiCharLength);
            randomPairs.put(key, value);
        }

        return randomPairs;
    }

    public String generateRandomAsciiString(int length) {
        StringBuilder randomString = new StringBuilder();
        while (randomString.length() < length) {
            String randomChar = RandomStringUtils.randomAscii(1);
            if (!" :|".contains(randomChar)) {
                randomString.append(randomChar);
            }
        }
        return randomString.toString();
    }




}
