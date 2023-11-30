package org.arqaine.maven.service;

import org.arqaine.maven.model.Table;

import java.util.LinkedHashMap;
import java.util.List;

public interface TableService {

    String search(String searchChoice, String target);
    void editCell(String keyToEdit);
    String printTable();
    void reset(int newRows, int newCols);
    String addNewRow(int rowIndex, int numColumns);
    String sortRow(int rowIndex);
    List<LinkedHashMap<String, String>> loadTableFromJar(String filePath, String outputFile);

    List<LinkedHashMap<String, String>> loadTableFromFile(String filePath);

    void saveTableToFile(List<LinkedHashMap<String, String>> tableData, String filePath);

    List<LinkedHashMap<String, String>> initializeTable(String[] args, Table table);
    LinkedHashMap<String, String> generateRandomKeyValuePairs(int numColumns);


}
