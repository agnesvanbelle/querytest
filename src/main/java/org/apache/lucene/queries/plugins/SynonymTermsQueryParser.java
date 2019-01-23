package org.apache.lucene.queries.plugins;

import org.apache.lucene.queries.SeqSpanQuery;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.xcontent.XContentLocation;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.index.query.QueryParser;

import java.io.IOException;
import java.util.Optional;

public class SynonymTermsQueryParser implements QueryParser<SynonymTermsQueryBuilder> {

	@Override
	public Optional<SynonymTermsQueryBuilder> fromXContent(QueryParseContext parseContext) throws IOException {
		XContentParser parser = parseContext.parser();
		SynonymTermsQueryBuilder builder = new SynonymTermsQueryBuilder();

		String currentFieldName = null;
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
				if (SynonymTermsQueryBuilder.TERMS_FIELD.match(currentFieldName)) {
					System.out.println(parser.text());
					builder.setTerms(parser.text());
				} else {
					System.out.println(parser.text());
					builder.setFieldName(parser.text());
				}
			}
		}
		return Optional.of(builder);
	}

//	private static void throwParsingExceptionOnMultipleFields(String queryName, XContentLocation contentLocation, String processedFieldName,
//			String currentFieldName) {
//		if (processedFieldName != null) {
//			throw new ParsingException(contentLocation,
//					"[" + queryName + "] query doesn't support multiple fields, found [" + processedFieldName + "] and [" + currentFieldName + "]");
//		}
//	}
}
