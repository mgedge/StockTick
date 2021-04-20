/************************************************************************
 * 	File Name: Stock.java			        		    				*
 * 																		*
 *  Developer: Matthew Gedge											*
 *   																	*
 *    Purpose: This java class defines a stock object and allows for    *
 *    manipulation of the object variables.                             *
 *																		*
 * *********************************************************************/
package edu.csi.niu.z1818828.stocktick.objects;

import android.util.Log;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Scanner;
import java.lang.String;

public class Stock {
    String symbol;
    String stockName;
    String exchange;
    String date;
    double price;
    double openPrice;
    double dayHigh;
    double dayLow;
    double volume;
    double change;
    double changePct;
    boolean selected = false;

    /**
     * Create an empty stock object.
     * A stock has many properties and may or may not have all the properties available.
     */
    public Stock() {
    }

    /**
     * Format the volume such that it is (ideally) 3 digits with a character to denote the scale.
     *
     * @return a string for the formatted volume
     */
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

    /**
     * Format the input value such that it is (ideally) 3 digits with a character to denote the scale.
     * This is used for the volume chart
     *
     * @param label - float value for the volume
     * @return a string for the formatted volume
     */
    public String prettifyVolumeLabel(float label) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String item;

        if (label < 1000)
            item = decimalFormat.format(label);
        else if (label < 1000000)
            item = decimalFormat.format(label / 1000) + "K";
        else if (label < 1000000000)
            item = decimalFormat.format(label / 1000000) + "M";
        else if (label >= 1000000000)
            item = decimalFormat.format(label / 1000000000) + "B";
        else
            item = decimalFormat.format(label);

        for (int i = 0; i < 6; i++) {
            if (item.length() != 6) {
                item += " ";
            }
        }

        return item;
    }

    /**
     * Format the date to have the day of the week and date (eg. Sunday, 1/1/2000)
     *
     * @param date - date as a string formatted as (yyyy-mm-dd)
     * @return a string with the new format
     */
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

    /**
     * Format the date without the year or day
     *
     * @param date - string for the date (yyyy-mm-dd)
     * @return a string with format (mm/dd)
     */
    public String formatDate(String date) {
        String year, month, day;
        Scanner scanner = new Scanner(date);
        scanner.useDelimiter("-");

        scanner.next();
        month = scanner.next();
        day = scanner.next();

        return month + "/" + day;
    }

    /**
     * Format the price to have two decimal decimal places
     *
     * @param price - the price as a double
     * @return a string with the formatted price (e.g. 1,000.00)
     */
    public String formatPrice(double price) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        return decimalFormat.format(price);
    }

    /**
     * Format the label for the price chart
     *
     * @param price - float value for the price to be formatted
     * @return a string for the formatted price
     */
    public String formatPriceLabel(float price) {
        DecimalFormat decimalFormat = new DecimalFormat("##,###");
        String item;

        item = decimalFormat.format(price);

        for (int i = 0; i < 6; i++) {
            if (item.length() != 6) {
                item += " ";
            }
        }

        return item;
    }

    /**
     * Format the price change
     *
     * @param _change - the change value to be formatted
     * @return a string for the formatted change
     */
    public String formatChange(double _change) {
        return new DecimalFormat("#,###.##").format(_change);
    }

    /**
     * Format the price change as a percentage
     *
     * @param _change - the change percentage value to be formatted
     * @return a string for the formatted change percentage
     */
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

    //Adapter methods - used to determine if the stock is selected in the list
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}

