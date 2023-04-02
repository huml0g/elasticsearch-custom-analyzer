package com.github.huml0g.parser;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.synonym.SolrSynonymParser;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.CharsRefBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;

// Custom SolrSynonymParser
public class CustomSynonymParser extends SynonymMap.Parser {

    public CustomSynonymParser(boolean dedup, Analyzer analyzer) {
        super(dedup, analyzer);
    }

    @Override
    public void parse(Reader in) throws IOException, ParseException {
        LineNumberReader br = new LineNumberReader(in);
        try {
            addInternal(br);
        } catch (IllegalArgumentException e) {
            ParseException ex = new ParseException("Invalid synonym rule at line " + br.getLineNumber(), 0);
            ex.initCause(e);
            throw ex;
        } finally {
            br.close();
        }
    }

    private void addInternal(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            if ((line.length() == 0 || line.charAt(0) == '#') || line.contains("=>")) {
                continue;
            }
            String[] inputStrings = split(line);
            CharsRef[] inputs = new CharsRef[inputStrings.length];
            for (int i = 0; i < inputs.length; i++) {
                inputs[i] = analyze(unescape(inputStrings[i]).trim(), new CharsRefBuilder());
            }
            for (CharsRef input : inputs) {
                add(input, inputs[0], false);
            }
        }
    }

    private static String[] split(String s) {
        ArrayList<String> list = new ArrayList<>(2);
        StringBuilder sb = new StringBuilder();
        int pos = 0, end = s.length();
        while (pos < end) {
            if (s.startsWith(",", pos)) {
                if (sb.length() > 0) {
                    list.add(sb.toString());
                    sb = new StringBuilder();
                }
                pos++;
                continue;
            }
            char ch = s.charAt(pos++);
            if (ch == '\\') {
                sb.append(ch);
                if (pos >= end) break;
                ch = s.charAt(pos++);
            }
            sb.append(ch);
        }
        if (sb.length() > 0) {
            list.add(sb.toString());
        }
        return list.toArray(String[]::new);
    }

    private String unescape(String s) {
        if (s.contains("\\")) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (ch == '\\' && i < s.length() - 1) {
                    sb.append(s.charAt(++i));
                } else {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }
        return s;
    }
}