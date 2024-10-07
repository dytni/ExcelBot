package com.dytni.logging;

import java.util.List;

public class LoggSOP {
    public static void printDataInTable(List<String> numList, List<String> productList, List<Double> quantityList, List<Double> priceList) {
        // Шапка таблицы
        System.out.printf("%-4s %-60s %-8s %-8s%n",
                "№", "Товары (работы, услуги)", "Кол-во", "Цена");
        System.out.println("----------------------------------------------------------------------");

        // Вывод строк с данными
        for (int i = 0; i < numList.size(); i++) {
            System.out.printf("%-4s %-60s %-8.2f %-8.2f%n",
                    numList.get(i),               // Номер
                    productList.get(i),            // Товары (работы, услуги)
                    quantityList.get(i),           // Количество
                    priceList.get(i));             // Цена
        }
    }
}
