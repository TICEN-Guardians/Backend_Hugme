package com.project.hugme.infra.elasticsearch.intent.service;

import com.project.hugme.infra.elasticsearch.intent.DocumentQuestionIntent;
import com.project.hugme.infra.elasticsearch.intent.dto.DocumentStructuredQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class DocumentStructuredQueryParserTest {

    @Autowired
    private DocumentStructuredQueryParser parser;

    @Test
    void singleIntentTest() {

        DocumentStructuredQuery result =
                parser.parse("전입세대확인서는 왜 필요한가요?");

        printResult(result);
    }

    @Test
    void multipleIntentTest() {

        DocumentStructuredQuery result =
                parser.parse("전입세대확인서 인터넷으로 발급돼? 비용도 알려줘.");

        printResult(result);
    }

    @Test
    void documentSearchTest() {

        DocumentStructuredQuery result =
                parser.parse("온라인으로 발급 가능한 서류 찾아줘.");

        printResult(result);
    }

    @Test
    void easyExpressionTest() {

        DocumentStructuredQuery result =
                parser.parse("등본 인터넷으로 떼져? 돈도 들어?");

        printResult(result);
    }

    @Test
    void otherIntentTest() {

        DocumentStructuredQuery result =
                parser.parse("이 집 전세사기 위험해?");

        printResult(result);
    }

    private void printResult(DocumentStructuredQuery result) {

        System.out.println("--------------------------------");
        System.out.println("documentName = " + result.documentName());
        System.out.println("intents = " + result.intents());
        System.out.println("normalizedQuestion = " + result.normalizedQuestion());
        System.out.println("--------------------------------");
    }
}