package com.example.sam.sc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.NumberFormat;
import java.util.ArrayList;


public class GraphActivity extends AppCompatActivity {

    protected static ArrayList<XYValue> XY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
//        intent.getIntegerArrayListExtra("XYValue:");
        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        for (int i = 0; i < XY.size(); i++) {
            try {
                double x = XY.get(i).getX();
                double y = XY.get(i).getY();
                series.appendData(new DataPoint(x, y), true, 1000);
            } catch (IllegalArgumentException e) {
//                Log.e(TAG, "createScatterPlot: IllegalArgumentException: " + e.getMessage() );
                e.printStackTrace();
            }
        }
//        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
//                new DataPoint(0, 1),
//                new DataPoint(1, 5),
//                new DataPoint(2, 3),
//                new DataPoint(3, 2),
//                new DataPoint(4, 6)
//        });

        graph.addSeries(series);
//        graph.computeScroll();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(0);
        nf.setMinimumIntegerDigits(1);

        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf, nf));
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Trials");
        graph.getGridLabelRenderer().setVerticalAxisTitle("PI");


//        graph.canScrollHorizontally(1);
    }

    @Override
    public void onBackPressed() {
//        Intent
        Intent intent = getParentActivityIntent();
        startActivity(intent);
//        finish();
//        finish();
    }
}
