package chav1961.jcw.screen;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.Directory;

import chav1961.jcw.lucene.DirectoryIndexer;
import chav1961.jcw.lucene.DirectorySearcher;
import chav1961.jcw.lucene.DirectorySearcher.URIAndScore;
import chav1961.jcw.util.Utils;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = -6792354393497736416L;

	private final Directory			luceneDir;
	private final StandardAnalyzer	analyzer;
	private final JLabel			stateString = new JLabel(" ");
	private final JTextField		queryString = new JTextField(" ");
	private final JTabbedPane		tabs = new JTabbedPane();
	private final ActionListener	listener = new ActionListener(){
											@Override
											public void actionPerformed(final ActionEvent event) {
												processAction(event.getActionCommand());
											}
										};
	private File					currentDir = new File("./");
	private Map<String,URI>			classes = new HashMap<>();
	
	@FunctionalInterface
	private interface LambdaCall {
		void call();
	}
	
	public MainFrame(final String caption, final Directory luceneDir, final StandardAnalyzer analyzer) {
		super(caption);
		this.luceneDir = luceneDir;
		this.analyzer = analyzer;
		
		getContentPane().add(menuBar(
					menu("File"
							,menuItem("Index","index")
							,new JSeparator()
							,menuItem("Exit","exit")
					),
					menu("Help"
							,menuItem("About","about")
					)
				)
				,BorderLayout.NORTH);
		stateString.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		getContentPane().add(stateString,BorderLayout.SOUTH);
		
		final JPanel		inside = new JPanel();
		
		inside.setLayout(new BorderLayout());
		inside.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		inside.add(queryString,BorderLayout.NORTH);
		inside.add(tabs,BorderLayout.CENTER);

		getContentPane().add(inside,BorderLayout.CENTER);
		
		queryString.addActionListener(listener);
		queryString.setActionCommand("query");
		
		final Dimension 	screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		setMinimumSize(new Dimension(200,200));
		setPreferredSize(new Dimension(screenSize.width/2,screenSize.height/2));
		setLocation(new Point(screenSize.width/4,screenSize.height/4));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	public void buildIndex(final File root) {
		currentDir = root;
		classes.clear();
		
		final Thread		t = new Thread(()->new DirectoryIndexer(luceneDir, analyzer)
									.buildDirectoryIndex(currentDir,classes,MainFrame.this::stateMessage));
		t.setDaemon(true);	t.start();
	}
	
	private JMenuItem menuItem(final String caption, final String actionCommand) {
		final JMenuItem		item = new JMenuItem(caption);
		
		item.addActionListener(listener);
		item.setActionCommand(actionCommand);
		return item;
	}

	private JMenuItem menuItem(final String caption, final LambdaCall call) {
		final JMenuItem		item = new JMenuItem(caption);
		
		item.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(final ActionEvent event) {
					call.call();
				}
			}
		);
		return item;
	}
	
	private JMenu menu(final String caption, final JComponent... content) {
		final JMenu			submenu = new JMenu(caption);
		
		for (JComponent item : content) {
			submenu.add(item);
		}
		return submenu;
	}
	
	private JMenuBar menuBar(final JComponent... content) {
		final JMenuBar		menuBar = new JMenuBar();
		
		for (JComponent item : content) {
			menuBar.add(item);
		}
		return menuBar;
	}

	private JPopupMenu popupMenu(final JComponent... content) {
		final JPopupMenu	popupMenu = new JPopupMenu();
		
		for (JComponent item : content) {
			popupMenu.add(item);
		}
		return popupMenu;
	}
	
	private void stateMessage(final String format, final Object... parameters) {
		if (parameters.length == 0) {
			stateString.setText(format);
		}
		else {
			stateString.setText(String.format(format,parameters));
		}
	}
	
	private void processAction(final String action) {
		switch (action) {
			case "index"	:
				indexQuery();
				break;
			case "query"	:
				searchResult(queryString.getText());
				break;
			case "exit"		:
				System.exit(0);
		}
	}

	private void indexQuery() {
		final JFileChooser	chooser = new JFileChooser(currentDir);
		
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Choose directory to index content for");
		if (chooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
			buildIndex(chooser.getSelectedFile());
		}
	}

	private void searchResult(final String query) {
		final DirectorySearcher	searcher = new DirectorySearcher(luceneDir,analyzer);
		final StringBuilder		sb = new StringBuilder("<html><head></head><body><table><col width=10%><col width=90%>");
		int						amount = 0;
		
		for (URIAndScore item : searcher.search(query)) {
			final URI	rel = currentDir.toURI().relativize(item.getURI()).normalize();
			
			sb.append("<tr><td>").append(Utils.FONT_TAG).append(item.getScore())
			  .append("</td><td><a href=\"").append(item.getURI()).append("\">").append(Utils.FONT_TAG)
			  .append(rel).append("</a></td></tr>");
			amount++;
		}
		sb.append("</table></body></html>");
		
		if (amount > 0) {
			final JEditorPane	area = new JEditorPane("text/html",sb.toString());
			final JScrollPane	pane = new JScrollPane(area);
			
			area.setEditable(false);
			area.addHyperlinkListener(new HyperlinkListener() {
					@Override
					public void hyperlinkUpdate(final HyperlinkEvent event) {
						if (event.getEventType() == EventType.ACTIVATED) {
							openHyperlink(event.getURL());
						}
					}
				}
			);

			appendTab("query",query,pane,false);
			stateMessage("%1$d files were found",amount);
		}
		else {
			stateMessage("None found",amount);
		}
	}

	private void openHyperlink(final URL url) {
		try{final File 			f = new File(url.toURI());
			try(final InputStream	is = new FileInputStream(f)) {
				
				final JEditorPane	area = new JEditorPane("text/html",Utils.loadAndEscapeContent(is,classes));
				final JScrollPane	pane = new JScrollPane(area);
				
				area.setEditable(false);
				area.addHyperlinkListener(new HyperlinkListener() {
						@Override
						public void hyperlinkUpdate(final HyperlinkEvent event) {
							if (event.getEventType() == EventType.ACTIVATED) {
								openHyperlink(event.getURL());
							}
						}
					}
				);
				appendTab(f.getName(),f.getAbsolutePath(),pane,true);
			}
		} catch (URISyntaxException | IOException e) {
			stateMessage("Error loading [%1$s]: $2$s",url,e.getMessage());
		}
	}
	
	private void appendTab(final String name, final String tooltip, final JComponent content, final boolean usePopup) {
		final JPanel		panel = new JPanel();
		final JLabel		text = new JLabel(name);
		final JLabel		button = new JLabel(new ImageIcon(this.getClass().getResource(usePopup ? "popup.png" : "close.png")));
		final JPopupMenu	popup = popupMenu(menuItem("close",()->tabs.remove(content)),new JSeparator());
		final int			tabIndex = tabs.getTabCount();
		
		panel.add(text);	panel.add(button);
		text.setToolTipText(tooltip);
		tabs.add(content);	tabs.setTabComponentAt(tabIndex,panel);

		text.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {tabs.setSelectedComponent(content);}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override public void mouseClicked(MouseEvent e) {}
		});

		button.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {if (usePopup) {popup.show(button,8,8);} else {tabs.remove(content);}}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override public void mouseClicked(MouseEvent e) {}
		});
		
		tabs.setSelectedComponent(content);
	}
}
