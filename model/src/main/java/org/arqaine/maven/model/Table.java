package org.arqaine.maven.model;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.ArrayList;

public class Table {
    private List<LinkedHashMap<String, String>> tableData;


    private String filePath;

    public Table() {
        tableData = new ArrayList<>();
    }

    public List<LinkedHashMap<String, String>> getTableData() {
        return tableData;
    }

    public void setTableData(List<LinkedHashMap<String, String>> tableData) {
        this.tableData = tableData;
    }

    public String getFilePath(){
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


}