package org.apache.lucene.queries.plugins;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SynonymTermsQueryPlugin extends Plugin implements SearchPlugin {
  @Override
  public List<QuerySpec<?>> getQueries() {
    SynonymTermsQueryParser queryParser = new SynonymTermsQueryParser();
    return Collections.singletonList(new QuerySpec<QueryBuilder>(SynonymTermsQueryBuilder.NAME,
        SeqSpanQueryBuilder::new,
        parseContext -> (Optional<QueryBuilder>) (Optional) queryParser.fromXContent(parseContext)));
  }
}
