package org.arqaine.maven.service.impl;

import org.apache.commons.io.IOUtils;
import org.arqaine.maven.model.Table;
import org.arqaine.maven.service.TableService;
import org.arqaine.maven.util.InputHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TableServiceImplTest {
    private TableService tableService;
    private InputHandler inputHandler;

    private Table table;
    Scanner scanner = new Scanner(System.in);
    private List<LinkedHashMap<String, String>> mockTableData;

    @Before
    public void setUp() {
    table = mock(Table.class);
    inputHandler = mock(InputHandler.class);
    tableService = new TableServiceImpl(table);
    System.out.println("Before");

    // Create mock table data
    mockTableData = createMockTableData();
    mockTableData(mockTableData);
}

    private List<LinkedHashMap<String, String>> createMockTableData() {
    LinkedHashMap<String, String> row1 = new LinkedHashMap<>();
    row1.put("key1", "value1");
    row1.put("key2", "value2");
    LinkedHashMap<String, String> row2 = new LinkedHashMap<>();
    row2.put("key3", "value3");
    row2.put("key4", "value4");
    return new ArrayList<>(Arrays.asList(row1, row2));

}

//behavior of mock
private void mockTableData(List<LinkedHashMap<String, String>> mockData) {
    when(table.getTableData()).thenReturn(mockData);
}

    @Test
    public void testSearchSuccess() {

        String searchChoice = "K";
        String target = "key1";

        String searchResult = tableService.search(searchChoice, target);

        assertEquals("Found 'key1' in row 0 with key 'key1' and value 'value1'.", searchResult);
        System.out.println("Test Search");
    }

//    @Test
//    public void testEditCellSuccess() {
//
//        // Call the method under test
//        tableService.editCell("key1");
//
//
//
//        // Mocking the user input
//        when(inputHandler.getChoice(any(), any())).thenReturn("K");
//        when(inputHandler.getUserInputString(any(), any())).thenReturn("newKey");
//
//        // Verify that the key was updated successfully
//        LinkedHashMap<String, String> updatedRow = mockTableData.get(0);
//        assertTrue(updatedRow.containsKey("newKey"));
//        assertEquals("value1", updatedRow.get("newKey"));
//    }
//
//
//    @Test
//    public void testEditCellValueSuccess() {
//        String keyToEdit = "key1";
//        String actionChoice = "K";
//
//        // Mock user input for "Enter the new key:" prompt
//        when(inputHandler.getUserInputString(eq(scanner), eq("Enter the new key: ")))
//                .thenReturn("newKey");
//
//        // Call the method under test
//        tableService.editCell(keyToEdit);
//
//        // Verify that the key was updated successfully
//        LinkedHashMap<String, String> updatedRow = mockTableData.get(0);
//        assertTrue(updatedRow.containsKey("newKey"));
//        assertEquals("value1", updatedRow.get("newKey"));
//
//        System.out.println("Test Edit Key Success");
//    }
//
//    @Test
//    public void testEditCellKeyAlreadyExists() {
//        // Assume that key 'key1' exists in the first row
//        String keyToEdit = "key1";
//        String actionChoice = "K";
//
//        // Call the method under test with a new key that already exists
//        assertThrows(IllegalArgumentException.class,
//                () -> tableService.editCell(keyToEdit));
//
//        System.out.println("Test Edit Key Already Exist");
//    }
//
//    @Test
//    public void testEditCellKeyExistsInOtherRows() {
//        // Assume that key 'key1' exists in the first row
//        String keyToEdit = "key1";
//        String actionChoice = "K";
//
//        // Modify the mock data to make 'newKey' already exist in another row
//        LinkedHashMap<String, String> row2 = mockTableData.get(1);
//        row2.put("newKey", "value2");
//
//        // Call the method under test with a new key that exists in other rows
//        assertThrows(IllegalArgumentException.class,
//                () -> tableService.editCell(keyToEdit));
//        System.out.println("Test Edit Key Exist in other rows");
//    }


    @Test
    public void testPrintTable() {

        // Call the method under test
        String printedTable = tableService.printTable();

        // Define the expected result
        String expectedPrintedTable = "key1:value1 | key2:value2 | \nkey3:value3 | key4:value4 | \n";

        // Assert the result
        assertEquals(expectedPrintedTable, printedTable);
        System.out.println("Test Print");
    }

    @Test
    public void testReset() {
        int newRows = 2;
        int newCols = 2;

        tableService.reset(newRows, newCols);


        // Ensure the correct number of rows is generated
        assertEquals(newRows, mockTableData.size());

        for (LinkedHashMap<String, String> row : mockTableData) {
            // Ensure the correct number of columns is generated for each row
            assertEquals(newCols, row.size());

            // Ensure that each key and value is not null or empty
            for (Map.Entry<String, String> entry : row.entrySet()) {
                assertNotNull(entry.getKey());
                assertNotNull(entry.getValue());
                assertFalse(entry.getKey().isEmpty());
                assertFalse(entry.getValue().isEmpty());
            }
        }
        System.out.println("Test Reset");
    }

    @Test
    public void testAddNewRowSuccess() {
        int rowIndex = 1;
        int numColumns = 3;


        // Call the method under test
        String resultMessage = tableService.addNewRow(rowIndex, numColumns);

        // Verify the changes in the table
        assertEquals("New row inserted successfully!", resultMessage);
        assertEquals(3, mockTableData.size()); // Check that one row was added

        LinkedHashMap<String, String> addedRow = mockTableData.get(1); // Adjusted to index 1
        assertEquals(numColumns, addedRow.size()); // Check the number of columns in the added row
        System.out.println("Test Add New Row Success");
    }

    @Test
    public void testAddNewRowInvalidIndex() {
        int rowIndex = 5;
        int numColumns = 3;

        // Call the method under test
        String resultMessage = tableService.addNewRow(rowIndex, numColumns);

        // Verify that the exception message is returned
        assertEquals("Invalid row index. Row not inserted.", resultMessage);
        assertEquals(2, mockTableData.size()); // Check that no row was added
        System.out.println("Test Add New Row Invalid Index");
    }

    @Test
    public void testSortRow() {

        // Call the method under test
        String sortRowMessage = tableService.sortRow(0);

        // Verify that the row is sorted correctly
        List<String> sortedValues = new ArrayList<>(Arrays.asList("value1", "value2"));

        LinkedHashMap<String, String> expectedSortedRow = new LinkedHashMap<>();
        expectedSortedRow.put("key1", sortedValues.get(0));
        expectedSortedRow.put("key2", sortedValues.get(1));

        // Verify that the row in the table is now sorted
        verify(table, times(2)).getTableData(); // Adjusted to times(2)
        assertEquals(expectedSortedRow, mockTableData.get(0));

        // Verify the success message
        assertEquals("Row 0 sorted successfully.", sortRowMessage);
        System.out.println("Test Sort Row");
    }

    @Test
    public void testSortRowInvalidIndex() {

        // Call the method under test with an invalid row index
        String sortRowMessage = tableService.sortRow(5);

        // Verify that no changes were made to the table
        verify(table, times(1)).getTableData(); // Adjusted to times(1)
        assertEquals("Invalid row index. No changes made.", sortRowMessage);
        System.out.println("Test Sort Row Invalid Index");
    }


    //load table from file
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testLoadTableFromFileWithActualFile() throws IOException {
        // Create a temporary file and write some content to it
        File tempFile = tempFolder.newFile("testFile.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("key1: value1 | key2: value2");
        }

        // Call the method under test with the actual file path
        List<LinkedHashMap<String, String>> result = tableService.loadTableFromFile(tempFile.getAbsolutePath());

        // Assert the result
        assertEquals(3, result.size());

        LinkedHashMap<String, String> expectedRow = new LinkedHashMap<>();
        expectedRow.put("key1", "value1");
        expectedRow.put("key2", "value2");

        assertEquals(expectedRow, result.get(0));
    }

    @Test
    public void testLoadTableFromJar() throws IOException {
        // Create a temporary JAR file and write some content to it
        File tempJarFile = tempFolder.newFile("testJar.jar");
        try (Writer writer = new FileWriter(tempJarFile)) {
            writer.write("key1: value1 | key2: value2");
        }

        // Specify the path to the resource file within the JAR
        String jarFilePath = "/" + tempJarFile.getName();

        // Call the method under test
        List<LinkedHashMap<String, String>> result = tableService.loadTableFromJar(jarFilePath, "output.txt");

        // Verify that the result is not empty
        assertTrue(result.size() > 0);

        // Optionally, you can assert specific values based on the content of the resource file
        LinkedHashMap<String, String> expectedRow = new LinkedHashMap<>();
        expectedRow.put("key1", "value1");
        expectedRow.put("key2", "value2");

        assertEquals(expectedRow, result.get(0));
    }

    @Test
    public void testSaveTableToFile() throws IOException {


        // Create a temporary file for saving the data
        File outputFile = tempFolder.newFile("output.txt");


        tableService.saveTableToFile(mockTableData, outputFile.getAbsolutePath());

        // Read the saved content from the file
        List<String> lines = Files.readAllLines(outputFile.toPath());

        // Assert that the content matches the expected data
        assertEquals(2, lines.size()); // Ensure there's two line in the file

        String expectedRow = "key1:value1 | key2:value2";
        assertEquals(expectedRow, lines.get(0)); // Check if the content matches
    }

    //Initialize table
    @Test
    public void testInitializeTableWithExternalFile() throws IOException {
        // Create a temporary external file and write some content to it
        File externalFile = tempFolder.newFile("externalFile.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(externalFile))) {
            writer.write("key1: value1 | key2: value2");
        }


        String[] args = {externalFile.getAbsolutePath()};
        List<LinkedHashMap<String, String>> result = tableService.initializeTable(args, table);

        // Assert the result
        assertEquals(3, result.size()); // Ensure that data is loaded
        LinkedHashMap<String, String> expectedRow = new LinkedHashMap<>();
        expectedRow.put("key1", "value1");
        expectedRow.put("key2", "value2");
        assertEquals(expectedRow, result.get(0)); // Check if the loaded data matches

        // Ensure that the file path is set correctly
        assertEquals(externalFile.getAbsolutePath(), table.getFilePath());
    }



//
//    @Test
//    public void testHandleKeyAction() {
//        tableService.reset(1, 2);
//        LinkedHashMap<String, String> row = tableService.getTableData().get(0);
//        tableService.handleKeyAction(row, "key1", 0);
//        String result = tableService.printTable();
//        assertEquals("newKey:key2 | newValue:newValue | \n", result);
//    }
//
//    @Test
//    public void testHandleValueAction() {
//        tableService.reset(1, 1);
//        LinkedHashMap<String, String> row = tableService.getTableData().get(0);
//        tableService.handleValueAction(row, "key");
//        String result = tableService.printTable();
//        assertEquals("key:newValue | \n", result);
//    }
//

//

//
//    @Test
//    public void testAddNewRow() {
//        tableService.reset(1, 1);
//        tableService.addNewRow(0, 2);
//        String result = tableService.printTable();
//        assertEquals("key:newValue | \nnewKey:newValue | \n", result);
//    }
//

//
//    @Test
//    public void testLoadTableFromJar() {
//        List<LinkedHashMap<String, String>> tableData = tableService.loadTableFromJar("/default_table.txt", "MyTable.txt");
//        assertEquals(2, tableData.size());
//        assertEquals(2, tableData.get(0).size());
//        assertEquals(2, tableData.get(1).size());
//    }
//
//    @Test
//    public void testLoadTableFromFile() {
//        List<LinkedHashMap<String, String>> tableData = tableService.loadTableFromFile("MyTable.txt");
//        assertEquals(2, tableData.size());
//        assertEquals(2, tableData.get(0).size());
//        assertEquals(2, tableData.get(1).size());
//    }
//
//    @Test
//    public void testSaveTableToFile() {
//        tableService.reset(1, 1);
//        tableService.saveTableToFile(tableService.getTableData(), "MyTable.txt");
//        List<LinkedHashMap<String, String>> tableData = tableService.loadTableFromFile("MyTable.txt");
//        assertEquals(1, tableData.size());
//        assertEquals(1, tableData.get(0).size());
//    }
//
//    @Test
//    public void testInitializeTable() {
//        String[] args = {"MyTable.txt"};
//        List<LinkedHashMap<String, String>> tableData = tableService.initializeTable(args, new Table());
//        assertEquals(2, tableData.size());
//        assertEquals(2, tableData.get(0).size());
//        assertEquals(2, tableData.get(1).size());
//    }
//
//    @Test
//    public void testGenerateRandomKeyValuePairs() {
//        LinkedHashMap<String, String> randomPairs = tableService.generateRandomKeyValuePairs(2);
//        assertEquals(2, randomPairs.size());
//    }
}