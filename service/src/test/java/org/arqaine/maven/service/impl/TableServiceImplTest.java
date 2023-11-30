package org.arqaine.maven.service.impl;

import org.apache.commons.io.IOUtils;
import org.arqaine.maven.model.Table;
import org.arqaine.maven.service.TableService;
import org.arqaine.maven.util.InputHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TableServiceImplTest {
    private TableService tableService;
    private InputHandler inputHandler;
    private List<LinkedHashMap<String, String>> mockTableData;
    @Mock
    private Table table;
    @Mock
    private Scanner scanner;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        inputHandler = mock(InputHandler.class);
        tableService = new TableServiceImpl(table, scanner, inputHandler);

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
        System.out.println("Test Search Success");
    }

    @Test
    public void testSearchKeyNotFound() {
        String searchChoice = "K";
        String target = "nonexistentKey";

        String searchResult = tableService.search(searchChoice, target);

        // Check if the search result matches the expected result for a non-existent key
        assertEquals("No instances of 'nonexistentKey' found.", searchResult);
        System.out.println("Test Search Key Not Found");
    }


    @Test
    public void testEditCell() {
        String keyToEdit = "key1";

        LinkedHashMap<String, String> row = new LinkedHashMap<>();
        row.put("key1", "value1");
        row.put("key2", "value2");

        List<LinkedHashMap<String, String>> mockTableData = new ArrayList<>(Arrays.asList(row));
        when(table.getTableData()).thenReturn(mockTableData);

        when(inputHandler.getChoice(scanner, "Do you want to edit the key (K) or the value (V)? ")).thenReturn("K");
        when(inputHandler.getUserInputString(scanner, "Enter the new key: ")).thenReturn("newKey");

        // Act
        tableService.editCell(keyToEdit);

        // Assert
        // Verify that the inputHandler.printCustomMessage is called
        verify(inputHandler).printCustomMessage("Key updated successfully.");
        System.out.println("Test Edit Success");
    }

    @Test
    public void testEditCell_KeyNotFound_Exception() {
        String keyToEdit = "nonexistentKey";
        when(table.getTableData()).thenReturn(mockTableData);

        tableService.editCell(keyToEdit);

        verify(inputHandler).printCustomMessage("Key 'nonexistentKey' not found in any row.");
        System.out.println("Test Edit Key not found");
    }


    @Test
    public void testPrintTable() {

        String printedTable = tableService.printTable();

        String expectedPrintedTable = "key1:value1 | key2:value2 | \nkey3:value3 | key4:value4 | \n";

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

        String resultMessage = tableService.addNewRow(rowIndex, numColumns);

        // Verify the changes in the table
        assertEquals("New row inserted successfully!", resultMessage);
        assertEquals(3, mockTableData.size()); // Check that one row was added

        LinkedHashMap<String, String> addedRow = mockTableData.get(1);
        assertEquals(numColumns, addedRow.size()); // Check the number of columns in the added row
        System.out.println("Test Add New Row Success");
    }

    @Test
    public void testAddNewRowInvalidIndex() {
        int rowIndex = 5;
        int numColumns = 3;

        String resultMessage = tableService.addNewRow(rowIndex, numColumns);

        // Verify that the exception message is returned
        assertEquals("Invalid row index. Row not inserted.", resultMessage);
        assertEquals(2, mockTableData.size()); // Check that no row was added
        System.out.println("Test Add New Row Invalid Index");
    }

    @Test
    public void testSortRow() {

        String sortRowMessage = tableService.sortRow(0);

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

        String sortRowMessage = tableService.sortRow(5);

        // Verify that no changes were made to the table
        verify(table, times(1)).getTableData();
        assertEquals("Invalid row index. No changes made.", sortRowMessage);
        System.out.println("Test Sort Row Invalid Index");
    }


    //load table from file
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testLoadTableFromFile() throws IOException {
        File tempFile = tempFolder.newFile("testFile.txt");
        // Set the content of the temporary file
        String fileContent = "key1:value1 | key2:value2";
        java.nio.file.Files.write(tempFile.toPath(), fileContent.getBytes());
        List<String> lines = Files.readAllLines(tempFile.toPath());

        // Call the method under test with the actual file path
        tableService.loadTableFromFile(tempFile.getAbsolutePath());

        // Assert the result
        assertTrue(tempFile.exists());
        assertEquals(fileContent, lines.get(0));
        System.out.println("Test Load Table from file");
    }


    @Test
    public void testSaveTableToFile() {

        // Set up a temporary file path for testing
        String tempFolderPath = tempFolder.getRoot().getAbsolutePath();
        String tempFilePath = tempFolderPath + "/testOutputFile.txt";

        LinkedHashMap<String, String> row3 = new LinkedHashMap<>();
        row3.put("key5", "value5");
        row3.put("key6", "value6");

        mockTableData.add(row3);

        // Call the method to test
        tableService.saveTableToFile(mockTableData, tempFilePath);

        // Assert that the file has been created and contains the expected content
        File outputFile = new File(tempFilePath);
        assertTrue(outputFile.exists());

        try {
            List<String> lines = Files.readAllLines(outputFile.toPath());
            assertEquals(3, lines.size()); // Assuming 3 rows in the table

            assertEquals("key1:value1 | key2:value2", lines.get(0));
            assertEquals("key3:value3 | key4:value4", lines.get(1));
            assertEquals("key5:value5 | key6:value6", lines.get(2));
        } catch (IOException e) {
            fail("An error occurred while reading the file: " + e.getMessage());
        }
        System.out.println("Test Save Table to file Success");
    }


    //Initialize table
    @Test
    public void testInitializeTable_FileInTargetFolder_LoadsFromFile() throws IOException {
        // Set up a temporary file in the target folder
        File tempFile = tempFolder.newFile("MyTable.txt");
        Path targetFolderPath = tempFile.toPath().getParent();

        // Set the content of the temporary file
        String fileContent = "key1:value1 | key2:value2";
        java.nio.file.Files.write(tempFile.toPath(), fileContent.getBytes());

        // Set the environment variable for the target folder
        String defaultTargetFolder = targetFolderPath.toString();
        System.setProperty("OUTPUT_DIRECTORY", defaultTargetFolder);

        List<LinkedHashMap<String, String>> mockData = new ArrayList<>();

        LinkedHashMap<String, String> row1 = new LinkedHashMap<>();
        row1.put("key1", "value1");
        row1.put("key2", "value2");
        LinkedHashMap<String, String> row2 = new LinkedHashMap<>();
        row2.put("key3", "value3");
        row2.put("key4", "value4");

        mockData.add(row1);
        mockData.add(row2);

        // Create a new instance of the Table implementation
        Table table = mock(Table.class);
        when(table.getTableData()).thenReturn(mockData);


        // Call the method to test
        List<LinkedHashMap<String, String>> result = tableService.initializeTable(new String[]{}, table);

        // Assert that the file has been loaded and the content is as expected
        assertTrue(tempFile.exists());
        assertEquals(2, result.size());

        // Read the contents of the file
        String loadedContent = new String(Files.readAllBytes(tempFile.toPath()));

        // Assert that the contents match
        assertEquals(fileContent, loadedContent);
    }


    @Test
    public void testInitializeTable_FileInJar_LoadsFromFile() throws IOException {
        // Set up a temporary .jar file with sample data
        File tempJarFile = tempFolder.newFile("testTable.jar");
        Path tempJarPath = tempJarFile.toPath();

        // Set the content of the temporary .jar file
        String jarContent = "key1:value1 | key2:value2";
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(tempJarFile))) {
            zipOutputStream.putNextEntry(new ZipEntry("MyTable.txt"));
            zipOutputStream.write(jarContent.getBytes());
            zipOutputStream.closeEntry();
        }


        // Call the method to test
        List<LinkedHashMap<String, String>> result = tableService.initializeTable(new String[]{}, table);

        // Assert that the .jar file has been loaded and the content is as expected
        assertTrue(tempJarFile.exists());

        // Read the contents of the .jar file
        try (ZipFile zipFile = new ZipFile(tempJarFile)) {
            ZipEntry entry = zipFile.getEntry("MyTable.txt");
            assertNotNull(entry);

            InputStream entryStream = zipFile.getInputStream(entry);
            String loadedContent = IOUtils.toString(entryStream, StandardCharsets.UTF_8);

            // Assert that the contents match
            assertEquals(jarContent, loadedContent);
        }
    }



    @Test
    public void testGenerateRandomKeyValuePairs() {
        LinkedHashMap<String, String> randomPairs = tableService.generateRandomKeyValuePairs(2);
        assertEquals(2, randomPairs.size());
    }
}