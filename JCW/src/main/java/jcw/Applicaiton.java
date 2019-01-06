package jcw;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import jcw.exceptions.CommandLineParametersException;
import jcw.screen.MainFrame;

public class Applicaiton {
	static final String	CAPTION = "??????";
	
	static final String	KEY_INDEX = "index";
	static final String	KEY_CROWLING = "crowling";
	static final String	KEY_REFRESH = "refresh";

	static final String	PARM_CONF = "-conf";
	static final String	PARM_INDEX = '-'+KEY_INDEX;
	static final String	PARM_CROWLING = '-'+KEY_CROWLING;
	static final String	PARM_CROWLING_ADD = '+'+KEY_CROWLING;
	static final String	PARM_NOCROWLING = "-no"+KEY_CROWLING;
	static final String	PARM_REFRESH = '-'+KEY_REFRESH;
	static final String	PARM_NOSCREEN = "-noscreen";
	static final String	PARM_HELP = "-help";
	
	static void printUsage() {
		System.err.println("Usage: jcw.jar [-conf <config_file>] [-index <lucene_index_dir>] [-nocrowling] [{-|+}crowling <URI_list_to_crowl>] [-refresh] [-noscreen] [-help]");
		System.err.println("\t -nocrowling - reject crowling independently to '-conf' definitions");
		System.err.println("\t -refresh - refresh existent index");
		System.err.println("\t -noscreen - index data only, but not show screen");
		System.err.println("Key '-crowling' crowls only location(s) typed, key '+crowling' crowls location(s) typed additionally to '-conf' definitions");
	}
	
	static String checkLuceneDir(final File luceneDir) {
		if (!luceneDir.exists()) {
			if (!luceneDir.mkdirs()) {
				return "Lucene index directory ["+luceneDir.getAbsolutePath()+"] is not exists and can be created!";
			}
			else {
				return null;
			}
		}
		else if (luceneDir.isFile()) {
			return "Lucene index location ["+luceneDir.getAbsolutePath()+"] is a file, not directory!";
		}
		else if (!luceneDir.canWrite() || !luceneDir.canRead()) {
			return "Lucene index directory ["+luceneDir.getAbsolutePath()+"] is not accessible for reading and/or writing!";
		}
		else {
			return null;
		}
	}
	
	static void fillParameters(final InputStream is, final Parameters parm) throws IOException, CommandLineParametersException {
		final Properties	props = new Properties();
		
		props.load(is);
		for (Entry<Object, Object> item : props.entrySet()) {
			switch (item.getKey().toString()) {
				case KEY_INDEX		:
					if (item.getValue() != null) {
						final File		f = new File(item.getValue().toString());
						final String	checkError = checkLuceneDir(f);
						
						if (checkError != null) {
							throw new CommandLineParametersException(checkError);
						}
						else {
							parm.luceneDir = f;
							parm.inMemory = false;
						}
					}
					else {
						throw new CommandLineParametersException("Illegal file name value ["+item.getValue()+"] for ["+KEY_INDEX+"] key in the configuration file");
					}
					break;
				case KEY_CROWLING	:
					if (item.getValue() != null) {
						final String[]	list = item.getValue().toString().split("\\;");
						final List<URI>	uris = new ArrayList<>();	
						final Set<URI>	alreadyTyped = new HashSet<>();
						
						for (int uriIndex = 0; uriIndex < list.length; uriIndex++) {
							try{final URI	newUri = URI.create(list[uriIndex]); 
								
								if (!alreadyTyped.contains(newUri)) {
									uris.add(newUri);
								}
								alreadyTyped.add(newUri);
							} catch (IllegalArgumentException exc) {
								throw new CommandLineParametersException("URI to crowl ["+list[uriIndex]+"] has invalid format!");
							}
						}
						parm.crowlingDir = uris.toArray(new URI[uris.size()]);
					}
					else {
						throw new CommandLineParametersException("Illegal file name value ["+item.getValue()+"] for ["+KEY_CROWLING+"] key in the configuration file");
					}
					break;
				case KEY_REFRESH	:
					try{parm.refresh = Boolean.valueOf(item.getValue() == null ? "false" : item.getValue().toString());
					} catch (IllegalArgumentException exc) {
						throw new CommandLineParametersException("Illegal boolean value ["+item.getValue()+"] for ["+KEY_REFRESH+"] key in the configuration file");
					}
					break;
				default :
					throw new CommandLineParametersException("Unknown key ["+item.getKey()+"] in the configuration file");
			}
			
		}
	}
	
