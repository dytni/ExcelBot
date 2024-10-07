package com.dytni.parser;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


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
        headerRow.createCell(4).setCellValue("Итог за товар");  // Новая колонка

        double total = 0.0;

        // Заполняем данные
        for (int i = 0; i < numList.size(); i++) {
            Row row = sheet.createRow(i + 1);  // Начинаем с 1, т.к. строка 0 - это заголовок

            // Проверка на null и запись значения или пустой строки
            String num = numList.get(i) != null ? numList.get(i) : "";
            String product = productList.get(i) != null ? productList.get(i) : "";
            double quantity = quantityList.get(i) != null ? quantityList.get(i) : 0.0;
            double price = priceList.get(i) != null ? priceList.get(i) : 0.0;

            row.createCell(0).setCellValue(num);
            row.createCell(1).setCellValue(product);
            row.createCell(2).setCellValue(quantity);
            row.createCell(3).setCellValue(price);

            // Рассчитываем итог за товар (количество * цена)
            double itemTotal = quantity * price;
            row.createCell(4).setCellValue(itemTotal);  // Запись итога за товар

            // Добавляем в итоговую сумму
            total += itemTotal;
        }

        // Автоматическое выравнивание столбцов по содержимому
        for (int i = 0; i < 5; i++) {  // Теперь 5 колонок, включая "Итог за товар"
            sheet.autoSizeColumn(i);
        }

        // Добавляем строку "Итого" в конце
        Row totalRow = sheet.createRow(numList.size() + 1); // Следующая строка после данных
        totalRow.createCell(1).setCellValue("Итого:");
        totalRow.createCell(4).setCellValue(total); // Общая сумма в колонке "Итог за товар"

        // Автоматическое выравнивание для новых строк
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(4);

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
