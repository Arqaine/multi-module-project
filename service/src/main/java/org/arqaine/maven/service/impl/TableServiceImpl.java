package org.arqaine.maven.service.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.arqaine.maven.model.Table;
import org.arqaine.maven.service.TableService;
import org.arqaine.maven.util.InputHandler;


import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Comparator;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;

public class TableServiceImpl implements TableService {
    private static final int CHAR_LENGTH = 3;
    private final Table table;
    // Specify the output path using an environment variable
    private String outputDirectory = System.getenv("OUTPUT_DIRECTORY");
    private Path outputPath = Paths.get(outputDirectory);
    private Path outputFilePath;

    private final Scanner scanner;
    private final InputHandler inputHandler;

    public TableServiceImpl(Table table, Scanner scanner, InputHandler inputHandler) {
        this.table = table;
        this.scanner = scanner;
        this.inputHandler = inputHandler;
    }



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
    public String search(String searchChoice, String target) {
        List<String> results = new ArrayList<>();

        for (LinkedHashMap<String, String> row : this.table.getTableData()) {
            for (Map.Entry<String, String> entry : row.entrySet()) {
                boolean keyMatch = searchChoice.equalsIgnoreCase("K") && entry.getKey().contains(target);
                boolean valueMatch = searchChoice.equalsIgnoreCase("V") && entry.getValue().contains(target);

                if (keyMatch || valueMatch) {
                    String found = "Found '" + target + "' in row " + table.getTableData().indexOf(row) +
                            " with key '" + entry.getKey() + "' and value '" + entry.getValue() + "'.";
                    results.add(found);
                }
            }
        }

        if (results.isEmpty()) {
            return "No instances of '" + target + "' found.";
        }

        return String.join("\n", results);
    }


