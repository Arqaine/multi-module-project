package org.arqaine.maven.app;

import org.arqaine.maven.model.Table;
import org.arqaine.maven.service.TableService;
import org.arqaine.maven.service.TableServiceImpl;
import org.arqaine.maven.util.InputHandler;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        Table table = new Table();
        TableService tableService = new TableServiceImpl();
        InputHandler inputHandler = new InputHandler();

        //Initializing the contents of the table
        tableService.initializeTable(args, table);

        //Saving
        tableService.saveTableToFile(table.getTableData(), table.getFilePath());


        int choice;
        do {

            System.out.println("\nMenu:");
            System.out.println("1. Search");
            System.out.println("2. Edit");
            System.out.println("3. Print");
            System.out.println("4. Reset");
            System.out.println("5. Add new row");
            System.out.println("6. Sort a row");
            System.out.println("7. Exit");

            choice = inputHandler.getUserInput(scanner, "Enter your Choice: ");

            switch (choice) {
                case 1:
                    //Search
                    String searchChoice = inputHandler.getChoice(scanner, "Do you want to search for a key (K) or a value (V)? ");
                    System.out.print("Enter the target: ");
                    tableService.search(searchChoice);
                    break;
                case 2:
                    //Edit
                    tableService.editCell();
                    tableService.saveTableToFile(table.getTableData(), table.getFilePath());
                    break;
                case 3:
                    //Print Table
                    System.out.println("\nTABLE\n");
                    tableService.printTable();
                    tableService.saveTableToFile(table.getTableData(), table.getFilePath());
                    break;
                case 4:
                    //Reset
                    int newRows = inputHandler.getUserInput(scanner, "Enter new number of rows: ");
                    int newCols = inputHandler.getUserInput(scanner, "Enter new number of columns: ");
                    tableService.reset(newRows, newCols);
                    tableService.saveTableToFile(table.getTableData(), table.getFilePath());
                    inputHandler.printCustomMessage("Table reset with new random data.");
                    break;
                case 5:
                    //Add new row
                    tableService.addNewRow();
                    tableService.saveTableToFile(table.getTableData(), table.getFilePath());
                    break;
                case 6:
                    //Sort row
                    tableService.sortRow();
                    tableService.saveTableToFile(table.getTableData(), table.getFilePath());
                    break;
                case 7:
                    System.out.println("Exiting program. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }

        } while (choice != 7);

        scanner.close();

    }
}

