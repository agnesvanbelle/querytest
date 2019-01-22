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

public class SynonymTermsScorer extends Scorer{


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
  public final List<DocIdSetIterator> iterators;

  public SynonymTermsScorer(SynonymTermsWeight weight, LeafReaderContext context, List<Scorer> scorers) throws IOException {
    super(weight);
    this.selfWeight = weight;
    
    
    List<PostingsAndPosition> postingsAndPositions = new ArrayList<>();
    
    iterators = new ArrayList<>();
    for (Scorer s : scorers) {
    	iterators.add(s.iterator());
    }
  }

  @Override
  public TwoPhaseIterator twoPhaseIterator() {
    return null;
  }

  @Override
  public DocIdSetIterator iterator() {
    return new Iterator(this.iterators);
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
    return conjunction.docID();
  }

  @Override
  public float score() {
    return docScorer.score(docID(), freq);
  }
  
  public class Iterator extends DocIdSetIterator {

	  private List<DocIdSetIterator> iterators;

	  public Iterator(List<DocIdSetIterator> iters ) {
	        this.iterators = iters;
      }

      @Override
      public int docID() {
    	  //List<Integer> docIds = new ArrayList<>();
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
