package org.apache.lucene.search.plugins;

import org.apache.lucene.search.SeqSpanQuery;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.xcontent.XContentLocation;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.QueryParser;

import java.io.IOException;
import java.util.Optional;

public class SeqSpanQueryParser implements QueryParser<SeqSpanQueryBuilder>{


  @Override
  public SeqSpanQueryBuilder fromXContent(XContentParser parser) throws IOException {
    SeqSpanQueryBuilder builder = new SeqSpanQueryBuilder();

    String currentFieldName = null;
    String fieldName = null;
    XContentParser.Token token = null;
    while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
      if (token == XContentParser.Token.START_OBJECT) {
        // skip
        System.out.println("start object " + token.isValue());
      } else if (token == XContentParser.Token.FIELD_NAME) {
        currentFieldName = parser.currentName();
        System.out.println(currentFieldName + " " + token.isValue());
      } else if (token.isValue()) {
        System.out.println("=== " + currentFieldName);
        if (SeqSpanQueryBuilder.START_TERM_FIELD.match(currentFieldName)) {
          System.out.println(parser.text());
          builder.setStartTerm(parser.text());
        } else if (SeqSpanQueryBuilder.END_TERM_FIELD.match(currentFieldName)) {
          System.out.println(parser.text());
          builder.setEndTerm(parser.text());
        } else if (SeqSpanQueryBuilder.SEQ_TERM_FIELD.match(currentFieldName)) {
          System.out.println(parser.text());
          builder.setSeqTerm(parser.text());
        } else if (SeqSpanQueryBuilder.MAX_SPAN_FIELD.match(currentFieldName)) {
          System.out.println(parser.intValue());
          builder.setMaxSpan(parser.intValue());
        } else {
          System.out.println(parser.text());
          builder.setFieldName(parser.text());
        }
      }
    }
    return builder;
  }

  private static void throwParsingExceptionOnMultipleFields(String queryName, XContentLocation contentLocation,
                                                              String processedFieldName, String currentFieldName) {
    if (processedFieldName != null) {
      throw new ParsingException(contentLocation, "[" + queryName + "] query doesn't support multiple fields, found ["
          + processedFieldName + "] and [" + currentFieldName + "]");
    }
  }

}
