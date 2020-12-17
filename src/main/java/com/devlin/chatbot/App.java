package com.devlin.chatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class App {

    public App() throws Exception {
        // initialize various settings when start running the springbootapplication
        super();
        try {
            /*
                initialize the DB when app starts to run.
            */
//            ChatBotSearchBot.MySQLInitTable("Small");
//            ChatBotSearchBot.MySQLInitTable("Medium");
//            ChatBotSearchBot.MySQLInitTable("Large");
//            ChatBotSearchBot.MySQLParseXML("Small");
//            ChatBotSearchBot.MySQLParseXML("Medium");
//            ChatBotSearchBot.MySQLParseXML("Large");
//            ChatBotSearchUtil.MongoDBParseXML(ChatBotSearchUtil.smallCol, new File("src/main/resources/data-xml/pubmed20n1333.xml"));
//            ChatBotSearchUtil.MongoDBParseXML(ChatBotSearchUtil.mediumCol, new File("src/main/resources/data-xml/pubmed20n1016.xml"));
//            ChatBotSearchUtil.MongoDBParseXML(ChatBotSearchUtil.largeCol, new File("src/main/resources/data-xml/pubmed20n1410.xml"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

}
