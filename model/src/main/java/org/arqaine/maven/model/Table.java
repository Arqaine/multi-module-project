package org.arqaine.maven.model;

import java.util.*;

public class Table {
    private ArrayList<LinkedHashMap<String, String>> tableData;

    private String filePath;

    public Table() {
        tableData = new ArrayList<>();
    }

    public ArrayList<LinkedHashMap<String, String>> getTableData() {
        return tableData;
    }

    public void setTableData(ArrayList<LinkedHashMap<String, String>> tableData) {
        this.tableData = tableData;
    }

    public String getFilePath(){
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


}