package org.apache.lucene.queries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.search.ConjunctionDISI;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TwoPhaseIterator;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.Similarity.SimScorer;

public class SynonymTermsScorer extends Scorer {

	private static class PostingsAndPosition {
		private final PostingsEnum postings;
		private final int offset;
		private int freq, upTo, pos;

		public PostingsAndPosition(PostingsEnum postings, int offset) {
			this.postings = postings;
			this.offset = offset;
		}
	}

	private int freq;

	private float matchCost;

	private final SynonymTermsWeight selfWeight;

	public final Iterator mergedIterator;
	public final SimScorer singleScorer;
	public final List<Scorer> scorers;

	public SynonymTermsScorer(SynonymTermsWeight weight, LeafReaderContext context, List<Scorer> scorers, SimScorer singleScorer) throws IOException {
		super(weight);
		this.selfWeight = weight;

		this.singleScorer = singleScorer;
		this.scorers = scorers;

		//List<PostingsAndPosition> postingsAndPositions = new ArrayList<>();

		final List<DocIdSetIterator> iterators = new ArrayList<>();
		for (Scorer s : scorers) {
			if (s != null) {
				iterators.add(s.iterator());
			}
		}
		mergedIterator = new Iterator(iterators);
	}

	@Override
	public TwoPhaseIterator twoPhaseIterator() {
		return null;
	}

	@Override
	public DocIdSetIterator iterator() {
		return mergedIterator;
	}

	@Override
	public String toString() {
		return "SynonymTermsScorer(" + weight + ")";
	}

	@Override
	public int freq() {
		return freq;
	}

	@Override
	public int docID() {
		return mergedIterator.docID();
	}

	@Override
	public float score() {
		int sumFreq = 0;
		int i = 0;
		for (Scorer s : scorers) {
			int freq;
			if (s == null) {
				System.out.printf("scorer: %d is null\n", i++);
			}
			else {
				try {
					freq = s.freq();
					System.out.printf("scorer: %d, freq: %d\n", i++, freq);
					sumFreq += freq;
				} catch (IOException e) {
					System.err.println("Error getting freq of scorer");
					e.printStackTrace();
				}
			}
		}
		System.out.printf("sumFreq: %d\n", sumFreq);
		final double asymptoticScore  =  1 - (1 / (4 + (double) sumFreq));
		
		final int docID  = docID();
		System.out.printf("Asymptotic score would be: %2.4f\n", asymptoticScore);
		
		// this should apply, the default scorer. So if the AsymptoticSimilarity plugin is installed, it should be
		// Asymptotic similarity score.
		return singleScorer.score(docID, sumFreq); 
	}

	public class Iterator extends DocIdSetIterator {

		private List<DocIdSetIterator> iterators;

		public Iterator(List<DocIdSetIterator> iters) {
			this.iterators = iters;
		}

		@Override
		public int docID() {
			// List<Integer> docIds = new ArrayList<>();
			int minimumDocId = NO_MORE_DOCS;
			for (DocIdSetIterator it : iterators) {
				final int docId = it.docID();
				if (docId < minimumDocId) {
					minimumDocId = docId;
				}
			}
			return minimumDocId;
		}

		@Override
		public int nextDoc() throws IOException {
			int currDocId = docID();
			// increment one or multiple
			for (DocIdSetIterator it : iterators) {
				if (currDocId == it.docID()) {
					it.nextDoc();
				}
			}
			return docID();
		}

		@Override
		public int advance(int target) throws IOException {

			for (DocIdSetIterator it : iterators) {
				it.advance(target);
			}
			return docID();
		}

		@Override
		public long cost() {
			return 1;
		}

	}

}
