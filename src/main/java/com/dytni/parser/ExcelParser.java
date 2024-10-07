package com.dytni.parser;

import com.dytni.logging.LoggSOP;
import com.dytni.repository.DataDAO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelParser {

    // Коллекции для каждого столбца
    public static List<String> numList = new ArrayList<>();
    public static List<String> productList = new ArrayList<>();
    public static List<Double> quantityList = new ArrayList<>();
    public static List<Double> priceList = new ArrayList<>();

    // Основной метод для парсинга файла


    // Парсинг Excel файла
    public static DataDAO parseExcelFile(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Workbook workbook = WorkbookFactory.create(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0);
            int skipFirstRow = 0;
            List<Integer> list = findLastOccurrence(file, "№");
            Map<String, Integer> map = findKeywordsFromRow(sheet, list.get(0));

            for (Row row : sheet) {
                if (skipFirstRow != list.get(0) + 1) {
                    skipFirstRow += 1;
                    continue; // Пропускаем заголовок
                }

                if (row.getCell(list.get(1)) == null || row.getCell(list.get(1)).getCellType() == CellType.BLANK) {
                    break;
                }

                // Проверяем тип ячейки и добавляем данные в коллекции
                numList.add(getStringCellValue(row.getCell(list.get(1))));
                if (map.get("наименование") != null) {
                    productList.add(getStringCellValue(row.getCell(map.get("наименование"))));
                } else if (map.get("товары") != null) {
                    productList.add(getStringCellValue(row.getCell(map.get("товары"))));
                } else {
                    productList.add(getStringCellValue(row.getCell(map.get("артикул"))));
                }

                if (map.get("кол-во") != null) {
                    quantityList.add(getNumericCellValue(row.getCell(map.get("кол-во"))));
                } else if (map.get("кол-") != null) {
                    quantityList.add(getNumericCellValue(row.getCell(map.get("кол-"))));
                } else {
                    quantityList.add(getNumericCellValue(row.getCell(map.get("Кол-во"))));
                }
                priceList.add(getNumericCellValue(row.getCell(map.get("цена"))));
            }

            LoggSOP.printDataInTable(numList, productList, quantityList, priceList);
            return new DataDAO(numList, productList, quantityList, priceList);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Метод для получения строкового значения ячейки
    private static String getStringCellValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return "";  // Возвращаем пустую строку, если ячейка пуста
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            default -> "";
        };
    }

    // Метод для получения числового значения ячейки
    private static Double getNumericCellValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;  // Возвращаем null вместо 0.0 для пустых значений
        }
        switch (cell.getCellType()) {
            case NUMERIC -> {
                return cell.getNumericCellValue();
            }
            case STRING -> {
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return null;  // Если не удаётся преобразовать в число, возвращаем null
                }
            }
            default -> {
                return null;
            }
        }
    }

    public static List<Integer> findLastOccurrence(File file, String target) {
        int lastRow = -1;      // Номер последней строки с вхождением "№"
        int lastCol = -1;      // Номер последней колонки с вхождением "№"
        List<Integer> list = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);  // Чтение первой страницы

            // Проход по строкам и колонкам, чтобы найти последнюю непустую ячейку с "№"
            for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue; // Пропустить пустые строки

                for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
                    Cell cell = row.getCell(colIndex);
                    if (cell == null) continue; // Пропустить пустые ячейки

                    if (cell.getCellType() == CellType.STRING && target.equals(cell.getStringCellValue().trim())) {
                        lastRow = rowIndex;
                        lastCol = colIndex;
                    }
                }
            }

            list.add(lastRow);
            list.add(lastCol);
            return list;

        } catch (IOException e) {
            e.printStackTrace();
            return List.of(0,0);
        }
    }

    public static void clear(){
        numList.clear();
        productList.clear();
        quantityList.clear();
        priceList.clear();
    }

    private static Map<String, Integer> findKeywordsFromRow(Sheet sheet, int startRow) {
        Map<String, Integer> foundKeywords = new HashMap<>();
        String[] keywords = {"кол-во", "цена", "наименование", "товары", "кол-"};

        for (int rowIndex = startRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;

            for (int colIndex = 0; colIndex < row.getLastCellNum(); colIndex++) {
                Cell cell = row.getCell(colIndex);
                if (cell == null || cell.getCellType() != CellType.STRING) continue;

                String cellValue = cell.getStringCellValue().trim().toLowerCase();

                for (String keyword : keywords) {
                    if (cellValue.contains(keyword.toLowerCase())) {
                        foundKeywords.put(keyword, colIndex);
                    }
                }
            }

            if (foundKeywords.size() == keywords.length) {
                break;
            }
        }

        return foundKeywords;
    }

}
