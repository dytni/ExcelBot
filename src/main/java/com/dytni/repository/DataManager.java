package com.dytni.repository;

import com.dytni.parser.AllParser;
import com.dytni.parser.ExcelParser;
import com.dytni.parser.ExcelWriter;


import java.io.File;




public class DataManager {
    static DataDAO dao;

    public static void setCoefficient(double coefficient){

         dao.multiplyPriceList(coefficient);
    }


public static String parse(File file){
    dao = AllParser.parse(file);
    if(dao != null){
        dao.multiplyPriceList(1.3);
        String filePath = ExcelWriter.writeExcelFile(dao.numList,dao.productList,dao.quantityList,dao.priceList);
        ExcelParser.clear();
        deleteAllFilesInDirectory(new File("files"));
        return filePath;
    }
    else{
        return "";
    }
}
    private static void deleteAllFilesInDirectory(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isFile()) {
                        if (file.delete()) {
                            System.out.println("Файл " + file.getName() + " успешно удалён.");
                        } else {
                            System.err.println("Не удалось удалить файл " + file.getName() + ".");
                        }
                    }
                }
            } else {
                System.out.println("Папка пуста или не содержит файлов.");
            }
        } else {
            System.err.println("Директория " + directory.getName() + " не существует.");
        }
    }

}
