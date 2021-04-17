package edu.csi.niu.z1818828.stocktick.objects;

import android.util.Log;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Scanner;

public class Stock {
    String symbol;
    String stockName;
    String exchange;
    String date;
    double price;
    double openPrice;
    //    double closePrice;
//    double weekHigh;
//    double weekLow;
    double dayHigh;
    double dayLow;
    double volume;
    double change;
    double changePct;
    boolean selected = false;

    public Stock() {
    }

    public Stock(String symbol, String stockName, String exchange, double price, double openPrice, double closePrice,
                 double weekHigh, double weekLow, double dayHigh, double dayLow, double volume, double range) {
        this.symbol = symbol;
        this.stockName = stockName;
        this.exchange = exchange;
        this.price = price;
        this.openPrice = openPrice;
        this.dayHigh = dayHigh;
        this.dayLow = dayLow;
        this.volume = volume;
    }

    public String prettifyVolume() {
        DecimalFormat decimalFormat = new DecimalFormat("#,###.00");

        if (volume < 1000)
            return decimalFormat.format(volume);
        else if (volume < 1000000)
            return decimalFormat.format(volume / 1000) + "K";
        else if (volume < 1000000000)
            return decimalFormat.format(volume / 1000000) + "M";
        else if (volume >= 1000000000)
            return decimalFormat.format(volume / 1000000000) + "B";
        else
            return decimalFormat.format(volume);
    }

    public String prettifyVolumeLabel(float label) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");

        if (label < 1000)
            return decimalFormat.format(label);
        else if (label < 1000000)
            return decimalFormat.format(label / 1000) + "K";
        else if (label < 1000000000)
            return decimalFormat.format(label / 1000000) + "M";
        else if (label >= 1000000000)
            return decimalFormat.format(label / 1000000000) + "B";
        else
            return decimalFormat.format(label);
    }

    public String formatDateDay(String date) {
        if (date != null) {
            String year, month, day;
            Scanner scanner = new Scanner(date);
            scanner.useDelimiter("-");

            year = scanner.next();
            month = scanner.next();
            day = scanner.next();

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                LocalDate localDate = LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
                java.time.DayOfWeek dayOfWeek = localDate.getDayOfWeek();

                return dayOfWeek + ", " + month + "/" + day + "/" + year;
            } else {
                return month + "/" + day + "/" + year;
            }
        } else {
            Log.e("formateDateDay", "Date was null and could not format");
            return date;
        }
    }

    public String formatDate(String date) {
        String year, month, day;
        Scanner scanner = new Scanner(date);
        scanner.useDelimiter("-");

        scanner.next();
        month = scanner.next();
        day = scanner.next();

        return month + "/" + day;
    }

    public String formatPrice(double price) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        return decimalFormat.format(price);
    }

    public String formatPriceLabel(float price) {
        DecimalFormat decimalFormat = new DecimalFormat("##,###");
        String item;

        item = decimalFormat.format(price);
//        if(price < 0) {
//            item = String.format("0.3f", price);
//        }
//        else {
////            item = decimalFormat.format(price);
////            item = String.format("%4$f", price);
//        }


        return item;
    }

    public String formatChange(double _change) {
        return new DecimalFormat("#,###.##").format(_change);
    }

    public String formatChangePercentage(double _change) {
        DecimalFormat format = new DecimalFormat("#,##0.00");
        return format.format(_change) + "%";
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getChangePct() {
        return changePct;
    }

    public void setChangePct(double changePct) {
        this.changePct = changePct;
    }

    //Adapter methods
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }
}

