package com.dytni.parser;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExcelWriter {

    public static String writeExcelFile(List<String> numList, List<String> productList,
                                        List<Double> quantityList, List<Double> priceList) {

        // Создаем новый workbook
        Workbook workbook = new XSSFWorkbook();

        // Создаем новый лист
        Sheet sheet = workbook.createSheet("Products");

        // Создаем строку заголовка
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("№");
        headerRow.createCell(1).setCellValue("Товары (работы, услуги)");
        headerRow.createCell(2).setCellValue("Кол-во");
        headerRow.createCell(3).setCellValue("Цена");

        // Заполняем данные
        for (int i = 0; i < numList.size(); i++) {
            Row row = sheet.createRow(i + 1);  // Начинаем с 1, т.к. строка 0 - это заголовок

            // Проверка на null и запись значения или пустой строки
            row.createCell(0).setCellValue(numList.get(i) != null ? numList.get(i) : "");
            row.createCell(1).setCellValue(productList.get(i) != null ? productList.get(i) : "");

            // Для числовых значений: если null, записываем 0
            row.createCell(2).setCellValue(quantityList.get(i) != null ? quantityList.get(i) : 0.0);
            row.createCell(3).setCellValue(priceList.get(i) != null ? ((double) Math.round((priceList.get(i) * 1.2 * 1.3) * 100) / 100) : 0.0);
        }

        // Автоматическое выравнивание столбцов по содержимому
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }

        // Генерация уникального имени файла
        String fileName = generateFileName();

        // Создание пути к файлу (создаём в текущей директории или временной папке)
        Path filePath = Paths.get(System.getProperty("user.dir"), "temp", fileName);

        // Убедитесь, что директория существует
        try {
            Files.createDirectories(filePath.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Сохраняем файл
        try (FileOutputStream fileOut = new FileOutputStream(filePath.toFile())) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Возвращаем полный путь к файлу
        return filePath.toString();
    }

    // Метод для генерации уникального имени файла
    private static String generateFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return "products_" + timeStamp + ".xlsx";
    }
}
