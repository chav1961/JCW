package jcw.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

public class Utils {
	public static final String	FONT_TAG = "<font size=\"4\" face=\"Courier New\">"; 
	
	public static void addDoc(final IndexWriter writer, final String name, final String content, final URI location) throws IOException {
		final Document 	doc = new Document();
		
		doc.add(new StringField("name", name, Field.Store.YES));
		doc.add(new TextField("content", content, Field.Store.YES));
		doc.add(new StringField("location", location.toString(), Field.Store.YES));
		writer.addDocument(doc);
	}

	public static void parseContent(final InputStream is, final String name, final URI location, final IndexWriter writer, final Map<String,URI> classes) throws IOException {
		final StringBuilder	sb = new StringBuilder(); 
		
		try(final Reader	rdr = new InputStreamReader(is);
			final BufferedReader	brdr = new BufferedReader(rdr)) {
			int				index;
			String			line;
			
			while ((line = brdr.readLine()) != null) {
				if ((index = line.indexOf("package ")) >= 0) {
					final String	className = line.substring(index+"package ".length()).split("\\;")[0].trim()+"."+(name.replace(".java",""));
					
					classes.put(className,location);
				}
				
				sb.append(line).append('\n');
			}
		}
		if (sb.length() > 0) {
			Utils.addDoc(writer, name, sb.toString(), location);
		}
	}
	
	public static String loadAndEscapeContent(final InputStream is, final Map<String,URI> classes) throws IOException {
		final StringBuilder	sb = new StringBuilder("<html><head></head><body>").append(FONT_TAG); 
		
		try(final Reader	rdr = new InputStreamReader(is);
			final BufferedReader	brdr = new BufferedReader(rdr)) {
			int				index;
			String			line;
			
			while ((line = brdr.readLine()) != null) {
				line = line.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
				if ((index = line.indexOf("import ")) >= 0) {
					final String	className = line.substring(index+"import ".length()).split("\\;")[0].trim();
					final URI		ref = classes.get(className);
						
					if (ref != null) {
						line = line.replace(className, "<a href=\""+ref+"\">"+className+"</a>");
					}
				}
				line = line.replace("\t","        ").replace(" ","&nbsp;");
				sb.append(line).append("<br/>");
			}
		}
		return sb.append("</body></html>").toString();
	}
}
