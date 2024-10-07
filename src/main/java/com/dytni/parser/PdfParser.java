package com.dytni.parser;

import com.dytni.logging.LoggSOP;
import com.dytni.repository.DataDAO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfParser {
    public static List<String> numList = new ArrayList<>();
    public static List<String> productList = new ArrayList<>();
    public static List<Double> quantityList = new ArrayList<>();
    public static List<Double> priceList = new ArrayList<>();
    public static DataDAO parsePdfFile(File file) {
        try (PDDocument document = PDDocument.load(file)) {
            // Use PDFTextStripper to extract text from PDF
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String pdfText = pdfStripper.getText(document);

           String lines = extractDataAfterLastSymbol(pdfText, "№");


            // Find the header row containing the keywords
           parseData(lines.split("\n"));

            LoggSOP.printDataInTable(numList, productList, quantityList, priceList);
            return new DataDAO(numList, productList, quantityList, priceList);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private static Map<String, Integer> findKeywordsInLines(String[] lines) {
        Map<String, Integer> foundKeywords = new HashMap<>();
        String[] keywords = {"кол-во", "цена", "наименование", "товары", "кол-"};

        // Find the header line with keywords
        String headerLine = lines[0].toLowerCase(); // Assuming the first line is the header

        for (String keyword : keywords) {
            int index = headerLine.indexOf(keyword.toLowerCase());
            if (index != -1) {
                foundKeywords.put(keyword, headerLine.split("\\s+").length); // Store the column index
            }
        }

        return foundKeywords;
    }

    public static String extractDataAfterLastSymbol(String pdfText, String symbol) {
        // Split the text into lines
        String[] lines = pdfText.split("\n");

        // Initialize variables to hold the index of the last occurrence
        int lastSymbolRow = -1;

        // Iterate through the lines to find the last occurrence of the symbol
        for (int rowIndex = 0; rowIndex < lines.length; rowIndex++) {
            String line = lines[rowIndex];
            int index = line.lastIndexOf(symbol);
            if(index >= 0 ){
                lastSymbolRow = rowIndex; // Keep track of the row index
            }
        }

        // If the symbol was found
        if (lastSymbolRow != -1) {
            // Create a StringBuilder to hold the resulting data
            StringBuilder result = new StringBuilder();

            // Extract only the data after the last occurrence of the symbol
            for (int rowIndex = lastSymbolRow; rowIndex < lines.length; rowIndex++) {
                result.append(lines[rowIndex].trim()).append("\n");
            }

            // Return the resulting data as an array of lines
            return result.toString();
        }

        // Return an empty array if the symbol was not found
        return "";
    }
    public static void clear(){
        numList.clear();
        productList.clear();
        quantityList.clear();
        priceList.clear();
    }

    public static void parseData(String[] lines) {
        for (String line : lines) {
            // Trim whitespace and skip empty lines
            line = line.trim();
            if (line.isEmpty()) continue;

            // Check if the line is a header or summary line by checking specific keywords
            if (line.startsWith("№") || line.startsWith("ИТОГО:") || line.startsWith("Всего на сумму:") || line.contains("Сумма НДС")) {
                continue; // Skip headers and summary lines
            }

            // Split the line into parts based on whitespace
            String[] parts = line.split("\\s+");

            // Check if the line has at least 4 parts (Num, Product, Quantity, Price)
            if (parts.length >= 4) {
                try {
                    // Extract the number, product, quantity, and price
                    String number = parts[0]; // Article number
                    // Product can be multiple parts, collect until the last two parts (Quantity and Price)
                    String product = String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length - 3));
                    double quantity = parseQuantity(parts[parts.length - 3]); // Quantity
                    double price = parsePrice(parts[parts.length - 2]); // Price

                    // Add the extracted data to the corresponding lists
                    if (quantity != -1 && price != -1) { // Ensure quantity and price are valid
                        numList.add(number);
                        productList.add(product);
                        quantityList.add(quantity);
                        priceList.add(price);
                    }
                } catch (NumberFormatException e) {
                    // Handle parsing error for quantity and price
                    System.err.println("Failed to parse quantity or price for line: " + line);
                }
            }
        }
    }

    // Method to parse quantity
    private static double parseQuantity(String quantityStr) {
        // Remove any non-numeric characters except for comma
        quantityStr = quantityStr.replaceAll("[^0-9,]", "").trim();
        // Replace comma with dot for parsing
        quantityStr = quantityStr.replace(',', '.');
        try {
            return Double.parseDouble(quantityStr);
        } catch (NumberFormatException e) {
            return -1; // Indicate invalid quantity
        }
    }

    // Method to parse price
    private static double parsePrice(String priceStr) {
        // Remove any non-numeric characters except for comma
        priceStr = priceStr.replaceAll("[^0-9,]", "").trim();
        // Replace comma with dot for parsing
        priceStr = priceStr.replace(',', '.');
        try {
            return Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            return -1; // Indicate invalid price
        }
    }

    // For testing purposes
}
