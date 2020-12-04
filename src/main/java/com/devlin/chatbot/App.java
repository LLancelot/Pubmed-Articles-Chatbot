package com.devlin.chatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App {

    public App() throws Exception {
        // initialize various settings when start running the springbootapplication
        super();
        try {
//            UtilClass.MySQLInitTable("Small");
//            UtilClass.MySQLInitTable("Medium");
//            UtilClass.MySQLInitTable("Large");
//            UtilClass.MySQLParseXML("Small");
//            UtilClass.MySQLParseXML("Medium");
//            UtilClass.MySQLParseXML("Large");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

}
