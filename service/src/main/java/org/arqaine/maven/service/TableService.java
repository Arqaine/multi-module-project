package org.arqaine.maven.service;

import org.arqaine.maven.model.Table;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public interface TableService {

    void search(String searchChoice);
    void editCell();
    void printTable();
    void reset(int newRows, int newCols);
    void addNewRow();
    void sortRow();
    ArrayList<LinkedHashMap<String, String>> loadTableFromJar(String filePath, String outputFile);

    ArrayList<LinkedHashMap<String, String>> loadTableFromFile(String filePath);

    void saveTableToFile(ArrayList<LinkedHashMap<String, String>> tableData, String filePath);

    ArrayList<LinkedHashMap<String, String>> initializeTable(String[] args, Table table);
}
