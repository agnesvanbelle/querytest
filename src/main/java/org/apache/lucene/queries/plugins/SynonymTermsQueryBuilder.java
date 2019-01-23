package org.apache.lucene.queries.plugins;

import org.apache.lucene.queries.SeqSpanQuery;
import org.apache.lucene.queries.SynonymTermsQuery;
import org.apache.lucene.search.Query;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.QueryShardContext;

import java.io.IOException;
import java.util.Objects;

public class SynonymTermsQueryBuilder extends AbstractQueryBuilder<SynonymTermsQueryBuilder> {

  protected static final String NAME = "synonym_terms";

  protected static final ParseField TERMS_FIELD = new ParseField("terms");

  private String fieldName;
  private String terms;

  public SynonymTermsQueryBuilder() {
  }

  public SynonymTermsQueryBuilder(StreamInput in) throws IOException {

    fieldName = in.readString();
    terms = in.readString();
  }

  @Override
  protected void doWriteTo(StreamOutput out) throws IOException {

    out.writeString(fieldName);
    out.writeString(terms);
  }

  @Override
  protected void doXContent(XContentBuilder builder, Params params) throws IOException {

    builder.startObject(NAME);
    builder.startObject(fieldName);
    builder.field(TERMS_FIELD.getPreferredName(), terms);
    builder.endObject();
    builder.endObject();
  }

  @Override
  protected Query doToQuery(QueryShardContext context) throws IOException {
    return new SynonymTermsQuery(fieldName, terms.split(" "));
  }

  @Override
  protected boolean doEquals(SynonymTermsQueryBuilder other) {
    if (!Objects.equals(fieldName, other.fieldName)) {
      return false;
    }
    if (!Objects.equals(terms, other.terms)) {
      return false;
    }
    return true;
  }

  @Override
  protected int doHashCode() {
    return Objects.hash(fieldName, terms);
  }

  @Override
  public String getWriteableName() {
    return NAME;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public void setTerms(String terms) {
    this.terms = terms;
  }
}