    @Override
    public void editCell(String keyToEdit) {
        try {
            boolean keyFound = findAndEditCell(keyToEdit);

            if (!keyFound) {
                throw new IllegalArgumentException("Key '" + keyToEdit + "' not found in any row.");
            }

        } catch (IllegalArgumentException e) {
            inputHandler.printCustomMessage(e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            inputHandler.printCustomMessage("Invalid row index. No changes made.");
        }
    }

    private boolean findAndEditCell(String keyToEdit) {
        boolean keyFound = false;

        for (int i = 0; i < table.getTableData().size(); i++) {
            LinkedHashMap<String, String> row = table.getTableData().get(i);
            if (row.containsKey(keyToEdit)) {
                keyFound = true;
                editCellAction(row, keyToEdit, i);
            }
        }

        return keyFound;
    }

    private void editCellAction(LinkedHashMap<String, String> row, String keyToEdit, int rowIndex)  {
        // Prompt user for action (edit key or value)
        String actionChoice = inputHandler.getChoice(scanner, "Do you want to edit the key (K) or the value (V)? ");

        if (actionChoice.equalsIgnoreCase("K")) {
            editKey(row, keyToEdit, rowIndex);
        } else if (actionChoice.equalsIgnoreCase("V")) {
            editValue(row, keyToEdit);
        }
    }

    private void editKey(LinkedHashMap<String, String> row, String keyToEdit, int rowIndex) {
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
    }


    private boolean checkKeyExistsInOtherRows(LinkedHashMap<String, String> currentRow, String key) {
        for (LinkedHashMap<String, String> otherRow : table.getTableData()) {
            if (otherRow != currentRow && otherRow.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    private void editValue(LinkedHashMap<String, String> row, String keyToEdit) {
        // Prompt user for the new value
        String newValue = inputHandler.getUserInputString(scanner, "Enter the new value: ");

        // Update the value
        row.put(keyToEdit, newValue);
        inputHandler.printCustomMessage("Value updated successfully.");
    }

    @Override
    public String printTable() {
        // Print the loaded table data
        StringBuilder printedTable = new StringBuilder();
        for (LinkedHashMap<String, String> row : table.getTableData()) {
            for (Map.Entry<String, String> entry : row.entrySet()) {
                printedTable.append(entry.getKey()).append(":").append(entry.getValue()).append(" | ");
            }
            printedTable.append("\n"); // for new line after each row
        }

        return printedTable.toString();
    }


    @Override
    public String addNewRow(int rowIndex, int numColumns) {
        String resultMessage;

        try {
            // Insert the new row at the specified index
            if (rowIndex >= 0 && rowIndex <= table.getTableData().size()) {

                // Generate random key-value pairs for the new row
                LinkedHashMap<String, String> newRow = generateRandomKeyValuePairs(numColumns);

                table.getTableData().add(rowIndex, newRow);
                resultMessage = "New row inserted successfully!";
            } else {
                throw new IndexOutOfBoundsException("Invalid row index. Row not inserted.");
            }
        } catch (IndexOutOfBoundsException e) {
            resultMessage = e.getMessage();
        }

        return resultMessage;
    }


    @Override
    public String sortRow(int rowIndex) {
        String sortRowMessage;
        try {

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

            sortRowMessage = "Row " + rowIndex + " sorted successfully.";
        } catch (IndexOutOfBoundsException e) {
            sortRowMessage = "Invalid row index. No changes made.";
        }
        return sortRowMessage;
    }

    @Override
    public List<LinkedHashMap<String, String>> loadTableFromJar(String filePath, String outputFile) {
        try (InputStream inputStream = getClass().getResourceAsStream(filePath)) {
            if (inputStream != null) {
                if (outputDirectory == null || outputDirectory.isEmpty()) {
                    throw new IllegalStateException("OUTPUT_DIRECTORY environment variable not set.");
                }

                // Create the output directory if it doesn't exist
                Files.createDirectories(outputPath);

                // Specify the output file path
                outputFilePath = outputPath.resolve(outputFile);

                // Copy the content of the input stream to the output file
                Files.copy(inputStream, outputFilePath, StandardCopyOption.REPLACE_EXISTING);

                // BufferedReader should be created after the InputStream is copied
                loadTableFromReader(new BufferedReader(new InputStreamReader(new FileInputStream(outputFilePath.toFile()))));

            } else {
                inputHandler.printCustomMessage("\nResource not found.");
            }
        } catch (IOException e) {
            inputHandler.printCustomMessage("An error occurred: " + e.getMessage());
        }
        return table.getTableData();
    }

    @Override
    public List<LinkedHashMap<String, String>> loadTableFromFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            loadTableFromReader(reader);
        } catch (IOException e) {
            inputHandler.printCustomMessage("An error occurred while reading the file: " + e.getMessage());
        }
        return table.getTableData();
    }

    private void loadTableFromReader(BufferedReader reader) throws IOException {
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
    }

    @Override
    public void saveTableToFile(List<LinkedHashMap<String, String>> tableData, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
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
    public List<LinkedHashMap<String, String>> initializeTable(String[] args, Table table) {
        String relativeFileName = "MyTable.txt";
        String jarFileName = "/" + relativeFileName;

        // Use the environment variable as the default target folder
        String defaultTargetFolder = outputDirectory != null && !outputDirectory.isEmpty()
                ? outputDirectory
                : "target"; // Set a default value if OUTPUT_DIRECTORY is not set

        String fileName = (args.length > 0) ? args[0] : relativeFileName;

        // Specify the target folder path
        Path targetFolderPath = Paths.get(defaultTargetFolder);

        // Check if the file exists inside the target folder
        File fileInTargetFolder = targetFolderPath.resolve(fileName).toFile();

        if (fileInTargetFolder.exists() && fileInTargetFolder.isFile()) {
            // load txt file from the target folder
            loadTableFromFile(fileInTargetFolder.getAbsolutePath());
            table.setFilePath(fileInTargetFolder.getAbsolutePath()); // Set the correct file path
        } else {
            // If the file is not in the target folder or outside the jar, load from jar
            loadTableFromJar(jarFileName, fileName);
            table.setFilePath(fileInTargetFolder.exists() ? fileInTargetFolder.getAbsolutePath() : jarFileName);
        }

        return table.getTableData();
    }


    @Override
    public LinkedHashMap<String, String> generateRandomKeyValuePairs(int numColumns) {
        LinkedHashMap<String, String> randomPairs = new LinkedHashMap<>();

        for (int i = 0; i < numColumns; i++) {
            String key = generateRandomAsciiString(CHAR_LENGTH);
            String value = generateRandomAsciiString(CHAR_LENGTH);
            randomPairs.put(key, value);
        }

        return randomPairs;
    }

    private String generateRandomAsciiString(int length) {
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
