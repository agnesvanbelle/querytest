package org.apache.lucene.search;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.Weight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SynonymTermsQuery extends Query {

	private static Term[] toTerms(String field, String... termStrings) {
		Term[] terms = new Term[termStrings.length];
		for (int i = 0; i < terms.length; ++i) {
			terms[i] = new Term(field, termStrings[i]);
		}
		return terms;
	}

	private final Term[] terms;
	private final List<String> inputtedTerms;
	public final List<TermQuery> termQueries;
	
	public SynonymTermsQuery(String field, String... terms) {

		if (terms.length == 0) {
			throw new IllegalStateException("We need terms");
		}
		this.inputtedTerms = Arrays.asList(terms);
		
		System.out.printf("SynonymTermsQuery constructor: inputtedTerms: %s\n", this.inputtedTerms);
		
		this.terms = toTerms(field, terms);
		
		termQueries = new ArrayList<>();
		for (Term t : this.terms) {
			termQueries.add(new TermQuery(t));
		}
	}

	@Override
	public Weight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
		return new SynonymTermsWeight(this, searcher, boost);
	}

	@Override
	public Query rewrite(IndexReader reader) throws IOException {
		return super.rewrite(reader);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SynonymTermsQuery)) {
			return false;
		}
		SynonymTermsQuery other = (SynonymTermsQuery) obj;
		if (!Objects.equals(inputtedTerms, other.inputtedTerms)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(inputtedTerms);
	}

	@Override
	public String toString(String field) {
		return "SynonymTermsQuery: " + "";
	}


	public Term[] getTerms() {
		return Stream.of(this.terms).toArray(t -> new Term[t]);
	}

}
