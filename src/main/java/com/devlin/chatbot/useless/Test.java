package com.devlin.chatbot.useless;/*
 * @created 27/11/2020 - 21:25
 * @project 622-chatbot
 * @author devlin
 */

import java.util.*;

public class Test {
    public static void main(String[] args) {
//        String str = "search%20cancer%20in%202018";
//        String[] splited = str.toLowerCase().split("%20");
//        List<String> stringArrayList = Arrays.asList(splited);
//        System.out.println(stringArrayList);
//        System.out.println(stringArrayList.contains("in"));
//        String str1 = "2020Apr";
//        System.out.println(str1.contains("2021"));
        HashMap<String, String> map = new HashMap<>();
        Set<Map.Entry<String, String>> ms = map.entrySet();

        map.put("1", "one");
        map.put("2", "two");
        for (Map.Entry<String, String> entry : ms)
            System.out.println(entry.getKey() + " ---> " + entry.getValue());

    }
}
