package com.github.huml0g.analyzer;

import com.github.huml0g.parser.CustomSynonymParser;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.ko.KoreanPartOfSpeechStopFilter;
import org.apache.lucene.analysis.ko.KoreanReadingFormFilter;
import org.apache.lucene.analysis.ko.KoreanTokenizer;
import org.apache.lucene.analysis.ko.POS;
import org.apache.lucene.analysis.ko.dict.UserDictionary;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;

import java.io.Reader;
import java.util.Set;

public class NoriCustomAnalyzer extends StopwordAnalyzerBase {

    private final UserDictionary userDict;
    private final SynonymMap synonymMap;
    private final KoreanTokenizer.DecompoundMode mode = KoreanTokenizer.DecompoundMode.DISCARD;
    private final Set<POS.Tag> stopTags = Set.of(
            POS.Tag.SP,
            POS.Tag.SSC,
            POS.Tag.SSO,
            POS.Tag.SC,
            POS.Tag.SE,
            POS.Tag.VSV,
            POS.Tag.SF,
            POS.Tag.SY,
            POS.Tag.UNA,
            POS.Tag.UNKNOWN,
            POS.Tag.NA
    );

    public NoriCustomAnalyzer(Reader userDictionary, Reader synonym, Reader stopwords) throws Exception {
        super(loadStopwordSet(stopwords));
        CustomSynonymParser customSynonymParser = new CustomSynonymParser(true, new SimpleAnalyzer());
        customSynonymParser.parse(synonym);
        this.synonymMap = customSynonymParser.build();
        this.userDict = UserDictionary.open(userDictionary);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer tokenizer = new KoreanTokenizer(TokenStream.DEFAULT_TOKEN_ATTRIBUTE_FACTORY, userDict, mode, false);
        TokenStream stream = new KoreanPartOfSpeechStopFilter(tokenizer, stopTags);
        stream = new KoreanReadingFormFilter(stream);
        stream = new LowerCaseFilter(stream);
        stream = new TrimFilter(stream);
        stream = new StopFilter(stream, stopwords);
        stream = new SynonymGraphFilter(stream, synonymMap, false);
        return new TokenStreamComponents(tokenizer, stream);
    }
}