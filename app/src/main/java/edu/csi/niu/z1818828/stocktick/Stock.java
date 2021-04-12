package edu.csi.niu.z1818828.stocktick;

public class Stock {
    String symbol;
    String stockName;
    String exchange;
    double price;
    double openPrice;
    double closePrice;
    double weekHigh;
    double weekLow;
    double dayHigh;
    double dayLow;
    double volume;
    double range;

    public Stock(String symbol, String stockName, String exchange, double price, double openPrice, double closePrice,
                 double weekHigh, double weekLow, double dayHigh, double dayLow, double volume, double range) {
        this.symbol = symbol;
        this.stockName = stockName;
        this.exchange = exchange;
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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }

    public double getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(double closePrice) {
        this.closePrice = closePrice;
    }

    public double getWeekHigh() {
        return weekHigh;
    }

    public void setWeekHigh(double weekHigh) {
        this.weekHigh = weekHigh;
    }

    public double getWeekLow() {
        return weekLow;
    }

    public void setWeekLow(double weekLow) {
        this.weekLow = weekLow;
    }

    public double getDayHigh() {
        return dayHigh;
    }

    public void setDayHigh(double dayHigh) {
        this.dayHigh = dayHigh;
    }

    public double getDayLow() {
        return dayLow;
    }

    public void setDayLow(double dayLow) {
        this.dayLow = dayLow;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }
}

