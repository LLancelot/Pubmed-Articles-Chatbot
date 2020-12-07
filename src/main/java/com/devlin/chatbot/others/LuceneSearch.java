package com.devlin.chatbot.others;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;


/*
    Design Lucene Search - HW4
 */
public class LuceneSearch {
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

    // maximum count
    private int MAX_HIT = 1000;

    // reading the file
    private static File xmlFile = new File("data-xml/pubmed20n1016.xml");

    // add document
    private static void addDocument(IndexWriter w, String keyword, String contents) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("keyword", keyword, Field.Store.YES));
        doc.add(new StringField("content", contents, Field.Store.YES));
        w.addDocument(doc);
    }
    public void doSearch() throws Exception {
        // 0. create index file and store.
        FSDirectory index = FSDirectory.open(Paths.get("data-output").toAbsolutePath());

        // 1. create analyzer
        StandardAnalyzer analyzer = new StandardAnalyzer();

        // 2. index writer configuration, parsing xml data
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter w = new IndexWriter(index, config);
        org.w3c.dom.Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
        NodeList article = doc.getElementsByTagName("PubmedArticle");
        NodeList title = doc.getElementsByTagName("Title");

        // 3. create query string of keyword "cancer" and "obesity"
        String strCancer = "cancer";
        String strObesity = "obesity";

        for (int i = 0; i < article.getLength(); ++i) {
            if (article.item(i).getTextContent().contains(strCancer))
                addDocument(w, strCancer, title.item(i).getTextContent());
            if (article.item(i).getTextContent().contains(strObesity))
                addDocument(w, strObesity, title.item(i).getTextContent());
        }
        w.close();

        // 4. create QueryParser
        Query queryCancer = new QueryParser("keyword", analyzer).parse(strCancer);
        Query queryObesity = new QueryParser("keyword", analyzer).parse(strObesity);

        // 5. apply the index and search
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docsCancer = searcher.search(queryCancer, MAX_HIT);
        TopDocs docsObesity = searcher.search(queryObesity, MAX_HIT);

        ScoreDoc[] cancerHits = docsCancer.scoreDocs;
        ScoreDoc[] obesityHits = docsObesity.scoreDocs;


        System.out.println("\nLucene Search Result:");
        System.out.println("Hits of Cancer: " + cancerHits.length);
        System.out.println("Hits of Obesity: " + obesityHits.length);

        // 6. iterate Hits
        startTime = System.currentTimeMillis();
//        TopScoreDocCollector collector = TopScoreDocCollector.create(1, MAX_HIT);
        for (int i = 0; i < cancerHits.length; i++) {
            ScoreDoc cancerHit = cancerHits[i];
            Document document = searcher.doc(cancerHit.doc);
            cancerCount++;
            searcher.search(queryCancer, MAX_HIT);
            cancerSearchTime = System.currentTimeMillis();
            cancerTimeSeries.add(cancerSearchTime - startTime);
            xCancerCount.add(cancerCount);

            // we only get 100 articles
            if (cancerCount >= 100)
                break;
        }

        for (ScoreDoc obesityHit : obesityHits) {
            Document document = searcher.doc(obesityHit.doc);
            obesityCount++;
            searcher.search(queryObesity, MAX_HIT);
            obesitySearchTime = System.currentTimeMillis();
            obesityTimeSeries.add(obesitySearchTime - startTime);
            xObesityCount.add(obesityCount);
            if (obesityCount >= 100)
                break;
        }
        reader.close();
    }

    // plot Lucene Search result
    public void plotCancer() {
        XYChart chart = QuickChart.getChart("Lucene Searching Result - Cancer",
                "Number of Articles", "Elapsed Time (ms)", "Cancer", xCancerCount, cancerTimeSeries);
        new SwingWrapper(chart).displayChart();
    }

    public void plotObesity() {
        XYChart chart = QuickChart.getChart("Lucene Searching Result - Obesity",
                "Number of Articles", "Elapsed Time (ms)", "Obesity", xObesityCount, obesityTimeSeries);
        new SwingWrapper(chart).displayChart();
    }
}