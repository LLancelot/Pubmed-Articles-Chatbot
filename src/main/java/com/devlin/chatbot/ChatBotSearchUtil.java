package com.devlin.chatbot;

/*
 * @created 27/11/2020 - 17:07
 * @project 622-chatbot
 * @author devlin
 */

import com.devlin.chatbot.chart.PlotChart;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.sql.Connection;
import java.util.*;

public class ChatBotSearchUtil {
    public enum KEYWORD {
        MONGODB, MYSQL, LUCENE_SEARCH, BRUTE_FORCE, LUCENE_AND_BRUTE_FORCE
    }


    protected final static String INVALID_YEAR_WARNING = "Your input year is invalid!";

    protected final static String INVALID_QUERY_WARNING = "Invalid query format, please check query rules and retry!";

    protected final static int MAX_HIT = 100000;

    public static HashMap<String, String> fileNameMap, tableNameMap, historyResultMap, BruteForceQA, LuceneQA;

    public static HashMap<String, Double> MongoQA, MySQLQA;

    public static HashMap<String, Integer> MongoCount, MySQLCount;

    public static HashMap<String, MongoCollection<org.bson.Document>> mongoCollectionHashMap;

    public static Set<Map.Entry<String, String>> entrySet;

//    public static String cacheHistResult;

    public static double startTime = 0;

    public static ArrayList<Integer> xCount;

    public static ArrayList<Double> yBruteForceTimeSeries, yLuceneTimeSeries, yMySQLRunTime, yMongoRunTime;

    public static PlotChart plotChart;

    static Connection connection = null;

    static MongoClient mongoClient = null;

    static MongoDatabase mongoDatabase = null;

    static MongoCollection<org.bson.Document> smallCol, mediumCol, largeCol = null;

    static {
        /*
            Initialize global variables: fileNameMap, sqlTableNameMap
         */
        fileNameMap = new HashMap<>();
        fileNameMap.put("Small", "pubmed20n1333.xml");
        fileNameMap.put("Medium", "pubmed20n1016.xml");
        fileNameMap.put("Large", "pubmed20n1410.xml");
        tableNameMap = new HashMap<>();
        tableNameMap.put("Small", "Article1");
        tableNameMap.put("Medium", "Article2");
        tableNameMap.put("Large", "Article3");

        BruteForceQA = new HashMap<>();
        LuceneQA = new HashMap<>();
        MongoQA = new HashMap<>();
        MySQLQA = new HashMap<>();
        MongoCount = new HashMap<>();
        MySQLCount = new HashMap<>();


        /*
            in historyResultMap, we need to store <key, value> as <userInput, queryResult>;
         */
        historyResultMap = new HashMap<>();
        entrySet = historyResultMap.entrySet();

        /*
            Initialize xCount, yTimeSeries for plotting graphs with x-axis and y-axis
         */
        xCount = new ArrayList<>();
        yBruteForceTimeSeries = new ArrayList<>();
        yLuceneTimeSeries = new ArrayList<>();
        yMySQLRunTime = new ArrayList<>();
        yMongoRunTime = new ArrayList<>();
        plotChart = new PlotChart();
    }

