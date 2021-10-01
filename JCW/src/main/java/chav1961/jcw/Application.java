package chav1961.jcw;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;

import chav1961.bt.lucenewrapper.LuceneSearchRepository;
import chav1961.bt.lucenewrapper.interfaces.Document2Save;
import chav1961.bt.lucenewrapper.interfaces.SearchRepository.SearchRepositoryTransaction;
import chav1961.bt.lucenewrapper.interfaces.SearchRepositoryException;
import chav1961.bt.lucenewrapper.interfaces.SearchableDocument;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class Application {
	static final String	CAPTION = "??????";
	
	static final String	KEY_CONF = "conf";
	static final String	KEY_INDEX = "index";
	static final String	KEY_NOCROWLING = "nocrowling";
	static final String	KEY_CROWLING = "crowling";
	static final String	KEY_APPEND = "append";
	static final String	KEY_NOSCREEN = "noscreen";
	static final String	KEY_HELP = "help";

	static boolean checkParameters(final ArgParser parser) throws CommandLineParametersException {
		if (parser.getValue(KEY_NOCROWLING, boolean.class) && parser.getValue(KEY_NOSCREEN, boolean.class) && !parser.isTyped(KEY_HELP)) {
			throw new CommandLineParametersException("Neither crowling nor screen in the parameters");
		}
		
		if (parser.isTyped(KEY_INDEX)) {
			final File	file = parser.getValue(KEY_INDEX, File.class);
			
			if (file.exists() && file.isFile()) {
				throw new CommandLineParametersException("["+KEY_INDEX+"] parameter locates to file, not directory");
			}
			else if (!file.exists()) {
				if (!file.mkdirs()) {
					throw new CommandLineParametersException("["+KEY_INDEX+"] error: can't create directory ["+file.getAbsolutePath()+"]");
				}
			}
		}
		
		if (parser.isTyped(KEY_CROWLING) && parser.isTyped(KEY_APPEND)) {
			throw new CommandLineParametersException("Mutually exclusive parameters ["+KEY_CROWLING+"] and ["+KEY_APPEND+"]");
		}
		
		final StringBuilder	sb = new StringBuilder();
		
		for (String item : parser.getValue(KEY_CROWLING, String[].class)) {
			final File	file = new File(item);
			
			if (!file.exists() || !file.canRead()) {
				sb.append(' ').append(file.getAbsolutePath());
			}
		}
		for (String item : parser.getValue(KEY_APPEND, String[].class)) {
			final File	file = new File(item);
			
			if (!file.exists() || !file.canRead()) {
				sb.append(' ').append(file.getAbsolutePath());
			}
		}
		
		if (!sb.isEmpty()) {
			throw new CommandLineParametersException("Some directories/files in the ["+KEY_CROWLING+"]/["+KEY_APPEND+"] list don't exist or not accessible: ["+sb+"]");
		}
		
		if (parser.isTyped(KEY_HELP)) {
			printUsage();
		}
		return true;
	}
	
	static void appendContent(final LuceneSearchRepository lsr, final String[] filesAndDirs, final boolean checkReplacement) throws IOException, SearchRepositoryException {
		final List<Document2Save>		docs = new ArrayList<>();
		final Map<String, NameAndId>	replacements = new HashMap<>();
		
		for (String item : filesAndDirs) {
			createDocument(new File(item), docs);
		}
		if (!docs.isEmpty()) {
			if (checkReplacement) {
				for (Document2Save item : docs) {
					final String	title = item.getTitle();
					
					try{
						for (SearchableDocument found : lsr.seekDocuments(title, 1)) {
							replacements.put(title, new NameAndId(title, found.getId()));
						}
					} catch (SyntaxException e) {
					}
				}
			}
			
			try(SearchRepositoryTransaction	srt = lsr.startTransaction()) {
				for (Document2Save item : docs) {
					if (replacements.containsKey(item.getTitle())) {
						srt.replaceDocument(replacements.get(item.getTitle()).id, item);
					}
					else {
						srt.addDocument(item);
					}
				}
				srt.commit();
			} catch (SyntaxException e) {
			}
		}
	}

	private static void printUsage() {
		System.err.println("Usage: jcw.jar [-conf <config_file>] [-index <lucene_index_dir>] [-nocrowling] [-crowling <URI_list_to_crowl>] [-append <URI_list_to_crowl>] [-noscreen] [-help]");
		System.err.println("\t -conf - get parameters from configuration file");
		System.err.println("\t -index - location of lucene index directory. If missing, in-memory index will be used");
		System.err.println("\t -nocrowling - reject crowling independently to '-conf' definitions");
		System.err.println("\t -crowling - create index and crowling data to it");
		System.err.println("\t -append - append cdata crowledto existent index");
		System.err.println("\t -noscreen - index data only, but not show screen");
	}
	
	private static void createDocument(final File source, final List<Document2Save> docs) throws IOException {
		if (source.exists()) {
			if (source.isFile()) {
				try(final Reader		rdr = new FileReader(source, Charset.forName(PureLibSettings.DEFAULT_CONTENT_ENCODING))) {
					final Document2Save	d2s = parseDocument(rdr); 
					
					if (d2s != null) {
						docs.add(d2s);
					}
				}
			}
			else {
				for (File f : source.listFiles()) {
					createDocument(f, docs);
				}
			}
		}
	}
	
	private static Document2Save parseDocument(final Reader rdr) {
		return null;
	}

	private static void showContent(final LuceneSearchRepository lsr) {
		// TODO Auto-generated method stub
		
	}
	
	private static Directory getLuceneDir(final ArgParser parser) throws CommandLineParametersException, IOException {
		if (parser.isTyped(KEY_INDEX)) {
			return FSDirectory.open(parser.getValue(KEY_INDEX, File.class).toPath());
		}
		else {
			final File	mapDir = File.createTempFile("map", ".tmp");

			Utils.deleteDir(mapDir);
			return new MMapDirectory(mapDir.toPath()); 
		}
	}
	

	
	
	/**
	 * <p>Start application</p>
	 * @param args:
	 * 		[-conf <configuration_file>] - configuration file to get parameters from
	 * 		[-index <lucene_index_dir>] - use lucene index directory for searching purposes. If missing, in-memory index will be used 
	 * 		[-nocrowling] - down't make crowling 
	 * 		[-crowling <crowling_dir_URI;...>] - create new index and crowling the given directory list 
	 * 		[-append <crowling_dir_URI;...>] - append content to existent index 
	 * 		[-noscreen] - crowl data, but don't start screen application 
	 * 		[-help] - get help 
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			printUsage();
			System.exit(128);
		}
		else {
			try{final ArgParser	parsed = new ApplicationArgParser().parse(args);
				
				if (checkParameters(parsed)) {
					try(final Directory 					index = getLuceneDir(parsed)) {
						
						if (parsed.isTyped(KEY_CROWLING)) {
							LuceneSearchRepository.prepareDirectory(index);
						}
						
						try(final LuceneSearchRepository	lsr = new LuceneSearchRepository(PureLibSettings.CURRENT_LOGGER, index)) {
							if (!parsed.getValue(KEY_NOCROWLING, boolean.class)) {
								if (parsed.isTyped(KEY_CROWLING)) {
									appendContent(lsr, parsed.getValue(KEY_CROWLING, String[].class), false);
								}
								if (parsed.isTyped(KEY_APPEND)) {
									appendContent(lsr, parsed.getValue(KEY_APPEND, String[].class), true);
								}
							}
							
							if (!parsed.getValue(KEY_NOSCREEN, boolean.class)) {
								showContent(lsr);
							}
						}
					} catch (IOException | SearchRepositoryException exc) {
						System.err.println(exc.getMessage());
						System.exit(128);
					}
				}
			} catch (CommandLineParametersException exc) {
				System.err.println(exc.getMessage());
				printUsage();
				System.exit(128);
			}
		}
	}

	static class ApplicationArgParser extends ArgParser {
		private static final ArgParser.AbstractArg[]	KEYS = {
															new ConfigArg(KEY_CONF, false, false, "location of the config file"),
															new FileArg(KEY_INDEX, false, false, "location of the lucene index directory"),
															new BooleanArg(KEY_NOCROWLING, false, false, "don't crowl any data"),
															new StringListArg(KEY_CROWLING, false, false, "list of directories for crowling"),
															new StringListArg(KEY_APPEND, false, false, "list of directories to crowl and add to index"),
															new BooleanArg(KEY_NOSCREEN, false, false, "don't show walking screen"),
															new BooleanArg(KEY_HELP, false, false, "get help about application"),
														};
		
		ApplicationArgParser() {
			super(KEYS);
		}
	}
	
	private static class NameAndId {
		private final String	name;
		private final UUID		id;
		
		private NameAndId(final String name, final UUID id)  {
			this.name = name;
			this.id = id;
		}

		@Override
		public String toString() {
			return "NameAndId [name=" + name + ", id=" + id + "]";
		}
	}
}
