package edu.csi.niu.z1818828.stocktick.ui.stock;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;

import edu.csi.niu.z1818828.stocktick.R;

public class StockActivity extends AppCompatActivity {
    private TextView textViewStockPrice;
    private TextView textViewDayChange;
    private TextView textViewDayChangePercent;
    private TextView textViewDate;
    private TextView textViewOpenValue;
    private TextView textViewDayRangeValue;
    private TextView textViewVolumeValue;
    private TextView textViewCloseValue;
    private TextView textViewWeekRangeValue;
    private TextView textViewPercRangeValue;
    private LineChart lineChart;
    private Button buttonWatchlist;

    private String stockSymbol;
    private String stockName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);


        ActionBar toolbar = getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        toolbar.back



        buttonWatchlist = findViewById(R.id.buttonAddToWatchlist);
        buttonWatchlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Added to the watchlist", Toast.LENGTH_SHORT).show();
            }
        });

        //Get the stock info
        try {
            Intent intent = getIntent();
            stockName = intent.getStringExtra("stockName");
            stockSymbol = intent.getStringExtra("stockSymbol");
        }
        catch (Exception e) {
            e.printStackTrace();
        }


        //Set the title of the page
        setTitle(stockSymbol);
        getSupportActionBar().setSubtitle(stockName);

        //Setup the chart
        {
            lineChart = findViewById(R.id.chart);

            // disable description text
            lineChart.getDescription().setEnabled(false);

            // enable touch gestures
            lineChart.setTouchEnabled(true);

            // set listeners
            //lineChart.setOnChartValueSelectedListener((OnChartValueSelectedListener) this);
            lineChart.setDrawGridBackground(false);

//            // create marker to display box when values are selected
//            MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
//
//            // Set the marker to the chart
//            mv.setChartView(chart);
//            chart.setMarker(mv);

            // enable scaling and dragging
            lineChart.setDragEnabled(true);
            lineChart.setScaleEnabled(true);
            // chart.setScaleXEnabled(true);
            // chart.setScaleYEnabled(true);

            // force pinch zoom along both axis
            lineChart.setPinchZoom(true);
        }
        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = lineChart.getXAxis();

            xAxis.setDrawLabels(true);

            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f);
        }
        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = lineChart.getAxisLeft();

            yAxis.setDrawLabels(true);

            // disable dual axis (only use LEFT axis)
            lineChart.getAxisRight().setEnabled(false);

            // horizontal grid lines
            yAxis.enableGridDashedLine(10f, 10f, 0f);

            // axis range
//            yAxis.setAxisMaximum(200f);
            yAxis.setAxisMinimum(0);
        }

        //Add data
        setDataChart(45, 100);
        lineChart.animateX(1500);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void setDataChart(int count, float range) {
        ArrayList<Entry> values = new ArrayList<>();

        for(int i = 0; i < count; i++) {
            //set value
            float val = (float) (Math.random() * range) + 30;
            values.add(new Entry(i, val));
        }

        LineDataSet set;

        if(lineChart.getData() != null && lineChart.getData().getDataSetCount() > 0) {
            set = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            set.setValues(values);
            set.notifyDataSetChanged();
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
        }
        else {
            // create a dataset and give it a type
            set = new LineDataSet(values, "Data Set");

            // black lines and points
            set.setColor(R.color.colorStockChartGradient);
            set.setCircleColor(R.color.colorStockChartGradient);

            // line thickness
            set.setLineWidth(1f);

            //Disable points
            set.setDrawCircles(false);
            set.setDrawValues(false);

            // customize legend entry
            set.setFormLineWidth(1f);
            set.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set.setFormSize(15.f);

            // draw selection line as dashed
//            set.enableDashedHighlightLine(10f, 5f, 0f);
            set.disableDashedHighlightLine();

            // set the filled area
            set.setDrawFilled(true);
            set.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return lineChart.getAxisLeft().getAxisMinimum();
                }
            });

            // set color of filled area
            if (Utils.getSDKInt() >= 18) {
                // drawables only supported on api level 18 and above
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_chart);
                set.setFillDrawable(drawable);
            } else {
                set.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // set data
            lineChart.setData(data);
        }
    }

    private void setDataVolume(int count, float range) {
        ArrayList<Entry> values = new ArrayList<>();

        for(int i = 0; i < count; i++) {
            //set value
            float val = (float) (Math.random() * range) + 30;
            values.add(new Entry(i, val));
        }

        LineDataSet set;

        if(lineChart.getData() != null && lineChart.getData().getDataSetCount() > 0) {
            set = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            set.setValues(values);
            set.notifyDataSetChanged();
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
        }
        else {
            // create a dataset and give it a type
            set = new LineDataSet(values, "Data Set");

            // black lines and points
            set.setColor(R.color.colorStockChartGradient);
            set.setCircleColor(R.color.colorStockChartGradient);

            // line thickness
            set.setLineWidth(1f);

            //Disable points
            set.setDrawCircles(false);
            set.setDrawValues(false);

            // customize legend entry
            set.setFormLineWidth(1f);
            set.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set.setFormSize(15.f);

            // draw selection line as dashed
//            set.enableDashedHighlightLine(10f, 5f, 0f);
            set.disableDashedHighlightLine();

            // set the filled area
            set.setDrawFilled(true);
            set.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return lineChart.getAxisLeft().getAxisMinimum();
                }
            });

            // set color of filled area
            if (Utils.getSDKInt() >= 18) {
                // drawables only supported on api level 18 and above
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_chart);
                set.setFillDrawable(drawable);
            } else {
                set.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // set data
            lineChart.setData(data);
        }
    }
}