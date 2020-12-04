package com.devlin.chatbot.controller;

import com.devlin.chatbot.ChatBotSearchUtil;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;

@RestController
public class RequestController {
    /*
        Request controller for brute force search
     */

    @RequestMapping("/getMySQL")
    public String showMySQLResult(
            @RequestParam(value = "filetype", defaultValue = "Medium") String fileType,
            @RequestParam(value = "searchMethod", defaultValue = "MySQL") String searchMethod,
            @RequestParam(value = "searchContent") String searchContent
    ) throws SQLException {
        String queryResult = ChatBotSearchUtil.doQueryMySQL(fileType, searchContent);
        return queryResult;
    }

    @RequestMapping("/getBruteForce")
    public String showBruteForceResult(
            @RequestParam(value = "filetype", defaultValue = "Medium") String fileType,
            @RequestParam(value = "searchMethod", defaultValue = "BruteForce") String searchMethod,
            @RequestParam(value = "searchContent") String searchContent
    ) throws IOException, SAXException, ParserConfigurationException {
        String queryResult = ChatBotSearchUtil.doBruteForceSearch(fileType, searchContent);
        return queryResult;
    }

    @RequestMapping("/getLucene")
    public String showLuceneResult(
            @RequestParam(value = "filetype", defaultValue = "Medium") String fileType,
            @RequestParam(value = "searchMethod", defaultValue = "Lucene") String searchMethod,
            @RequestParam(value = "searchContent") String searchContent
    ) throws ParserConfigurationException, SAXException, IOException, ParseException {
        String queryResult = ChatBotSearchUtil.doLuceneSearch(fileType, searchContent);
        return queryResult;
    }

    @RequestMapping("/getSearchHistory")
    public String showSearchHistory() {
        return ChatBotSearchUtil.printHistory();
    }

    @RequestMapping(value = "/image", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public void getImage(HttpServletRequest request, HttpServletResponse response,
                         @RequestParam(value = "filetype") String fileType,
                         @RequestParam(value = "searchContent") String searchContent)
            throws IOException {
        ChatBotSearchUtil.generateBFAndLuceneComboPlot(fileType, searchContent);
        // check if image generates, then send response to bot
        File imgFile = new File("src/main/resources/graphs/BF-Lucene-Compare-"
                + fileType + "-" + ".jpg");
        if (imgFile.exists()) {
            FileInputStream inputStream = new FileInputStream(imgFile);
            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            StreamUtils.copy(inputStream, response.getOutputStream());
        }
    }
//
//    @RequestMapping("/getMongoDB")
//    public Request showMongoDBResult(
//            @RequestParam(value = "filetype", defaultValue = "Medium") String fileType,
//            @RequestParam(value = "searchMethod", defaultValue = "MongoDB") String searchMethod,
//            @RequestParam(value = "searchContent") String searchContent
//    ) {return new Request("");}


}
