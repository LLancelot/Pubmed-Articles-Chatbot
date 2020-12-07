package com.devlin.chatbot.others;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

/*

    Modified the Brute Force method based on HW3

 */
public class BruteForceSearch {
    // set time series for searching cancer and obesity
    private static double startTime = 0;
    private static double cancerSearchTime = 0;
    private static double obesitySearchTime = 0;

    // set article counts
    private static int cancerCount, obesityCount = 0;
    private static ArrayList<Integer> xCancerCount = new ArrayList<>();
    private static ArrayList<Integer> xObesityCount = new ArrayList<>();

    // create list to store elapsed time series
    private static ArrayList<Double> cancerTimeSeries = new ArrayList<>();
    private static ArrayList<Double> obesityTimeSeries= new ArrayList<>();


    public void doSearch() throws Exception {
        // based on HW3
        File[] files = new File("data-xml").listFiles();
        // First, we need to input the keyword
        Scanner s = new Scanner(System.in);
        System.out.println("Please input any word to start processing before searching: ");
        String inputWord = s.nextLine();
        System.out.println("Brute-Force Search is processing, please wait...");
        setCurrentTime();

        for (File file : files) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("PubmedArticle");

            for (int temp = 0; temp < nList.getLength(); temp++){
                // temp is the index of the whole big record set matched by "PubmedArticle".
                Node nNode = nList.item(temp);
                if(nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) nNode;
                    if (element.getElementsByTagName("AbstractText").getLength() == 0) {
                        // ignore some papers without "AbstractText" tag
                        continue;
                    }
                    // for each paper, we search its "AbstractText", then check if it has keyword
                    else if (!inputWord.isEmpty()) {
                        // start searching...
                        // search all abstract text and collect them into separate list.
                        if (element.getElementsByTagName("AbstractText").item(0).getTextContent().toLowerCase().contains("obesity") && obesityCount < 100) {
                            /*
                                if "obesity" is found, increment the count and add response time
                             */
                            obesityCount++;
                            xObesityCount.add(obesityCount);
                            obesitySearchTime = System.currentTimeMillis();
                            obesityTimeSeries.add(obesitySearchTime - startTime);
                        }
                        if (element.getElementsByTagName("AbstractText").item(0).getTextContent().toLowerCase().contains("cancer") && cancerCount < 100) {
                            /*
                                if "cancer" is found, increment the count and add response time
                             */
                            cancerCount++;
                            xCancerCount.add(cancerCount);
                            cancerSearchTime = System.currentTimeMillis();
                            cancerTimeSeries.add(cancerSearchTime - startTime);
                        }
                        if (obesityCount >= 100 && cancerCount >= 100)
                            // end searching
                            return;

                    } else
                        continue;
                }
            }
        }

    }

    private void setCurrentTime() {
        this.startTime = System.currentTimeMillis();
    }

    public void plotCancer() {
        XYChart chart = QuickChart.getChart("Brute Force Searching Result - Cancer",
                "Number of Articles", "Elapsed Time (ms)", "Cancer", xCancerCount, cancerTimeSeries);
        new SwingWrapper(chart).displayChart();
    }

    public void plotObesity() {
        XYChart chart = QuickChart.getChart("Brute Force Searching Result - Obesity",
                "Number of Articles", "Elapsed Time (ms)", "Obesity", xObesityCount, obesityTimeSeries);
        new SwingWrapper(chart).displayChart();
    }
}













