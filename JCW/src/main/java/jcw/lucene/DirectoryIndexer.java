package jcw.lucene;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;

import jcw.util.Utils;
import jcw.interfaces.IMessagePrinter;

public class DirectoryIndexer {
	private final Directory			luceneDir;
	private final StandardAnalyzer	analyzer;
	private int						dirs = 0, files = 0, indexed = 0, errored = 0;
	
	public DirectoryIndexer(final Directory luceneDir, final StandardAnalyzer analyzer) {
		this.luceneDir = luceneDir;
		this.analyzer = analyzer;
	}

	public int[] buildDirectoryIndex(final File root, final Map<String,URI> classes) {
		return buildDirectoryIndex(root,classes,(format,parameters)->{});
	}

	public int[] buildDirectoryIndex(final File root, final Map<String,URI> classes, final IMessagePrinter printer) {
		final IndexWriterConfig	config = new IndexWriterConfig(analyzer);

		dirs = files = indexed = errored = 0;
	    try(final IndexWriter writer = new IndexWriter(luceneDir,config)) {
			
	    	indexContent(root,writer,classes,printer);
			printer.message("Indexing complete: "+dirs+" dirs and "+files+" fiels were processed, "+indexed+" were indexed successfully, "+errored+" were indexed with errors");
			return new int[]{dirs, files, indexed, errored};
	    } catch (IOException e) {
			printer.message("Error building lucene index: ("+e.getMessage()+")");
			return new int[4];
		}
	}
	
	private void indexContent(final File dir, final IndexWriter writer, final Map<String,URI> classes, final IMessagePrinter printer) {
		if (dir.isFile()) {
			files++;
			if (dir.getName().endsWith(".java")) {
				try(final InputStream	is = new FileInputStream(dir)) {
					
					Utils.parseContent(is, dir.getName(), dir.getAbsoluteFile().toURI(), writer, classes);
					printer.message("File ["+dir.getAbsolutePath()+"] appended to search index");
					indexed++;
				} catch (Exception e) {
					printer.message("File ["+dir.getAbsolutePath()+"] - I/O error on parsing ("+e.getMessage()+")");
					errored++;
				}
			}
		}
		else {
			dirs++;
			for (File item : dir.listFiles()) {
				printer.message("Scanning ["+dir.getAbsolutePath()+"]...");
				indexContent(item, writer, classes, printer);
			}
		}
	}
}
