package org.apache.lucene.search.plugins;

import java.util.Collections;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;

public class SynonymTermsQueryPlugin extends Plugin implements SearchPlugin {
  @Override
  public List<QuerySpec<?>> getQueries() {
    SynonymTermsQueryParser queryParser = new SynonymTermsQueryParser();
    return Collections.singletonList(new QuerySpec<QueryBuilder>(SynonymTermsQueryBuilder.NAME,
        SeqSpanQueryBuilder::new,
        parseContext -> queryParser.fromXContent(parseContext)));
  }
}