    static {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/db1?useTimezone=true&serverTimezone=UTC",
                    "root",
                    "admin123"
            );
        } catch (SQLException ex) {
            System.err.println("Driver not found: " + ex.getMessage());
        }
    }

    static {
        mongoClient = new MongoClient(
                "localhost", 27017
        );
        mongoDatabase = mongoClient.getDatabase("local_cs622");
        smallCol = mongoDatabase.getCollection("Article1");
        mediumCol = mongoDatabase.getCollection("Article2");
        largeCol = mongoDatabase.getCollection("Article3");
        mongoCollectionHashMap = new HashMap<>();
        mongoCollectionHashMap.put("Small", smallCol);
        mongoCollectionHashMap.put("Medium", mediumCol);
        mongoCollectionHashMap.put("Large", largeCol);
    }

    /**
     * @param year
     * @return if the string is a valid year
     */
    public static boolean isValidYear(String year) {
        if (year.length() == 4)
            return true;
        else
            return false;
    }

    private static void setStartTime() {
        startTime = System.currentTimeMillis();
    }

    private static double getRunningTime() {
        return System.currentTimeMillis() - startTime;
    }

    public static void resetXY(KEYWORD searchMethod) {
        xCount.clear();
        if (searchMethod.equals(KEYWORD.BRUTE_FORCE))
            yBruteForceTimeSeries.clear();
        else if (searchMethod.equals(KEYWORD.LUCENE_SEARCH))
            yLuceneTimeSeries.clear();
    }

    public static void resetDBXY(KEYWORD searchMethod) {
        if (searchMethod.equals(KEYWORD.MONGODB))
            yMongoRunTime.clear();
        else if (searchMethod.equals(KEYWORD.MYSQL))
            yMySQLRunTime.clear();
    }

    public static void MongoDBParseXML(MongoCollection collection, File file) throws ParserConfigurationException, IOException, SAXException {
        System.out.println(collection);
        collection.drop();
        ArrayList docs = new ArrayList<Document>();
        // xml parse
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document document = builder.parse(file);
        document.getDocumentElement().normalize();
        NodeList nodeList = document.getElementsByTagName("PubmedArticle");
        // iterate nodeList
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                List<String> titles = Arrays.asList(element.getElementsByTagName("ArticleTitle").item(0).getTextContent());
                List<String> dates = Arrays.asList(element.getElementsByTagName("PubDate").item(0).getTextContent().substring(0,4));
                for (int t = 0; t < titles.size(); t++) {
                    for (int d = 0; d < dates.size(); d++) {
                        org.bson.Document document1 = new org.bson.Document();
                        document1.append("title", titles.get(t));
                        document1.append("date", dates.get(d));
                        docs.add(document1);
                    }
                }
            }
        }
        collection.insertMany(docs);
    }

    /**
     * This method is used in LuceneSearch to add lucene document
     *
     * @param writer
     * @param articleTitle
     * @param keyword
     * @param pubDateStr
     * @throws IOException
     */
    private static void addDocument(IndexWriter writer, String articleTitle, String keyword, String pubDateStr) throws IOException {
        org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
        doc.add(new TextField("articleTitle", articleTitle, Field.Store.YES));
        doc.add(new TextField("keyword", keyword, Field.Store.YES));
        doc.add(new StringField("pubDateStr", pubDateStr, Field.Store.YES));
        writer.addDocument(doc);
    }

    /**
     * Init MySQL by creating tables for storing Pubmed articles
     *
     * @param fileType
     * @throws SQLException
     */
    public static void MySQLInitTable(String fileType) throws SQLException {
        String tableName = tableNameMap.get(fileType);
        connection.createStatement().execute("CREATE TABLE " + tableName + "(\n"
                + "  id integer primary key auto_increment,\n"
                + "  Title varchar(1000) not null,\n"
                + "  Date varchar(25) not null\n"
                + ")");
    }

    /**
     * Parse XML and insert ArticleTitle and PubDate into MySQL
     *
     * @param fileType
     * @throws SQLException
     */
    public static void MySQLParseXML(String fileType) throws SQLException {
        String tableName = tableNameMap.get(fileType);
        String fileName = fileNameMap.get(fileType);
        String curFilePath = "src/main/resources/data-xml/" + fileName;
        PreparedStatement statement = connection.prepareStatement("INSERT INTO " + tableName + "(" + "  Title, date)" + "VALUES(?, ?)");
        try {
            // xml parse
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document document = builder.parse(new File(curFilePath));
            document.getDocumentElement().normalize();
            NodeList nodeList = document.getElementsByTagName("PubmedArticle");
            // iterate node list
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    List<String> cols = Arrays.asList(element.getElementsByTagName("ArticleTitle").item(0).getTextContent(),
                            element.getElementsByTagName("PubDate").item(0).getTextContent());
                    for (int paramIndex = 0; paramIndex < cols.size(); paramIndex++) {
                        statement.setString(paramIndex + 1, cols.get(paramIndex));
                    }
                    statement.execute();
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
        Do Query by MySQL
     */

    /**
     * This method is implemented to do main query work using MySQL
     *
     * @param fileType
     * @param searchContent
     * @return the result of query from user input
     * @throws SQLException
     */
    public static String doQueryMySQL(String fileType, String searchContent) throws SQLException {
        String res = "";
        System.out.println(fileType);
        System.out.println(searchContent);
        // Here it needs to deconstruct the searchContent.
        List<String> words = Arrays.asList(searchContent.toLowerCase().split("\\s+"));
        String searchWord = words.get(words.indexOf("search") + 1);
        String searchYear = "";
        String startYear = "";
        String endYear = "";
        String sqlTableName = ChatBotSearchUtil.tableNameMap.get(fileType);
        String query = "";
        Statement statement = connection.createStatement();
        System.out.println(Arrays.asList(words));
        if (words.contains("in")) {
            // query by year
            setStartTime();
            searchYear = words.get(words.indexOf("in") + 1);
//            cacheHistResult = searchCacheHist(searchWord, searchYear);
//            if (!cacheHistResult.isEmpty())
//                return cacheHistResult;
            if (isValidYear(searchYear)) {
                query = "SELECT COUNT(*) FROM " + sqlTableName + " WHERE Title like '%" + searchWord +
                        "%' AND Date like '" + searchYear + "%'";
                ResultSet resultSet = statement.executeQuery(query);
                String count = "";
                while (resultSet.next())
                    count = resultSet.getString(1);
                double runTime = getRunningTime();
                res = "The total counts in year " + searchYear + " with word: " + searchWord + " is " + count + ". (response time:" + runTime + " ms)";
                MySQLQA.put(fileType, runTime);
                MySQLCount.put(fileType, Integer.parseInt(count));
                historyResultMap.put(searchContent + " (by MySQL)", res);
                return res;
            } else
                return INVALID_YEAR_WARNING;
        } else if (words.contains("from") && words.contains("to")) {
            // query by year range
            startYear = words.get(words.indexOf("from") + 1);
            endYear = words.get(words.indexOf("to") + 1);
            setStartTime();
//            cacheHistResult = searchCacheHist(searchWord, startYear, endYear);
//            if (!cacheHistResult.isEmpty())
//                return cacheHistResult;
            if (isValidYear(startYear) && isValidYear(endYear)) {
                query = "SELECT COUNT(*) FROM " + sqlTableName +
                        " where REGEXP_LIKE(Title, \'" + searchWord + "\') and convert(substring(Date, 1, 4), SIGNED) between " + Integer.parseInt(startYear) + " and " + Integer.parseInt(endYear) + ";";
                System.out.println(query);
                ResultSet resultSet = statement.executeQuery(query);
                String count = "";
                while (resultSet.next())
                    count = resultSet.getString(1);
                double runTime = getRunningTime();
                res = "Articles count from year " + startYear + " to year " + endYear + " with word: " + searchWord + " is " + count + ". (response time:" + runTime + " ms)";
                MySQLQA.put(fileType, runTime);
                MySQLCount.put(fileType, Integer.parseInt(count));
                historyResultMap.put(searchContent+ " (by MySQL)", res);
                return res;
            } else {
                return INVALID_YEAR_WARNING;
            }
        }
        return INVALID_QUERY_WARNING;
    }

    public static String doBruteForceSearch(String fileType, String searchContent) throws ParserConfigurationException, IOException, SAXException {
        resetXY(KEYWORD.BRUTE_FORCE);
        BruteForceQA.clear();
        String res = "";
        List<String> words = Arrays.asList(searchContent.toLowerCase().split("\\s+"));
        String searchWord = words.get(words.indexOf("search") + 1);
        String searchYear, startYear, endYear = "";
        String fileName = fileNameMap.get(fileType);
        File file = new File("src/main/resources/data-xml/" + fileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("PubmedArticle");

        if (words.contains("in")) {
            setStartTime();
            // query by year
            searchYear = words.get(words.indexOf("in") + 1); // which year
//            cacheHistResult = searchCacheHist(searchWord, searchYear);
//            if (!cacheHistResult.isEmpty())
//                return cacheHistResult;
            int paperCount = 0;
            if (isValidYear(searchYear)) {
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    // temp is the index of the whole big record set matched by "PubmedArticle".
                    Node nNode = nList.item(temp);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) nNode;
                        if (element.getElementsByTagName("ArticleTitle").getLength() == 0) {
                            // ignore some papers without "ArticleTitle" tag
                            continue;
                        } else {
                            // start searching...
                            if (element.getElementsByTagName("ArticleTitle").item(0).
                                    getTextContent().
                                    toLowerCase().
                                    contains(searchWord)
                                    &&
                                    element.getElementsByTagName("PubDate").item(0).
                                            getTextContent().
                                            contains(searchYear)
                            ) {
                                paperCount++;
                                xCount.add(paperCount);
                                yBruteForceTimeSeries.add(getRunningTime());
                            }
                        }
                    }
                }
                res = "The total counts in year " + searchYear + " with word: " + searchWord + " is " + paperCount + ". (time:" + getRunningTime() + " ms)";
                BruteForceQA.put(fileType, searchContent);
                historyResultMap.put(searchContent+ " (by Brute Force)", res);
                if (!xCount.isEmpty() && !yBruteForceTimeSeries.isEmpty()) {
                    plotChart.getAndSaveChart(fileType,
                            "BruteForceSearch",
                            searchWord,
                            searchContent,
                            KEYWORD.BRUTE_FORCE);
                }
                return res;
            } else return INVALID_YEAR_WARNING;
        } else if (words.contains("from") && words.contains("to")) {
            // query by year range
            startYear = words.get(words.indexOf("from") + 1);
            endYear = words.get(words.indexOf("to") + 1);
            setStartTime();
            int paperCount = 0;
            if (isValidYear(startYear) && isValidYear(endYear)) {
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) nNode;
                        if (element.getElementsByTagName("ArticleTitle").getLength() == 0) {
                            // ignore some papers without "ArticleTitle" tag
                            continue;
                        } else {
                            //start searching...
                            if (element.getElementsByTagName("ArticleTitle").item(0).
                                    getTextContent().
                                    toLowerCase().
                                    contains(searchWord)) {
                                String xmlYear = element.getElementsByTagName("PubDate").item(0).
                                        getTextContent().substring(0, 4);
                                if (Integer.parseInt(startYear) <= Integer.parseInt(xmlYear) && Integer.parseInt(endYear) >= Integer.parseInt(xmlYear)) {
                                    paperCount++;
                                    xCount.add(paperCount);
                                    yBruteForceTimeSeries.add(getRunningTime());
                                }
                            }
                        }
                    }
                }
                res = "Articles count from year " + startYear + " to year " + endYear + " with word: " + searchWord + " is " + paperCount + ". (time:" + getRunningTime() + " ms)";
                BruteForceQA.put(fileType, searchContent);
                historyResultMap.put(searchContent+ " (by Brute Force)", res);
                if (!xCount.isEmpty() && !yBruteForceTimeSeries.isEmpty()) {
                    plotChart.getAndSaveChart(fileType, "BruteForce-YearRange", searchWord, searchContent, KEYWORD.BRUTE_FORCE);
                }
                return res;
            } else return INVALID_YEAR_WARNING;
        }
        return INVALID_QUERY_WARNING;
    }

    public static String doLuceneSearch(String fileType, String searchContent) throws IOException, ParserConfigurationException, SAXException, ParseException {
        resetXY(KEYWORD.LUCENE_SEARCH);
        LuceneQA.clear();
        String res;
        List<String> words = Arrays.asList(searchContent.toLowerCase().split("\\s+"));
        String searchWord = words.get(words.indexOf("search") + 1);
        String searchYear = "";
        String startYear;
        String endYear;
        String fileName = fileNameMap.get(fileType);
        File file = new File("src/main/resources/data-xml/" + fileName);
        // 0. create index file and store.
        FileUtils.deleteDirectory(new File("src/main/resources/data-output"));
        FSDirectory index = FSDirectory.open(Paths.get("src/main/resources/data-output"));
        // 1. create analyzer
        StandardAnalyzer analyzer = new StandardAnalyzer();
        // 2. index writer configuration, parsing XML file
        IndexWriterConfig config = new IndexWriterConfig();
        IndexWriter writer = new IndexWriter(index, config);
        writer.commit();
        org.w3c.dom.Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        NodeList article = doc.getElementsByTagName("PubmedArticle");
        if (words.contains("in")) {
            // query by year
            searchYear = words.get(words.indexOf("in") + 1); // which year
//            cacheHistResult = searchCacheHist(searchWord, searchYear);
//            if (!cacheHistResult.isEmpty())
//                return cacheHistResult;
            setStartTime();
            int paperCount = 0;
            if (isValidYear(searchYear)) {
                for (int i = 0; i < article.getLength(); i++) {
                    Node nNode = article.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) nNode;
                        if (element.getElementsByTagName("ArticleTitle").getLength() == 0)
                            continue;
                        else {
                            // start searching...
                            if (element.getElementsByTagName("ArticleTitle").item(0).
                                    getTextContent().
                                    toLowerCase().
                                    contains(searchWord)
                                    &&
                                    element.getElementsByTagName("PubDate").item(0).
                                            getTextContent().
                                            contains(searchYear)
                            ) {
                                addDocument(writer,
                                        element.getElementsByTagName("ArticleTitle").item(0).getTextContent().toLowerCase(),
                                        searchWord,
                                        element.getElementsByTagName("PubDate").item(0).getTextContent().toLowerCase());
                                paperCount++;
                                xCount.add(paperCount);
                                yLuceneTimeSeries.add(getRunningTime());
                            }
                        }
                    }
                }
                writer.close();
                // create QueryParser

                Query query = new QueryParser("keyword", analyzer).parse(searchWord);
                // apply the index and search
                IndexReader reader = DirectoryReader.open(index);
                IndexSearcher searcher = new IndexSearcher(reader);
                TopDocs docs = searcher.search(query, MAX_HIT);
                ScoreDoc[] hits = docs.scoreDocs;

                res = "The total counts in year " + searchYear + " with word: " + searchWord + " is " + hits.length + ". (response time:" + getRunningTime() + " ms)";

                LuceneQA.put(fileType, searchContent);
                historyResultMap.put(searchContent+ " (by Lucene)", res);
                if (!xCount.isEmpty() && !yLuceneTimeSeries.isEmpty()) {
                    plotChart.getAndSaveChart(fileType,
                            "LuceneSearch",
                            searchWord,
                            searchContent,
                            KEYWORD.LUCENE_SEARCH);
                }
                return res;
            } else
                return INVALID_YEAR_WARNING;

        } else if (words.contains("from") && words.contains("to")) {
            // query by year range
            startYear = words.get(words.indexOf("from") + 1);
            endYear = words.get(words.indexOf("to") + 1);
//            cacheHistResult = searchCacheHist(searchWord, startYear, endYear);
//            if (!cacheHistResult.isEmpty())
//                return cacheHistResult;
            setStartTime();
            int paperCount = 0;
            if (isValidYear(startYear) && isValidYear(endYear)) {
                for (int i = 0; i < article.getLength(); i++) {
                    Node nNode = article.item(i);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) nNode;
                        if (element.getElementsByTagName("ArticleTitle").getLength() == 0)
                            continue;
                        else {
                            // start searching...
                            if (element.getElementsByTagName("ArticleTitle").item(0).
                                    getTextContent().
                                    toLowerCase().
                                    contains(searchWord)) {
                                String xmlYear = element.getElementsByTagName("PubDate").item(0).
                                        getTextContent().substring(0, 4);
                                if (Integer.parseInt(startYear) <= Integer.parseInt(xmlYear) && Integer.parseInt(endYear) >= Integer.parseInt(xmlYear))
                                {
                                    addDocument(writer,
                                            element.getElementsByTagName("ArticleTitle").item(0).getTextContent().toLowerCase(),
                                            searchWord,
                                            element.getElementsByTagName("PubDate").item(0).getTextContent().toLowerCase());
                                    paperCount++;
                                    xCount.add(paperCount);
                                    yLuceneTimeSeries.add(getRunningTime());
                                }
                            }
                        }
                    }
                }
                writer.close();
                // create QueryParser
                Query query = new QueryParser("keyword", analyzer).parse(searchWord);
                // apply the index and search
                IndexReader reader = DirectoryReader.open(index);
                IndexSearcher searcher = new IndexSearcher(reader);
                TopDocs docs = searcher.search(query, MAX_HIT);
                ScoreDoc[] hits = docs.scoreDocs;


                res = "Articles count from year " + startYear + " to year " + endYear + " with word: " + searchWord + " is " + hits.length + ". (response time:" + getRunningTime() + " ms)";
