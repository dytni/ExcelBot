package com.dytni.parser;

import com.dytni.repository.DataDAO;

import java.io.File;

public class AllParser {
    public static DataDAO parse(File file){
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            return ExcelParser.parseExcelFile(file);  // Парсим Excel
        } else if (fileName.endsWith(".pdf")) {
            return PdfParser.parsePdfFile(file);  // Парсим PDF
        } else {
            System.err.println("Файл имеет неподдерживаемый формат: " + fileName);
            return null;
        }
    }
}