	static Parameters parseParameters(final String[] args) throws CommandLineParametersException {
		final Parameters	parm = new Parameters();
		
		if (args.length == 0) {
			return parm;
		}
		else {
			boolean				conf = false, luceneIndex = false, crowling = false, noCrowling = false, refresh = false, noScreen = false;
			
			for (int index = 0; index < args.length; index++) {
				switch (args[index]) {
					case PARM_CONF			:
						if (!conf) {
							conf = true;
							if (index < args.length - 1) {
								final File	f = new File(args[index+1]);
								
								try(final InputStream	is = new FileInputStream(f)) {
									
									fillParameters(is,parm);
								} catch (FileNotFoundException e) {
									throw new CommandLineParametersException("Configuration file ["+f.getAbsolutePath()+"] not found or is not accessible");
								} catch (IOException e) {
									throw new CommandLineParametersException("Error reading configuration file ["+f.getAbsolutePath()+"]: "+e.getMessage());
								}
							}
							else {
								throw new CommandLineParametersException("File name is missing for '-conf' key!");
							}
						}
						else {
							throw new CommandLineParametersException("Duplicate '-conf' key!");
						}
						break;
					case PARM_INDEX			:
						if (!luceneIndex) {
							luceneIndex = true;
						}
						else {
							throw new CommandLineParametersException("Duplicate '-index' key!");
						}
						break;
					case PARM_CROWLING		:
					case PARM_CROWLING_ADD	:
						if (!crowling) {
							crowling = true;
						}
						else {
							throw new CommandLineParametersException("Duplicate '-crowling'/'+crowling' key!");
						}
						break;
					case PARM_NOCROWLING	:
						if (!noCrowling) {
							noCrowling = true;
						}
						else {
							throw new CommandLineParametersException("Duplicate '-nocrowling' key!");
						}
						break;
					case PARM_REFRESH		:
						if (!refresh) {
							refresh = true;
						}
						else {
							throw new CommandLineParametersException("Duplicate '-refresh' key!");
						}
						break;
					case PARM_NOSCREEN		:
						if (!noScreen) {
							noScreen = true;
						}
						else {
							throw new CommandLineParametersException("Duplicate '-noscreen' key!");
						}
						break;
					default :
						break;
				}
				if (crowling && noCrowling) {
					throw new CommandLineParametersException("Both '-crowling'/'+crowling' and '-nocrowling' keys were typed! The keys are mutually exclusive");
				}
			}
			
			boolean		expandCrowlingList = false;
			
			for (int index = 0; index < args.length; index++) {
				switch (args[index]) {
					case PARM_CONF		:
						index++;	// Skip file name
						break;
					case PARM_INDEX		:
						if (index < args.length - 1) {
							final File		f = new File(args[index+1]);
							final String	checkError = checkLuceneDir(f);
							
							if (checkError != null) {
								throw new CommandLineParametersException(checkError);
							}
							else {
								parm.luceneDir = f;
								parm.inMemory = false;
							}
							index++;
						}
						else {
							throw new CommandLineParametersException("Directory name is missing for '-index' key!");
						}
						break;
					case PARM_CROWLING_ADD	:
						expandCrowlingList = true;
					case PARM_CROWLING	:
						if (index < args.length - 1) {
							final String[]	list = args[index+1].split("\\;");
							final List<URI>	uris = new ArrayList<>();	
							final Set<URI>	alreadyTyped = new HashSet<>();
							
							for (int uriIndex = 0; uriIndex < list.length; uriIndex++) {
								try{final URI	newUri = URI.create(list[uriIndex]); 
									
									if (!alreadyTyped.contains(newUri)) {
										uris.add(newUri);
									}
									alreadyTyped.add(newUri);
								} catch (IllegalArgumentException exc) {
									throw new CommandLineParametersException("URI to crowl ["+list[uriIndex]+"] has invalid format!");
								}
							}
							
							if (expandCrowlingList && parm.crowlingDir != null) {
								for (URI item : parm.crowlingDir) {
									if (!alreadyTyped.contains(item)) {
										uris.add(item);
									}
									alreadyTyped.add(item);
								}
							}
							
							parm.crowlingDir = uris.toArray(new URI[uris.size()]);
							index++;
						}
						else {
							throw new CommandLineParametersException("Crowling URIs list is missing for '-crowling' key!");
						}
						break;
					case PARM_NOCROWLING	:
						parm.crowlingDir = null;
						break;
					case PARM_REFRESH	:
						parm.refresh = true;
						break;
					case PARM_NOSCREEN	:
						parm.screen = false;
						break;
					case PARM_HELP		:
						printUsage();
						break;
					default :
						throw new CommandLineParametersException("Unknown command line parameter ["+args[index]+"]!");
				}
			}
			return parm;
		}
	}
	

	/**
	 * <p>Start application</p>
	 * @param args:
	 * 		[-conf <configuration_file>] - configuration file to get parameters from
	 * 		[-index <lucene_index_dir>] - use lucene index directory for searching purposes. If missing, in-memory index will be used 
	 * 		[-crowling <crowling_dir_URI;...>] - crowling the given directory list 
	 * 		[+crowling <crowling_dir_URI;...>] - crowling the given directory list in additional to configuration list 
	 * 		[-refresh] - refresh existent indexes 
	 * 		[-noscreen] - crowl data, but don't start screen application 
	 * 		[-help] - get help 
	 */
	public static void main(String[] args) throws IOException, ParseException {
		try{final Parameters		parm = parseParameters(args);
			final StandardAnalyzer 	analyzer = new StandardAnalyzer();
			final Directory 		index = parm.inMemory ? new RAMDirectory() : FSDirectory.open(parm.luceneDir.toPath());
			final MainFrame			frame = new MainFrame(CAPTION,index,analyzer);

			if (parm.refresh || parm.inMemory) {
				frame.buildIndex(new File("c:/tmp/zzz"));
			}
		} catch (CommandLineParametersException exc) {
			System.err.println(exc.getMessage());
			printUsage();
			System.exit(128);
		}
	}
	
	public static class Parameters {
		boolean		inMemory = true;
		File		luceneDir = null;
		boolean		crowling = false;
		URI[]		crowlingDir = null;
		boolean		refresh = false;
		boolean		screen = true;
	}
}