//                System.out.println(res);
                LuceneQA.put(fileType, searchContent);

                historyResultMap.put(searchContent + " (by Lucene)", res);
                if (!xCount.isEmpty() && !yLuceneTimeSeries.isEmpty()) {
                    plotChart.getAndSaveChart(fileType,
                            "LuceneSearch-YearRange",
                            searchWord,
                            searchContent,
                            KEYWORD.LUCENE_SEARCH);
                }
                return res;

            } else
                return INVALID_YEAR_WARNING;
        }
        return INVALID_QUERY_WARNING;
    }

    public static String doQueryMongo(String fileType, String searchContent) {
        String res = "";
        // Here it needs to deconstruct the searchContent.
        List<String> words = Arrays.asList(searchContent.toLowerCase().split("\\s+"));
        String searchWord = words.get(words.indexOf("search") + 1);
        String searchYear = "";
        String startYear = "";
        String endYear = "";
        MongoCollection<org.bson.Document> collection = mongoCollectionHashMap.get(fileType);

        if (words.contains("in")) {
            // query by year
            setStartTime();
            searchYear = words.get(words.indexOf("in") + 1);
            if (isValidYear(searchYear)) {
                BasicDBObject query = new BasicDBObject();
                query.put("title", new BasicDBObject("$regex", searchWord).append("$options", "i"));
                query.put("date", new BasicDBObject("$regex", searchYear).append("$options", "i"));
                Iterator it = collection.find(query).iterator();
                int paperCount = 0;
                while (it.hasNext()) {
                    it.next();
                    paperCount++;
                }
                double runTime = getRunningTime();
                res = "The total counts in year " + searchYear + " with word: " + searchWord + " is " + paperCount + ". (response time:" + runTime + " ms)";
                MongoQA.put(fileType, runTime);
                MongoCount.put(fileType, paperCount);
                historyResultMap.put(searchContent+ " (by MongoDB)", res);
                return res;
            } else
                return INVALID_YEAR_WARNING;
        }
        else if (words.contains("from") && words.contains("to")) {
            // query by year range
            startYear = words.get(words.indexOf("from") + 1);
            endYear = words.get(words.indexOf("to") + 1);
            setStartTime();
            if (isValidYear(startYear) && isValidYear(endYear)) {
                BasicDBObject rangeQuery = new BasicDBObject();
                rangeQuery.put("date", new BasicDBObject("" +
                        "$gte", startYear).append("$lte", endYear));
                rangeQuery.put("title", new BasicDBObject("$regex", searchWord).append("$options", "i"));
                Iterator it = collection.find(rangeQuery).iterator();
                int paperCount = 0;
                while (it.hasNext()) {
                    it.next();
                    paperCount++;
                }
                double runTime = getRunningTime();
                res = "Articles count from year " + startYear + " to year " + endYear + " with word: " + searchWord + " is " + paperCount + ". (response time:" + runTime + " ms)";
                MongoQA.put(fileType, runTime);
                MongoCount.put(fileType, paperCount);
                historyResultMap.put(searchContent+ " (by MongoDB)", res);
                return res;
            } else {
                return INVALID_YEAR_WARNING;
            }
        }
        return INVALID_QUERY_WARNING;
    }

    public static String printHistory() {
        String res = "";
        for (Map.Entry<String, String> history : entrySet)
            res += "- Asked: " + history.getKey() + "<br>" + "- Answered: " + history.getValue() + "<br><br>";
        return res;
    }

    public static String searchCacheHist(String searchWord, String searchYear) {
        for (Map.Entry<String, String> history : entrySet) {
            if (history.getKey().contains(searchWord) && history.getKey().contains(searchYear))
                return history.getValue();
        }
        return "";
    }

    public static String searchCacheHist(String searchWord, String startYear, String endYear) {
        for (Map.Entry<String, String> history : entrySet) {
            if (history.getKey().contains(searchWord) && history.getKey().contains(startYear) && history.getKey().contains(endYear))
                return history.getValue();
        }
        return "";
    }

    /**
     * @param fileType
     * @param searchContent
     * @throws IOException
     */
    public static void generateBFAndLuceneComboPlot(String fileType, String searchContent) throws IOException {
        if (LuceneQA.size() == 1 && BruteForceQA.size() == 1 &&
            (LuceneQA.get(fileType).equals(BruteForceQA.get(fileType))))
        {
            // Here, we need to make sure the controlled trial of two methods have
            // same search contents and same file type.
            // eg. we need to know the different performances that under "small size" file
            // and search "search cancer in 2019" for 2 methods
            if (!xCount.isEmpty() && !yLuceneTimeSeries.isEmpty() && !yBruteForceTimeSeries.isEmpty()) {
                plotChart.getAndSaveChart(fileType, "BF-Lucene-Compare", "", searchContent, KEYWORD.LUCENE_AND_BRUTE_FORCE);
            }
        }
    }

    public static void generateDBComboPlot() throws IOException {
        if (MongoCount.size() == 3 && MySQLCount.size() == 3) {
            ArrayList<Integer> sizeCount = new ArrayList<>();
            sizeCount.add(MongoCount.get("Small"));
            sizeCount.add(MongoCount.get("Medium"));
            sizeCount.add(MongoCount.get("Large"));
            ArrayList<Double> MongoTime = new ArrayList<>();
            ArrayList<Double> MySQLTime = new ArrayList<>();
            MongoTime.add(MongoQA.get("Small"));
            MongoTime.add(MongoQA.get("Medium"));
            MongoTime.add(MongoQA.get("Large"));
            MySQLTime.add(MySQLQA.get("Small"));
            MySQLTime.add(MySQLQA.get("Medium"));
            MySQLTime.add(MySQLQA.get("Large"));
            plotChart.getAndSaveDBComboChart(sizeCount, MongoTime, MySQLTime);
        }
    }

}
