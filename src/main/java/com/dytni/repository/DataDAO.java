package com.dytni.repository;

import java.util.List;

public class DataDAO {
    public List<String> numList;
    public List<String> productList;
    public List<Double> quantityList;
    public List<Double> priceList;


    public DataDAO(List<String> numList, List<String> productList, List<Double> quantityList, List<Double> priceList) {
        this.numList = numList;
        this.productList = productList;
        this.quantityList = quantityList;
        this.priceList = priceList;
    }

    public void multiplyPriceList(double coefficient) {
        priceList.replaceAll(aDouble -> {
            if (aDouble == null) {
                return null; // Или оставить null, если это необходимо
            }
            double result = aDouble * coefficient * 1.2;
            return (double) Math.round(result * 100.0) / 100.0; // Округление до двух знаков
        });
    }


}
