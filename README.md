# Pubmed Article Chatbot

This is the introduction of my final project, Pubmed articles chatbot. The chatbot uses BotUI as graphical user interface (GUI) and Spring Boot as back-end framework to handle all the search contents from chatbot web requests and make response messages back to chatbot web application. In this chatbot, you can search the numbers of paper with certain medical terms or fields in particular year or year range.

### Technical stacks

BotUI, Spring Boot

### Discription

- The chatbot's GUI is designed by BotUI, a JavaScript framework which makes easy to build conversational UIs.
- The chatbot should be able to handle plain language query and answers. Basically, it supports two types of query, one is query by the term in particular year, another is range query in the scope of years. For example, you can input "search cancer in 2020" or "search covid-19 from 2019 to 2020".
- It contains four approaches to search three different sizes of files
  - **Approaches**: Lucene, Brute Force, MongoDB and MySQL.
  - **Files**: Pubmed20n1333.xml (small, 4475 papers), Pubmed20n1016.xml (medium, 15851 papers) and Pubmed20n1410.xml (large, 30000 papers)
- It is able to display your searching history and track every user's questions and system's response results.
- In order to better report the performance (the execution time) of these approaches, I implemented a visualization button that can provide charts in chatbot GUI. Also these charts can be stored in local folder. 

### Demo video

Please view on YouTube: https://www.youtube.com/watch?v=t8acIVZ1bSw

### Performance result

| **Search Method** | **Execution Time** **(small, 4475 papers)** | **Execution Time** **(medium, 15851 papers)** | **Execution Time** **(large, 30000 papers)** |
| ----------------- | ------------------------------------------- | --------------------------------------------- | -------------------------------------------- |
| **MySQL**         | 54.7 ms                                     | 58.5 ms                                       | 78.2 ms                                      |
| **MongoDB**       | 106.2 ms                                    | 45.3 ms                                       | 160.1 ms                                     |
| **Lucene**        | 180.4 ms                                    | 1080.3 ms                                     | 5113.8 ms                                    |
| **Brute Force**   | 121.7 ms                                    | 621.5 ms                                      | 4972.6 ms                                    |

### Data reference and libraries 

- XML source: https://ftp.ncbi.nlm.nih.gov/pubmed/updatefiles/
- Plot charts: `org.knowm.xchart.*`

### Source code

Please view on GitHub: https://github.com/LLancelot/Pubmed-Articles-Chatbot