package chav1961.jcw.lucene;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;

public class DirectorySearcher {
	private final Directory			luceneDir;
	private final StandardAnalyzer	analyzer;
	
	public interface URIAndScore {
		URI getURI();
		float getScore();
	}
	
	public DirectorySearcher(final Directory luceneDir, final StandardAnalyzer analyzer) {
		this.luceneDir = luceneDir;
		this.analyzer = analyzer;
	}

	public Iterable<URIAndScore> search(final String searchString) {
		return search(searchString,1000);
	}
	
	public Iterable<URIAndScore> search(final String searchString, final int maxResults) {
		try{final Query 			query = new QueryParser("content", analyzer).parse(searchString);
		    final List<URIAndScore>	result = new ArrayList<>(); 
	
		    try(final IndexReader 	reader = DirectoryReader.open(luceneDir)) {
			    final IndexSearcher	searcher = new IndexSearcher(reader);
			    final TopScoreDocCollector 	collector = TopScoreDocCollector.create(maxResults);
		    	
			    searcher.search(query, collector);
			    
			    for (ScoreDoc item : collector.topDocs().scoreDocs) {
			    	result.add(new URIAndScoreImpl(URI.create(searcher.doc(item.doc).get("location")),item.score));
			    }
		    } catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		} catch (ParseException e) {
			return new ArrayList<>();
		}
	}
	
	private static class URIAndScoreImpl implements URIAndScore {
		private final URI	uri;
		private final float	score;
		
		public URIAndScoreImpl(URI uri, float score) {
			this.uri = uri;
			this.score = score;
		}

		@Override public URI getURI() {return uri;}
		@Override public float getScore() {return score;}
		@Override public String toString() {return "URIAndScoreImpl [uri=" + uri + ", score=" + score + "]";}
	}
}
