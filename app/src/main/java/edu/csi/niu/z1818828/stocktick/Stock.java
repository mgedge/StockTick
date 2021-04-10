package edu.csi.niu.z1818828.stocktick;

public class Stock {
    String symbol;
    String stockName;
    double price;
    double openPrice;
    double closePrice;
    double weekHigh;
    double weekLow;
    double dayHigh;
    double dayLow;
    double volume;
    double range;

    public Stock(String symbol, String stockName, double price, double openPrice, double closePrice,
                 double weekHigh, double weekLow, double dayHigh, double dayLow, double volume, double range) {
        this.symbol = symbol;
        this.stockName = stockName;
        this.price = price;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.weekHigh = weekHigh;
        this.weekLow = weekLow;
        this.dayHigh = dayHigh;
        this.dayLow = dayLow;
        this.volume = volume;
        this.range = range;
    }
}

