package com.devlin.chatbot.others;/*
 * @created 27/11/2020 - 17:30
 * @project 622-chatbot
 * @author devlin
 */


import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class MongoDB {
    private static MongoClient mongoClient = null;
    private static MongoDatabase db = null;
    private static MongoCollection<Document> col = null;
    static {
        mongoClient = new MongoClient(
                "localhost", 27017
        );
        db = mongoClient.getDatabase("local_cs622");
        col = db.getCollection("article");
    }
    public void parse(File file) throws ParserConfigurationException, IOException, SAXException {

        col.drop();
        Iterator it = col.find().iterator();
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
                List<String> dates = Arrays.asList(element.getElementsByTagName("PubDate").item(0).getTextContent());
                for (int t = 0; t < titles.size(); t++) {
                    for (int d = 0; d < dates.size(); d++) {
                        Document document1 = new Document();
                        document1.append("title", titles.get(t));
                        document1.append("date", dates.get(d));
                        docs.add(document1);
                    }
                }
            }
        }

        col.insertMany(docs);
    }

    public void countKeyword(String keyword, int year) {
        BasicDBObject countKWQuery = new BasicDBObject();
        countKWQuery.put("title", new BasicDBObject("$regex", keyword).append("$options", "i"));
        countKWQuery.put("date", new BasicDBObject("$regex", String.valueOf(year)).append("$options", "i"));
//        System.out.println(countKWQuery.toString());
        Iterator it = col.find(countKWQuery).iterator();
        int count = 0;
        while (it.hasNext()){
//            System.out.println(it.next());
            it.next();
            count++;
        }
        System.out.println("The total counts in year: " + String.valueOf(year) + " with keyword: " + keyword + " is " + count);
    }

    public void rangeKeyword(String keyword, int startYear, int endYear) {
        BasicDBObject rangeKWQuery = new BasicDBObject();
        rangeKWQuery.put("date", new BasicDBObject("$gt", String.valueOf(startYear)).append("$lte",String.valueOf(endYear)));
        rangeKWQuery.put("title", new BasicDBObject("$regex", keyword).append("$options", "i"));

        Iterator it = col.find(rangeKWQuery).iterator();
        int count = 0;
        while (it.hasNext()) {
            System.out.println(it.next());
            count++;
        }
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println("The total counts between year: " + String.valueOf(startYear) + " ~ " + String.valueOf(endYear) + " with keyword: " + keyword + " is " + count);
    }
}
