package org.arqaine.maven.util;

import org.apache.commons.lang3.StringUtils;
import java.util.Scanner;

public class InputHandler {
    private final Scanner scanner;

    public InputHandler(Scanner scanner) {
        this.scanner = scanner;
    }

    public void printCustomMessage(String message) {
        int messageLength = message.length();
        String separator = StringUtils.repeat("=", messageLength);
        System.out.println(separator);
        System.out.println(message);
        System.out.println(separator);
    }

    public String getChoice(Scanner scanner, String prompt) {
        String choice;
        do {
            System.out.print(prompt);
            choice = scanner.next();
            if (!choice.equalsIgnoreCase("K") && !choice.equalsIgnoreCase("V")) {
                System.out.println("Invalid input. Please enter 'K' or 'V'.");
            }
        } while (!choice.equalsIgnoreCase("K") && !choice.equalsIgnoreCase("V"));
        return choice;
    }

    public int getUserInput(Scanner scanner, String prompt) {
        int userInput;
        do {
            System.out.print(prompt);
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input! Please enter a valid number: ");
                scanner.next(); // Consume invalid input
            }
            userInput = scanner.nextInt();
        } while (userInput < 0); // Keep prompting until a positive integer is entered
        return userInput;
    }

    public String getUserInputString(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.next();
    }

}
