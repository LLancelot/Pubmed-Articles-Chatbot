(function() {

        var url = 'http://localhost:8080/request?name=';
        var msgIndex, key;
        var botui = new BotUI('final-project');
        var fileName;
        var methodName;
        var searchWord;
        var fileType;
        // Initial message
        botui.message.bot({
            size: 100,
            content: 'This is my final project chatbot...'
        }).then(init);

        function init() {
            botui.message.bot({
                content: 'Hi! Please select one of the Pubmed file below:'
            }).then(fileSelectFunc).then(function(res) {
                fileName = res.value
                fileType = res.text
                return botui.message.bot({
                    content: res.value + ' selected.'
                })
            }).then(function(res) {
                return botui.message.bot({
                    content: "Please select your searching approach:"
                })
            }).then(methodSelectFunc).then(function(res) {
                methodName = res.value
                return botui.message.bot({
                    content: res.value + ' selected.'
                })
            }).then(function(res) {
                return botui.message.bot({
                    content: 'What do you want to search? (Begin with \'search\')' + '<br /><br />' +
                        'Please follow the searching rules below:<br />' +
                        '- Query by particular year:<br /> Ex. search cancer in 2019<br />' +
                        '- Query by year range:<br /> Ex. search cancer from 2018 to 2020<br />'
                })
            })
                .then(function(res) {
                return botui.action.text({
                    action: {
                        size: 50,
                        placeholder: 'Ex. search cancer in 2019'
                    }
                });
            })
                .then(function(res) {
                searchContent = res.value.toLowerCase();
                if (searchContent.search("search") == -1) {
                    botui.message.bot({
                        content: 'You must type \'search\' in the beginning of your query. Please try again.'
                    }).then(init);
                }
                else {
                    sendFileMethodRequest(methodName);
                    botui.message.bot({
                        loading: true
                    })
                        .then(function (index) {
                            msgIndex = index;
                        }).then(function () {
                        return botui.message.bot({
                            delay: 3000,
                            content: 'Retry?'
                        })
                    }).then(function () {

                        return botui.action.button({
                            action: [{
                                icon: 'circle-thin',
                                text: 'Yes, continue',
                                value: "yes"
                            },
                                {
                                icon: "check",
                                text: "View search history",
                                value: "view"
                            },
                                {
                                icon: 'close',
                                text: 'No, show me plots',
                                value: "no"
                            }]
                        });
                    }).then(function (res) {
                        if (res.value === "yes") {
                            init();
                        } else if (res.value === "view") {
                            getSearchHistory();
                        } else if (res.value === "no") {
                            plotChart();
                        } else {
                            end();
                        }
                    })
                }
            });
        }

        // Get Search record history
        function getSearchHistory() {
            var xhr = new XMLHttpRequest();
            xhr.open("GET", 'http://localhost:8080/getSearchHistory');
            xhr.onload = function () {
                var res_str = JSON.stringify(xhr.responseText);
                var result = JSON.parse(res_str);
                displayHistory(result);
            }
            xhr.send();
        }

        // Get current {method, content}, send different request
        function sendFileMethodRequest(methodName){
            if (methodName === "MongoDB") {
                getMongoDBResult();
            } else if (methodName === "MySQL") {
                getMySQLResult();
            } else if (methodName === "BruteForce") {
                getBruteForceResult();
            } else if (methodName === "Lucene") {
                getLuceneResult();
            }
        }

        // Get Brute Force result
        function getBruteForceResult() {
            var xhr = new XMLHttpRequest();
            xhr.open('GET', 'http://localhost:8080/getBruteForce?filetype=' + fileType + '&searchMethod=' + methodName + '&searchContent=' + searchContent);
            xhr.onload = function() {
                var res_str = JSON.stringify(xhr.responseText);
                var result = JSON.parse(res_str);
                displayMessage(result);
            }
            xhr.send();
        }

        // Get Lucene result
        function getLuceneResult() {
            var xhr = new XMLHttpRequest();
            xhr.open('GET', 'http://localhost:8080/getLucene?filetype=' + fileType + '&searchMethod=' + methodName + '&searchContent=' + searchContent);
            xhr.onload = function() {
                var res_str = JSON.stringify(xhr.responseText);
                var result = JSON.parse(res_str);
                displayMessage(result);
            }
            xhr.send();
        }

        // Get MongoDB result
        function getMongoDBResult() {
            var xhr = new XMLHttpRequest();
            xhr.open('GET', 'http://localhost:8080/getMongoDB?filetype=' + fileType + '&searchMethod=' + methodName + '&searchContent=' + searchContent);
            xhr.onload = function() {
                var res_str = JSON.stringify(xhr.responseText);
                var result = JSON.parse(res_str);
                displayMessage(result);
            }
            xhr.send();
        }

        // Get MySQL result
        function getMySQLResult() {
            var xhr = new XMLHttpRequest();
            xhr.open('GET', 'http://localhost:8080/getMySQL?filetype=' + fileType + '&searchMethod=' + methodName + '&searchContent=' + searchContent);
            xhr.onload = function() {
                var res_str = JSON.stringify(xhr.responseText);
                var result = JSON.parse(res_str);
                displayMessage(result);
            }
            xhr.send();
        }



        var fileSelectFunc = function() {
            return botui.action.button({
                addMessage: true,
                action: [{
                    text: 'Small',
                    value: 'pubmed20n1333'
                }, {
                    text: 'Medium',
                    value: 'pubmed20n1016'
                }, {
                    text: 'Large',
                    value: 'pubmed20n1410'
                }]
            })
        }

        var methodSelectFunc = function () {
            return botui.action.button({
                addMessage: true,
                // so we could the address in message instead if 'Existing Address'
                action: [
                //     {
                //     text: 'MongoDB',
                //     value: 'MongoDB'
                // },
                    {
                    text: 'MySQL',
                    value: 'MySQL'
                }, {
                    text: 'Lucene',
                    value: 'Lucene'
                }, {
                    text: 'BruteForce',
                    value: 'BruteForce'
                }]
            })
        }

        function displayMessage(result) {
            // Update the message by using the index of loading icon
            botui.message.update(msgIndex, {
                content: 'Result for ' + methodName + ': <br><br>' + result
            })
        }

        function displayHistory(result) {
            botui.message.bot({
                content: 'View your history' + ': <br><br>' + result
            })
            init();
        }

        function plotChart() {
            botui.message.bot({
                content: "Here are performance charts."
            }).then(function(res) {
                return botui.message.bot({
                    delay: 1000,
                    content: '<img src="image?filetype=' + fileType + '&searchContent=' + searchContent + '">'
                }).then(init)
            })
        }

        function showCharts() {
            // botui.message.bot({
            //     content: 'Ok, I will show you performance charts.'
            // }).then(function(res) {
            //     return botui.message.bot({
            //         delay: 1000,
            //         content: '<img src=image?type=SA/LightSensor>'
            //     })
            // }).then(function(res) {
            //     return botui.message.bot({
            //         delay: 1000,
            //         content: '<img src=image?type=ActivFit>'
            //     })
            // }).then(function(res) {
            //     botui.message.bot({
            //         delay: 1000,
            //         content: '<img src=image?type=HeartRate>'
            //     });
            //     end();
            // });
            console.log("xxx");
        }

        function end() {
            botui.message.bot({
                content: "Thanks! Goodbye!"
            })
        }

    }
)();