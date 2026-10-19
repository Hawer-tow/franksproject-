package com.example.mobiledbclient.ui.Kisenyi.Z_kisenyiFunctionsHandler;

import android.app.Activity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.mobiledbclient.ApiClient;
import com.example.mobiledbclient.ApiService;
import com.example.mobiledbclient.R;
import com.example.mobiledbclient.ui.Kisenyi.kisenyidtos.KisenyiCashSummaryDto;
import com.example.mobiledbclient.ui.Kisenyi.openandclosings.OpenAndClose;

import com.example.mobiledbclient.ui.Kisenyi.Z_kisenyiFunctionsHandler.*;
import com.example.mobiledbclient.ui.Kisenyi.openandclosings.OpenAndCloseAdapter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class shopsummary_Functions {

    private ApiService apiService;
    private ProgressBar loadingIndicator;
    private TextView loadingMessage;
    private View loadingOverlay;

    public void runSalesQueries(Context context, String date,
                                View loadingOverlay,
                                TextView loadingMessage,
                                RecyclerView recyclerView,
                                OpenAndCloseAdapter adapter) {


        if (apiService == null) {
            apiService = ApiClient.getClient().create(ApiService.class);
        }

        apiService.getCashSummary(date).enqueue(new Callback<KisenyiCashSummaryDto>() {
            @Override
            public void onResponse(Call<KisenyiCashSummaryDto> call, Response<KisenyiCashSummaryDto> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    showResultDialog(context, "Server error", "Server is OFFLINE\nCannot respond to " + date + " Data.");
                    return;
                }

                KisenyiCashSummaryDto summary = response.body();

                LayoutInflater inflater = LayoutInflater.from(context);
                View chartView = inflater.inflate(R.layout.kisenyi_chart_popup, null);
                BarChart chart = chartView.findViewById(R.id.salesChart);

                // Resolve theme attributes for colors
                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = context.getTheme();

                theme.resolveAttribute(R.attr.summary_salesColor, typedValue, true);
                int salesColor = typedValue.data;
                theme.resolveAttribute(R.attr.summary_buysColor, typedValue, true);
                int buysColor = typedValue.data;
                theme.resolveAttribute(R.attr.summary_expensesColor, typedValue, true);
                int expensesColor = typedValue.data;
                theme.resolveAttribute(R.attr.summary_openingColor, typedValue, true);
                int openingColor = typedValue.data;
                theme.resolveAttribute(R.attr.summary_expectedColor, typedValue, true);
                int expectedColor = typedValue.data;
                theme.resolveAttribute(R.attr.summary_recordedColor, typedValue, true);
                int recordedColor = typedValue.data;

                theme.resolveAttribute(R.attr.Primarycolor, typedValue, true);
                int textColor = typedValue.data;
                theme.resolveAttribute(R.attr.background_when_in_Primarycolor, typedValue, true);
                int backgroundColor = typedValue.data;

                // Build datasets
                List<IBarDataSet> dataSets = new ArrayList<>();
                dataSets.add(makeDataSet("Sales", 0, (float) summary.getSellingTotal(), salesColor, textColor));
                dataSets.add(makeDataSet("Buys", 1, (float) summary.getBuyingTotal(), buysColor, textColor));
                dataSets.add(makeDataSet("Expenses", 2, (float) summary.getExpensesTotal(), expensesColor, textColor));
                dataSets.add(makeDataSet("Opening", 3, (float) summary.getUgandaOpeningCash(), openingColor, textColor));
                dataSets.add(makeDataSet("Expected", 4, (float) summary.getNetCash(), expectedColor, textColor));
                dataSets.add(makeDataSet("Recorded", 5, (float) summary.getFinalClosingCash(), recordedColor, textColor));

                BarData barData = new BarData(dataSets);
                barData.setBarWidth(0.4f);
                chart.setData(barData);
                chart.setBackgroundColor(backgroundColor);

                XAxis xAxis = chart.getXAxis();
                xAxis.setDrawLabels(false);
                xAxis.setAxisMinimum(-0.5f);
                xAxis.setAxisMaximum(5.5f);

                chart.setExtraTopOffset(30f);
                chart.getDescription().setEnabled(false);
                chart.setFitBars(true);
                chart.setExtraBottomOffset(60f);

                Legend legend = chart.getLegend();
                legend.setEnabled(true);
                legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
                legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
                legend.setWordWrapEnabled(true);
                legend.setTextSize(12f);

                List<LegendEntry> legendEntries = new ArrayList<>();
                legendEntries.add(new LegendEntry("Sales", Legend.LegendForm.SQUARE, 10f, 2f, null, salesColor));
                legendEntries.add(new LegendEntry("Buys", Legend.LegendForm.SQUARE, 10f, 2f, null, buysColor));
                legendEntries.add(new LegendEntry("Expenses", Legend.LegendForm.SQUARE, 10f, 2f, null, expensesColor));
                legendEntries.add(new LegendEntry("Opening", Legend.LegendForm.SQUARE, 10f, 2f, null, openingColor));
                legendEntries.add(new LegendEntry("Expected", Legend.LegendForm.SQUARE, 10f, 2f, null, expectedColor));
                legendEntries.add(new LegendEntry("Recorded", Legend.LegendForm.SQUARE, 10f, 2f, null, recordedColor));
                legend.setCustom(legendEntries);

                chart.invalidate();
                checkBalanceAndUpdate(context, summary, date,
                        loadingOverlay, loadingMessage, recyclerView, adapter);

                new AlertDialog.Builder(context)
                        .setTitle("Kisenyi Cash Summary : " + date)
                        .setView(chartView)
                        .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                        .setNegativeButton("Share", (dialog, which) -> {
                            try {
                                Bitmap chartBitmap = chart.getChartBitmap();
                                File cachePath = new File(context.getCacheDir(), "images");
                                cachePath.mkdirs();
                                File file = new File(cachePath, "chart.png");
                                FileOutputStream stream = new FileOutputStream(file);
                                chartBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                stream.close();

                                Uri contentUri = FileProvider.getUriForFile(context,
                                        context.getPackageName() + ".fileprovider", file);

                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("image/png");
                                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                List<ResolveInfo> resInfoList = context.getPackageManager()
                                        .queryIntentActivities(shareIntent, PackageManager.MATCH_DEFAULT_ONLY);
                                for (ResolveInfo resolveInfo : resInfoList) {
                                    String packageName = resolveInfo.activityInfo.packageName;
                                    context.grantUriPermission(packageName, contentUri,
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                }

                                context.startActivity(Intent.createChooser(shareIntent, "Share chart via"));
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(context, "Unable to share chart", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }

            @Override
            public void onFailure(Call<KisenyiCashSummaryDto> call, Throwable t) {
                showResultDialog(context, "Connection Error", "You might be OFFLINE!! " + t.getMessage());
            }
        });
    }

    private BarDataSet makeDataSet(String label, int xIndex, float value, int color, int textColor) {
        BarDataSet set = new BarDataSet(Collections.singletonList(new BarEntry(xIndex, value)), label);
        set.setColor(color);
        set.setValueTextColor(textColor);
        set.setValueTextSize(5f);
        return set;
    }

    private void checkBalanceAndUpdate(Context context,
                                       KisenyiCashSummaryDto summary,
                                       String date,
                                       View loadingOverlay,
                                       TextView loadingMessage,
                                       RecyclerView recyclerView,
                                       OpenAndCloseAdapter adapter) {
        double expected = summary.getNetCash();
        double recorded = summary.getFinalClosingCash();

        String statusDescription = Math.abs(recorded - expected) <= 1000
                ? "Balanced"
                : "Unbalanced";

        if (apiService == null) {
            apiService = ApiClient.getClient().create(ApiService.class);
        }

        // Step 1: Get existing OpenAndClose record for this date
        apiService.getOpenAndClose().enqueue(new Callback<List<OpenAndClose>>() {
            @Override
            public void onResponse(Call<List<OpenAndClose>> call, Response<List<OpenAndClose>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OpenAndClose existing = null;
                    for (OpenAndClose oc : response.body()) {
                        if (date.equals(oc.getDate())) {
                            existing = oc;
                            break;
                        }
                    }

                    if (existing != null) {
                        // Step 2: Update only description
                        existing.setDescription(statusDescription);

                        // Step 3: Send updated record back
                        apiService.addOrUpdateOpenAndClose(existing).enqueue(new Callback<OpenAndClose>() {
                            @Override
                            public void onResponse(Call<OpenAndClose> call, Response<OpenAndClose> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(context,
                                            "Status updated to " + statusDescription,
                                            Toast.LENGTH_SHORT).show();
                                    // Call fetchOpenAndClose with context
                                    // Create an instance
                                    OpenAndClose_Functions ocFunctions = new OpenAndClose_Functions();
                                    ocFunctions.fetchOpenAndClose(context,
                                            (FragmentActivity) ((Activity) context),
                                            loadingOverlay,
                                            loadingMessage,
                                            recyclerView,
                                            adapter);
                                    // your RecyclerView


                                } else {
                                    Toast.makeText(context,
                                            "Failed to update status",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<OpenAndClose> call, Throwable t) {
                                Toast.makeText(context,
                                        "Error updating status",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // If no record exists, create a new one with just date + description
                        OpenAndClose newRecord = new OpenAndClose();
                        newRecord.setDate(date);
                        newRecord.setDescription(statusDescription);

                        apiService.addOrUpdateOpenAndClose(newRecord).enqueue(new Callback<OpenAndClose>() {
                            @Override
                            public void onResponse(Call<OpenAndClose> call, Response<OpenAndClose> response) {
                                Toast.makeText(context,
                                        "New record created with status " + statusDescription,
                                        Toast.LENGTH_SHORT).show();
                                // Create an instance
                                OpenAndClose_Functions ocFunctions = new OpenAndClose_Functions();
                                ocFunctions.fetchOpenAndClose(context,
                                        (FragmentActivity) ((Activity) context),
                                        loadingOverlay,
                                        loadingMessage,
                                        recyclerView,
                                        adapter);

                            }

                            @Override
                            public void onFailure(Call<OpenAndClose> call, Throwable t) {
                                Toast.makeText(context,
                                        "Error creating record",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<List<OpenAndClose>> call, Throwable t) {
                Toast.makeText(context,
                        "Error fetching OpenAndClose records",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showResultDialog(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
