package org.apache.lucene.queries;

import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.TermState;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.Similarity.SimScorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SynonymTermsWeight extends Weight {

  private final Similarity similarity;
  private final Similarity.SimWeight stats;
  private final boolean needsScores = true;
  private transient TermContext states[];
  private final Term[] terms;
  private final SynonymTermsQuery selfQuery;
  private final String field;

  public final List<Weight> weights;
  
  protected SynonymTermsWeight(SynonymTermsQuery query, IndexSearcher searcher) throws IOException {
    super(query);
    this.selfQuery = query;
    this.similarity = searcher.getSimilarity(needsScores);
    this.terms = selfQuery.getTerms();
    this.field = terms[0].field();
    
    this.weights = new ArrayList<>();
    int j = 0;
    for (TermQuery subQuery : this.selfQuery.termQueries) {
    	  System.out.printf("SynonymTermsWeight contructor: subQuery %d, representation:%s\n", j++, subQuery.getTerm().toString());
		  weights.add(subQuery.createWeight(searcher, true));
	  }
    
    
    //
    final IndexReaderContext context = searcher.getTopReaderContext();
    states = new TermContext[terms.length];
    TermStatistics termStats[] = new TermStatistics[terms.length];
    for (int i = 0; i < terms.length; i++) {
      final Term term = terms[i];
      states[i] = TermContext.build(context, term);
      termStats[i] = searcher.termStatistics(term, states[i]);
    }
    // last 2 parameters not necessary for asymptoticsimilarity
    stats = similarity.computeWeight(searcher.collectionStatistics(terms[0].field()), termStats);
  }

  @Override
  public void extractTerms(Set<Term> terms) {
    Collections.addAll(terms, this.terms);
  }

  @Override
  public Explanation explain(LeafReaderContext context, int doc) throws IOException {
    return Explanation.noMatch("future");
  }

  @Override
  public float getValueForNormalization() throws IOException {
    return stats.getValueForNormalization();
  }

  @Override
  public void normalize(float norm, float boost) {
    stats.normalize(norm, boost);
  }

  @Override
  public Scorer scorer(LeafReaderContext context) throws IOException {
	  
	  final List<Scorer> scorers = new ArrayList<>();
	  int i = 0;
	  for (Weight w : this.weights) {
		  System.out.printf("Scorer of weight %d: %s\n", i++, w.scorer(context));
		  scorers.add(w.scorer(context));
	  }
	  final SimScorer singleScorer = similarity.simScorer(stats, context);
    return new SynonymTermsScorer(this, context, scorers, singleScorer);
  }

  public Term[] getTerms() {
    return selfQuery.getTerms();
  }

 
}
