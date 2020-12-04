package com.devlin.chatbot.chart;/*
 * @created 02/12/2020 - 7:42 PM
 * @project 622-chatbot
 * @author devlin
 */

import org.knowm.xchart.*;
import org.knowm.xchart.demo.charts.ExampleChart;

import java.io.IOException;

import com.devlin.chatbot.ChatBotSearchUtil.*;

import static com.devlin.chatbot.ChatBotSearchUtil.xCount;
import static com.devlin.chatbot.ChatBotSearchUtil.yBruteForceTimeSeries;
import static com.devlin.chatbot.ChatBotSearchUtil.yLuceneTimeSeries;

import static org.knowm.xchart.BitmapEncoder.saveJPGWithQuality;

public class PlotChart implements ExampleChart<XYChart> {

    public void getAndSaveChart(String fileType, String exportFileName, String searchWord, String searchContent, KEYWORD keyword) throws IOException {
        XYChart chart = null;
        if (keyword == KEYWORD.BRUTE_FORCE || keyword == KEYWORD.LUCENE_SEARCH)
            chart = this.getSingleChart(fileType, searchContent, keyword);
        else if (keyword == KEYWORD.LUCENE_AND_BRUTE_FORCE)
            chart = this.getComboChart(fileType, searchContent);

        saveJPGWithQuality(chart, "src/main/resources/graphs/" +
                exportFileName + "-" +
                fileType + "-" +
                searchWord +
                ".jpg", 1);
    }

    /**
     * Create Chart only for BF search or Lucene Search
     * @param fileType
     * @param keyword
     * @return chart
     */
    public XYChart getSingleChart(String fileType, String searchContent, KEYWORD keyword) {

        XYChart chart = new XYChartBuilder().width(800).height(600).title(getClass().getSimpleName()).xAxisTitle("Paper Count").yAxisTitle("Elapsed Time(ms)").build();
        chart.setTitle("File Size is " + fileType + " and search --> " + searchContent);
        // Add series
        if (keyword == KEYWORD.BRUTE_FORCE) {
            chart.addSeries("BruteForce Search", xCount, yBruteForceTimeSeries);
        } else if (keyword == KEYWORD.LUCENE_SEARCH) {
            chart.addSeries("Lucene Search", xCount, yLuceneTimeSeries);
        }
        return chart;
    }

    /**
     * Create lines combo chart
     * @param fileType
     * @return chart
     */
    public XYChart getComboChart(String fileType, String searchContent) {
        XYChart chart = new XYChartBuilder().width(800).height(600).title(getClass().getSimpleName()).xAxisTitle("Paper Count").yAxisTitle("Elapsed Time(ms)").build();
        chart.setTitle("File Size is " + fileType + " and search --> " + searchContent);
        // Add series
        chart.addSeries("BruteForce Search", xCount, yBruteForceTimeSeries);
        chart.addSeries("Lucene Search", xCount, yLuceneTimeSeries);
        return chart;
    }

    @Override
    public XYChart getChart() {
        return null;
    }

    @Override
    public String getExampleChartName() {
        return getClass().getSimpleName() + " - Combination Area & Line Chart";
    }
}
