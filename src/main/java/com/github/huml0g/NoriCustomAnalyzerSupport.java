package com.github.huml0g;

import com.github.huml0g.analyzer.NoriCustomAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class NoriCustomAnalyzerSupport {

    @Value("${es.synonym-path}")
    private String synonymPath;

    @Value("${es.userdict-path}")
    private String userdictPath;

    @Value("${es.stopword-path}")
    private String stopwordPath;

    public NoriCustomAnalyzer create() throws Exception {
        try (FileReader userDictionary = new FileReader(userdictPath);
             FileReader stopwords = new FileReader(stopwordPath);
             FileReader synonym = new FileReader(synonymPath)) {
            return new NoriCustomAnalyzer(userDictionary, synonym, stopwords);
        }
    }

    public List<String> extractKeywords(Analyzer analyzer, String keyword) throws Exception {
        List<String> keywords = new ArrayList<>();
        try (TokenStream ts = analyzer.tokenStream(null, StringUtils.trimAllWhitespace(keyword))) {
            CharTermAttribute charTermAttribute = ts.getAttribute(CharTermAttribute.class);
            ts.reset();
            while (ts.incrementToken()) {
                keywords.add(charTermAttribute.toString());
            }
            ts.end();
        }
        return keywords;
    }
}