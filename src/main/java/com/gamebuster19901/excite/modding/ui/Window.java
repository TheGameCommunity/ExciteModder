package com.gamebuster19901.excite.modding.ui;

import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;

import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.concurrent.Callable;

import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.tuple.Pair;

import com.gamebuster19901.excite.modding.concurrent.Batch;
import com.gamebuster19901.excite.modding.concurrent.BatchListener;
import com.gamebuster19901.excite.modding.concurrent.BatchRunner;
import com.gamebuster19901.excite.modding.concurrent.Batcher;
import com.gamebuster19901.excite.modding.ui.EJTabbedPane.Tab;
import com.gamebuster19901.excite.modding.unarchiver.Unarchiver;
import com.gamebuster19901.excite.modding.unarchiver.concurrent.DecisionType;
import com.gamebuster19901.excite.modding.util.FileUtils;
import com.gamebuster19901.excite.modding.util.SplitOutputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

public class Window implements BatchListener {
	
	private static final String CONSOLE = "Console Output";
	private static final String STATUS = "Status";
	private static final String PROGRESS = "Progress";

	private JFrame frame;
	private JTextField textFieldSource;
	private JTextField textFieldDest;
	private JSlider threadSlider;
	
	private volatile Unarchiver unarchiver;
	private volatile BatchRunner<Pair<DecisionType, Callable<Void>>> copyOperations;
	private volatile BatchRunner<Void> processOperations;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		Window window;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Window window = new Window();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Window() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * @throws IOException 
	 */
	private void initialize() {
		unarchiver = genUnarchiver(null, null);
		
		threadSlider = new JSlider();
		threadSlider.setSnapToTicks(true);
		threadSlider.setMinimum(1);
		threadSlider.setMaximum(Runtime.getRuntime().availableProcessors());
		threadSlider.setPreferredSize(new Dimension(77, 16));
		threadSlider.setBorder(null);
		
		copyOperations = genCopyBatches();
		processOperations = genProcessBatches(null);
		
		frame = new JFrame();
		frame.setBounds(100, 100, 1000, 680);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{5, 0, 0, 0, 0, 0, 5, 0};
		gridBagLayout.rowHeights = new int[]{5, 0, 0, 5, 0, 0, 16, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		frame.getContentPane().setLayout(gridBagLayout);
		
		JLabel lblSourceDirectory = new JLabel("Source Directory");
		GridBagConstraints gbc_lblSourceDirectory = new GridBagConstraints();
		gbc_lblSourceDirectory.gridwidth = 2;
		gbc_lblSourceDirectory.fill = GridBagConstraints.VERTICAL;
		gbc_lblSourceDirectory.anchor = GridBagConstraints.EAST;
		gbc_lblSourceDirectory.insets = new Insets(0, 0, 5, 5);
		gbc_lblSourceDirectory.gridx = 1;
		gbc_lblSourceDirectory.gridy = 1;
		frame.getContentPane().add(lblSourceDirectory, gbc_lblSourceDirectory);
		
		textFieldSource = new JTextField();
		GridBagConstraints gbc_textFieldSource = new GridBagConstraints();
		gbc_textFieldSource.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldSource.fill = GridBagConstraints.BOTH;
		gbc_textFieldSource.gridx = 3;
		gbc_textFieldSource.gridy = 1;
		frame.getContentPane().add(textFieldSource, gbc_textFieldSource);
		textFieldSource.setColumns(10);
		
		JButton btnChangeSource = new JButton("Change");
		GridBagConstraints gbc_btnChangeSource = new GridBagConstraints();
		gbc_btnChangeSource.fill = GridBagConstraints.VERTICAL;
		gbc_btnChangeSource.insets = new Insets(0, 0, 5, 5);
		gbc_btnChangeSource.gridx = 4;
		gbc_btnChangeSource.gridy = 1;
		frame.getContentPane().add(btnChangeSource, gbc_btnChangeSource);
		
		JButton btnExtract = new JButton("Extract!");
		GridBagConstraints gbc_btnExtract = new GridBagConstraints();
		gbc_btnExtract.insets = new Insets(0, 0, 5, 5);
		gbc_btnExtract.fill = GridBagConstraints.VERTICAL;
		gbc_btnExtract.gridheight = 2;
		gbc_btnExtract.gridx = 5;
		gbc_btnExtract.gridy = 1;
		frame.getContentPane().add(btnExtract, gbc_btnExtract);
		
		JLabel lblDestinationDirectory = new JLabel("Destination Directory");
		GridBagConstraints gbc_lblDestinationDirectory = new GridBagConstraints();
		gbc_lblDestinationDirectory.gridwidth = 2;
		gbc_lblDestinationDirectory.insets = new Insets(0, 0, 5, 5);
		gbc_lblDestinationDirectory.anchor = GridBagConstraints.EAST;
		gbc_lblDestinationDirectory.gridx = 1;
		gbc_lblDestinationDirectory.gridy = 2;
		frame.getContentPane().add(lblDestinationDirectory, gbc_lblDestinationDirectory);
		
		textFieldDest = new JTextField();
		GridBagConstraints gbc_textFieldDest = new GridBagConstraints();
		gbc_textFieldDest.insets = new Insets(0, 0, 5, 5);
		gbc_textFieldDest.fill = GridBagConstraints.BOTH;
		gbc_textFieldDest.gridx = 3;
		gbc_textFieldDest.gridy = 2;
		frame.getContentPane().add(textFieldDest, gbc_textFieldDest);
		textFieldDest.setColumns(10);
		
		JButton btnChangeDest = new JButton("Change");
		GridBagConstraints gbc_btnChangeDest = new GridBagConstraints();
		gbc_btnChangeDest.insets = new Insets(0, 0, 5, 5);
		gbc_btnChangeDest.gridx = 4;
		gbc_btnChangeDest.gridy = 2;
		frame.getContentPane().add(btnChangeDest, gbc_btnChangeDest);
		
		JSeparator separator = new JSeparator();
		separator.setPreferredSize(new Dimension(1,1));
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.anchor = GridBagConstraints.SOUTH;
		gbc_separator.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator.gridwidth = 5;
		gbc_separator.insets = new Insets(0, 0, 5, 5);
		gbc_separator.gridx = 1;
		gbc_separator.gridy = 3;
		frame.getContentPane().add(separator, gbc_separator);
		
		JLabel lblThreads = new JLabel("Threads: ");
		GridBagConstraints gbc_lblThreads = new GridBagConstraints();
		gbc_lblThreads.anchor = GridBagConstraints.WEST;
		gbc_lblThreads.insets = new Insets(0, 0, 5, 5);
		gbc_lblThreads.gridx = 1;
		gbc_lblThreads.gridy = 4;
		frame.getContentPane().add(lblThreads, gbc_lblThreads);
		

		GridBagConstraints gbc_slider = new GridBagConstraints();
		gbc_slider.fill = GridBagConstraints.HORIZONTAL;
		gbc_slider.insets = new Insets(0, 0, 5, 5);
		gbc_slider.gridx = 1;
		gbc_slider.gridy = 5;
		frame.getContentPane().add(threadSlider, gbc_slider);
		
		JProgressBar progressBar = new CustomProgressBar();
		progressBar.setStringPainted(true);
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.gridwidth = 5;
		gbc_progressBar.insets = new Insets(0, 0, 5, 5);
		gbc_progressBar.gridx = 1;
		gbc_progressBar.gridy = 6;
		frame.getContentPane().add(progressBar, gbc_progressBar);
		
		EJTabbedPane tabbedPane = new EJTabbedPane(JTabbedPane.TOP);
		setupTabbedPane(tabbedPane);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.gridwidth = 5;
		gbc_tabbedPane.insets = new Insets(0, 0, 0, 5);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 1;
		gbc_tabbedPane.gridy = 7;
		frame.getContentPane().add(tabbedPane, gbc_tabbedPane);
		
		btnChangeSource.addActionListener((e) -> {
			File f;
			Path path = Path.of(textFieldSource.getText().trim()).toAbsolutePath();
			if(Files.exists(path)) {
				f = path.toAbsolutePath().toFile();
			}
			else {
				f = null;
			}
			
			JFileChooser chooser = new JFileChooser(f);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int status = chooser.showOpenDialog(frame);
			if(status == JFileChooser.APPROVE_OPTION) {
				try {
					selectDirectories(tabbedPane, chooser.getSelectedFile(), new File(textFieldDest.getText()));
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		btnChangeDest.addActionListener((e) -> {
			File f;
			Path path = Path.of(textFieldDest.getText().trim()).toAbsolutePath();
			if(Files.exists(path)) {
				f = path.toAbsolutePath().toFile();
			}
			else {
				f = null;
			}
			
			JFileChooser chooser = new JFileChooser(f);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int status = chooser.showOpenDialog(frame);
			if(status == JFileChooser.APPROVE_OPTION) {
				try {
					selectDirectories(tabbedPane, new File(textFieldSource.getText()), chooser.getSelectedFile());
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		btnExtract.addActionListener((e) -> {
			try {
				File folder = new File(textFieldDest.getText());
				if(folder.exists()) {
					LinkedHashSet<Path> paths = FileUtils.getFilesRecursively(folder.toPath());
					if(!folder.isDirectory()) {
						throw new NotDirectoryException(folder.getAbsolutePath().toString());
					}
					if(paths.size() != 0) {
						int result = JOptionPane.showOptionDialog(frame, "Are you sure? This directory has " + paths.size() + " pre-existing files.\n\nDuplicates will be overridden", "Overwrite?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
						if(result != 0) {
							return;
						}
						
					}
					new Thread(() -> {
						try {
							copyOperations.startBatch();
						} catch (InterruptedException e1) {
							throw new RuntimeException(e1);
						}
					}).start();
				}
				
				
			}
			catch(Throwable t) {
				throw new RuntimeException(t);
			}
		});
	}
	
	private void selectDirectories(EJTabbedPane pane, File sourceDir, File destDir) throws InterruptedException {
		Tab tab = pane.getSelectedTab();
		if(sourceDir != null && !sourceDir.getPath().isBlank()) {
			textFieldSource.setText(sourceDir.getAbsolutePath());
		}
		else {
			textFieldSource.setText("");
		}
		if(destDir != null && !destDir.getPath().isBlank()) {
			textFieldDest.setText(destDir.getAbsolutePath());
		}
		else {
			textFieldDest.setText("");
		}
		
		copyOperations.shutdownNow();
		unarchiver = genUnarchiver(sourceDir, destDir);
		copyOperations = genCopyBatches();
		setupTabbedPane(pane);
		pane.setSelectedTab(tab);
		System.out.println("Set tab!");
		update();
	}
	
	public EJTabbedPane setupTabbedPane(EJTabbedPane tabbedPane) {
		Tab consoleTab = setupConsoleOutputTab(tabbedPane);
		Tab statusTab = setupStatusTab(tabbedPane);
		Tab progressTab = setupProgressTab(tabbedPane);
		
		tabbedPane.removeAll();
		tabbedPane.setTabLayoutPolicy(EJTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.addTab(consoleTab);
		tabbedPane.addTab(statusTab);
		tabbedPane.addTab(progressTab);
		
		for(Batcher<Pair<DecisionType, Callable<Void>>> b : copyOperations.getBatches()) {
			tabbedPane.addTab(b.getName(), new JPanel());
		}
		
		return tabbedPane;
	}
	
	private Tab setupConsoleOutputTab(EJTabbedPane tabbedPane) {
		Tab consoleTab = tabbedPane.getTab(CONSOLE);
		if(consoleTab != null) {
			return consoleTab;
		}
		
		JPanel consolePanel = new JPanel();
		consolePanel.setLayout(new GridLayout(0, 2, 0, 0));
		JTextArea textArea = new JTextArea();
		textArea.setBorder(BorderFactory.createLoweredBevelBorder());
		textArea.setOpaque(true);
		JTextAreaOutputStream textPaneOutputStream = new JTextAreaOutputStream(textArea);
		System.setOut(new PrintStream(SplitOutputStream.splitSysOut(textPaneOutputStream)));
		System.setErr(new PrintStream(SplitOutputStream.splitErrOut(textPaneOutputStream)));
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		new Throwable().printStackTrace(pw);
		textArea.setText(sw.toString());
		
		JScrollPane scrollPaneConsole = new JScrollPane(textArea);
		scrollPaneConsole.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		Tab tab = new Tab(CONSOLE, null, scrollPaneConsole, null);
		tabbedPane.addTab(tab.title(), tab.icon(), tab.component(), tab.tip()); //need to add this separately so the window builder can see it
		return tab;
	}
	
	private Tab setupStatusTab(EJTabbedPane tabbedPane) {
		Tab statusTab = tabbedPane.getTab(STATUS);
		
		if(statusTab != null) {
			//remove all
		}
		
		JPanel contents = setupStatusNavigationPanel(tabbedPane);
		

		
		Tab tab = new Tab(STATUS, null, contents, null);
		JPanel statusGrid = new JPanel(new WrapLayout());
		JScrollPane statusGridScroller = new JScrollPane(statusGrid);
		
		Iterator<Batcher<Pair<DecisionType, Callable<Void>>>> batches = copyOperations.getBatches().iterator();
		int i = 0;
		while(batches.hasNext()) {
			BatchOperationComponent b = new BatchOperationComponent(batches.next());
			b.setPreferredSize(new Dimension(150, 175));
			statusGrid.add(b);
			i++;
		}
		
		GridBagConstraints gbc_statusGridScroller = new GridBagConstraints();
		gbc_statusGridScroller.fill = GridBagConstraints.BOTH;
		gbc_statusGridScroller.gridx = 0;
		gbc_statusGridScroller.gridy = 1;
		contents.add(statusGridScroller, gbc_statusGridScroller);
		
		statusGridScroller.getVerticalScrollBar().setUnitIncrement(175 / 2);
		tabbedPane.addTab(tab.title(), tab.icon(), tab.component(), tab.tip()); //need to add this separately so the window builder can see it
		
		
		return tab;
	}
	
	private JPanel setupStatusNavigationPanel(EJTabbedPane tabbedPane) {
		JPanel contents = new JPanel();
		GridBagLayout gbl_contents = new GridBagLayout();
		gbl_contents.columnWidths = new int[]{22, 0};
		gbl_contents.rowHeights = new int[]{0, 13, 0};
		gbl_contents.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contents.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		contents.setLayout(gbl_contents);
		
		JPanel NavigationPanel = new JPanel();
		GridBagConstraints gbc_NavigationPanel = new GridBagConstraints();
		gbc_NavigationPanel.insets = new Insets(0, 0, 5, 0);
		gbc_NavigationPanel.fill = GridBagConstraints.BOTH;
		gbc_NavigationPanel.gridx = 0;
		gbc_NavigationPanel.gridy = 0;
		contents.add(NavigationPanel, gbc_NavigationPanel);
		NavigationPanel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblKey = new JLabel("Legend");
		lblKey.setHorizontalAlignment(SwingConstants.CENTER);
		NavigationPanel.add(lblKey);
		
		JPanel keysPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) keysPanel.getLayout();
		flowLayout.setHgap(20);
		flowLayout.setVgap(0);
		NavigationPanel.add(keysPanel, BorderLayout.SOUTH);
		
		keysPanel.add(new ColorKeyComponent(Color.GRAY, "Not Started"));
		keysPanel.add(new ColorKeyComponent(Color.ORANGE, "Other"));
		keysPanel.add(new ColorKeyComponent(Color.WHITE, "Working"));
		keysPanel.add(new ColorKeyComponent(Color.RED, "Failure"));
		keysPanel.add(new ColorKeyComponent(Color.CYAN.darker(), "Skipped"));
		keysPanel.add(new ColorKeyComponent(Color.GREEN, "Success"));
		
		return contents;
	}
	
	public Tab setupProgressTab(EJTabbedPane tabbedPane) {
		Tab progressTab = tabbedPane.getTab(PROGRESS);
		/*if(progressTab != null) {
			return progressTab;
		}*/
		
		JPanel progressPanel = new JPanel();
		Tab tab = new Tab(PROGRESS, null, progressPanel, null);
		tabbedPane.addTab(tab.title(), tab.icon(), tab.component(), tab.tip()); //need to add this separately so the window builder can see it
		
		progressPanel.setLayout(new GridLayout(0, 2, 0, 0));
		setupLeftProgressPane(progressPanel);
		setupRightProgressPane(progressPanel);
		
		return tab;
	}
	
	private void setupLeftProgressPane(JPanel progressPanel) {
		JPanel leftPanel = new JPanel();
		progressPanel.add(leftPanel);
		GridBagLayout gbl_leftPanel = new GridBagLayout();
		gbl_leftPanel.columnWidths = new int[] {100, 0, 70, 90, 0, 0, 11};
		gbl_leftPanel.rowHeights = new int[] {0, 15, 0, 0, 30, 0, 0, 30, 0, 0, 30, 0, 0, 29, 0};
		gbl_leftPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_leftPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		leftPanel.setLayout(gbl_leftPanel);
		
		JLabel lblCopyOperation = new JLabel("Unarchive Operation");
		GridBagConstraints gbc_lblCopyOperation = new GridBagConstraints();
		gbc_lblCopyOperation.gridwidth = 6;
		gbc_lblCopyOperation.insets = new Insets(0, 0, 5, 5);
		gbc_lblCopyOperation.gridx = 0;
		gbc_lblCopyOperation.gridy = 0;
		leftPanel.add(lblCopyOperation, gbc_lblCopyOperation);
		
		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setPreferredSize(new Dimension(1, 1));
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.gridheight = 14;
		gbc_separator.fill = GridBagConstraints.VERTICAL;
		gbc_separator.gridx = 6;
		gbc_separator.gridy = 0;
		leftPanel.add(separator, gbc_separator);
		BatchOperationComponent copyBatchComponent = new BatchOperationComponent(copyOperations);
		copyOperations.addBatchListener(copyBatchComponent);
		copyBatchComponent.setToolTipText("All Batches");
		System.out.println(copyOperations.getBatches().size() + " OISHDFPIOHDFIUOSPHUIFSIDOHUOFHUIOSDHIFUHOHIUO");
		
		GridBagConstraints gbc_copyBatchComponent = new GridBagConstraints();
		gbc_copyBatchComponent.fill = GridBagConstraints.BOTH;
		gbc_copyBatchComponent.gridheight = 13;
		gbc_copyBatchComponent.insets = new Insets(0, 0, 0, 5);
		gbc_copyBatchComponent.gridx = 0;
		gbc_copyBatchComponent.gridy = 1;
		leftPanel.add(copyBatchComponent, gbc_copyBatchComponent);
		
		JSeparator separator_1 = new JSeparator();
		GridBagConstraints gbc_separator_1 = new GridBagConstraints();
		gbc_separator_1.fill = GridBagConstraints.BOTH;
		gbc_separator_1.gridheight = 10;
		gbc_separator_1.insets = new Insets(0, 0, 5, 5);
		gbc_separator_1.gridx = 1;
		gbc_separator_1.gridy = 0;
		leftPanel.add(separator_1, gbc_separator_1);
		
		JLabel lblTotalArchives = new JLabel("Total Archives:");
		lblTotalArchives.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblTotalArchives = new GridBagConstraints();
		gbc_lblTotalArchives.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalArchives.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblTotalArchives.gridx = 2;
		gbc_lblTotalArchives.gridy = 2;
		leftPanel.add(lblTotalArchives, gbc_lblTotalArchives);
		
		JLabel lblTotalArchivesCount = new JLabel("0");
		GridBagConstraints gbc_lblTotalArchivesCount = new GridBagConstraints();
		gbc_lblTotalArchivesCount.anchor = GridBagConstraints.WEST;
		gbc_lblTotalArchivesCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalArchivesCount.gridx = 3;
		gbc_lblTotalArchivesCount.gridy = 2;
		leftPanel.add(lblTotalArchivesCount, gbc_lblTotalArchivesCount);
		
		JLabel lblFoundResources = new JLabel("Total Resources:");
		lblFoundResources.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblFoundResources = new GridBagConstraints();
		gbc_lblFoundResources.anchor = GridBagConstraints.EAST;
		gbc_lblFoundResources.insets = new Insets(0, 0, 5, 5);
		gbc_lblFoundResources.gridx = 2;
		gbc_lblFoundResources.gridy = 3;
		leftPanel.add(lblFoundResources, gbc_lblFoundResources);
		
		JLabel lblTotalResourcesCount = new JLabel("0");
		GridBagConstraints gbc_lblTotalResourcesCount = new GridBagConstraints();
		gbc_lblTotalResourcesCount.anchor = GridBagConstraints.WEST;
		gbc_lblTotalResourcesCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalResourcesCount.gridx = 3;
		gbc_lblTotalResourcesCount.gridy = 3;
		leftPanel.add(lblTotalResourcesCount, gbc_lblTotalResourcesCount);
		
		JLabel lblArchivesCopied = new JLabel("Archives Copied:");
		GridBagConstraints gbc_lblArchivesCopied = new GridBagConstraints();
		gbc_lblArchivesCopied.anchor = GridBagConstraints.EAST;
		gbc_lblArchivesCopied.insets = new Insets(0, 0, 5, 5);
		gbc_lblArchivesCopied.gridx = 2;
		gbc_lblArchivesCopied.gridy = 5;
		leftPanel.add(lblArchivesCopied, gbc_lblArchivesCopied);
		
		JLabel lblArchivesCopiedCount = new JLabel("0");
		GridBagConstraints gbc_lblArchivesCopiedCount = new GridBagConstraints();
		gbc_lblArchivesCopiedCount.anchor = GridBagConstraints.WEST;
		gbc_lblArchivesCopiedCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblArchivesCopiedCount.gridx = 3;
		gbc_lblArchivesCopiedCount.gridy = 5;
		leftPanel.add(lblArchivesCopiedCount, gbc_lblArchivesCopiedCount);
		
		JLabel lblResourcesCopied = new JLabel("Resources Copied:");
		GridBagConstraints gbc_lblResourcesCopied = new GridBagConstraints();
		gbc_lblResourcesCopied.anchor = GridBagConstraints.EAST;
		gbc_lblResourcesCopied.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourcesCopied.gridx = 2;
		gbc_lblResourcesCopied.gridy = 6;
		leftPanel.add(lblResourcesCopied, gbc_lblResourcesCopied);
		
		JLabel lblResourcesCopiedCount = new JLabel("0");
		GridBagConstraints gbc_lblResourcesCopiedCount = new GridBagConstraints();
		gbc_lblResourcesCopiedCount.anchor = GridBagConstraints.WEST;
		gbc_lblResourcesCopiedCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourcesCopiedCount.gridx = 3;
		gbc_lblResourcesCopiedCount.gridy = 6;
		leftPanel.add(lblResourcesCopiedCount, gbc_lblResourcesCopiedCount);
		
		JLabel lblResourcesCopiedPercent = new JLabel("0%");
		GridBagConstraints gbc_lblResourcesCopiedPercent = new GridBagConstraints();
		gbc_lblResourcesCopiedPercent.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourcesCopiedPercent.gridx = 4;
		gbc_lblResourcesCopiedPercent.gridy = 6;
		leftPanel.add(lblResourcesCopiedPercent, gbc_lblResourcesCopiedPercent);
		
		JLabel lblArchivesSkipped = new JLabel("Archives Skipped:");
		lblArchivesSkipped.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblArchivesSkipped = new GridBagConstraints();
		gbc_lblArchivesSkipped.anchor = GridBagConstraints.EAST;
		gbc_lblArchivesSkipped.insets = new Insets(0, 0, 5, 5);
		gbc_lblArchivesSkipped.gridx = 2;
		gbc_lblArchivesSkipped.gridy = 8;
		leftPanel.add(lblArchivesSkipped, gbc_lblArchivesSkipped);
		
		JLabel lblArchivesSkippedCount = new JLabel("0");
		GridBagConstraints gbc_lblArchivesSkippedCount = new GridBagConstraints();
		gbc_lblArchivesSkippedCount.anchor = GridBagConstraints.WEST;
		gbc_lblArchivesSkippedCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblArchivesSkippedCount.gridx = 3;
		gbc_lblArchivesSkippedCount.gridy = 8;
		leftPanel.add(lblArchivesSkippedCount, gbc_lblArchivesSkippedCount);
		
		JLabel lblArchivesSkippedPercent = new JLabel("0%");
		GridBagConstraints gbc_lblArchivesSkippedPercent = new GridBagConstraints();
		gbc_lblArchivesSkippedPercent.insets = new Insets(0, 0, 5, 5);
		gbc_lblArchivesSkippedPercent.gridx = 4;
		gbc_lblArchivesSkippedPercent.gridy = 8;
		leftPanel.add(lblArchivesSkippedPercent, gbc_lblArchivesSkippedPercent);
		
		JLabel lblResourcesSkipped = new JLabel("Resources Skipped:");
		GridBagConstraints gbc_lblResourcesSkipped = new GridBagConstraints();
		gbc_lblResourcesSkipped.anchor = GridBagConstraints.EAST;
		gbc_lblResourcesSkipped.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourcesSkipped.gridx = 2;
		gbc_lblResourcesSkipped.gridy = 9;
		leftPanel.add(lblResourcesSkipped, gbc_lblResourcesSkipped);
		
		JLabel lblResourcesSkippedCount = new JLabel("0");
		GridBagConstraints gbc_lblResourcesSkippedCount = new GridBagConstraints();
		gbc_lblResourcesSkippedCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourcesSkippedCount.anchor = GridBagConstraints.WEST;
		gbc_lblResourcesSkippedCount.gridx = 3;
		gbc_lblResourcesSkippedCount.gridy = 9;
		leftPanel.add(lblResourcesSkippedCount, gbc_lblResourcesSkippedCount);
		
		JLabel labelResourcesSkippedPercent = new JLabel("0%");
		GridBagConstraints gbc_labelResourcesSkippedPercent = new GridBagConstraints();
		gbc_labelResourcesSkippedPercent.insets = new Insets(0, 0, 5, 5);
		gbc_labelResourcesSkippedPercent.gridx = 4;
		gbc_labelResourcesSkippedPercent.gridy = 9;
		leftPanel.add(labelResourcesSkippedPercent, gbc_labelResourcesSkippedPercent);
		
		JLabel lblArchivesFailed = new JLabel("Archives Failed:");
		lblArchivesFailed.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblArchivesFailed = new GridBagConstraints();
		gbc_lblArchivesFailed.anchor = GridBagConstraints.EAST;
		gbc_lblArchivesFailed.insets = new Insets(0, 0, 5, 5);
		gbc_lblArchivesFailed.gridx = 2;
		gbc_lblArchivesFailed.gridy = 11;
		leftPanel.add(lblArchivesFailed, gbc_lblArchivesFailed);
		
		JLabel lblArchivesFailedCount = new JLabel("0");
		GridBagConstraints gbc_lblArchivesFailedCount = new GridBagConstraints();
		gbc_lblArchivesFailedCount.anchor = GridBagConstraints.WEST;
		gbc_lblArchivesFailedCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblArchivesFailedCount.gridx = 3;
		gbc_lblArchivesFailedCount.gridy = 11;
		leftPanel.add(lblArchivesFailedCount, gbc_lblArchivesFailedCount);
		
		JLabel lblArchivesFailedPercent = new JLabel("0%");
		GridBagConstraints gbc_lblArchivesFailedPercent = new GridBagConstraints();
		gbc_lblArchivesFailedPercent.insets = new Insets(0, 0, 5, 5);
		gbc_lblArchivesFailedPercent.gridx = 4;
		gbc_lblArchivesFailedPercent.gridy = 11;
		leftPanel.add(lblArchivesFailedPercent, gbc_lblArchivesFailedPercent);
		
		JLabel lblResourcesFailed = new JLabel("Resources Failed:");
		GridBagConstraints gbc_lblResourcesFailed = new GridBagConstraints();
		gbc_lblResourcesFailed.anchor = GridBagConstraints.EAST;
		gbc_lblResourcesFailed.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourcesFailed.gridx = 2;
		gbc_lblResourcesFailed.gridy = 12;
		leftPanel.add(lblResourcesFailed, gbc_lblResourcesFailed);
		
		JLabel lblResourcesFailedCount = new JLabel("0");
		GridBagConstraints gbc_lblResourcesFailedCount = new GridBagConstraints();
		gbc_lblResourcesFailedCount.anchor = GridBagConstraints.WEST;
		gbc_lblResourcesFailedCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourcesFailedCount.gridx = 3;
		gbc_lblResourcesFailedCount.gridy = 12;
		leftPanel.add(lblResourcesFailedCount, gbc_lblResourcesFailedCount);
		
		JLabel lblResourcesFailedPercent = new JLabel("0%");
		GridBagConstraints gbc_lblResourcesFailedPercent = new GridBagConstraints();
		gbc_lblResourcesFailedPercent.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourcesFailedPercent.gridx = 4;
		gbc_lblResourcesFailedPercent.gridy = 12;
		leftPanel.add(lblResourcesFailedPercent, gbc_lblResourcesFailedPercent);
		
		copyOperations.addBatchListener(() -> {
			SwingUtilities.invokeLater(() -> {
				//lblResourcesCopiedCount.setText(copyBatchComponent.);
				update();
			});
		});
	}
	
	private void setupRightProgressPane(JPanel progressPanel) {
		JPanel rightPanel = new JPanel();
		progressPanel.add(rightPanel);
		GridBagLayout gbl_rightPanel = new GridBagLayout();
		gbl_rightPanel.columnWidths = new int[] {0, 0, 90, 0, 30, 30, 0, 0};
		gbl_rightPanel.rowHeights = new int[]{0, 30, 0, 30, 0, 0, 0, 30, 30, 30, 30, 0, 0, 0, 0};
		gbl_rightPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_rightPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		rightPanel.setLayout(gbl_rightPanel);
		
		BatchOperationComponent copyBatchComponent = new BatchOperationComponent(processOperations);
		copyBatchComponent.setToolTipText("All Batches");
		
		GridBagConstraints gbc_copyBatchComponent = new GridBagConstraints();
		gbc_copyBatchComponent.fill = GridBagConstraints.BOTH;
		gbc_copyBatchComponent.gridheight = 13;
		gbc_copyBatchComponent.insets = new Insets(0, 0, 5, 5);
		gbc_copyBatchComponent.gridx = 0;
		gbc_copyBatchComponent.gridy = 1;
		rightPanel.add(copyBatchComponent, gbc_copyBatchComponent);
		
		JLabel lblNewLabel = new JLabel("Process Operation");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.gridwidth = 7;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		rightPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		JLabel lblTotalResources = new JLabel("Total Resources:");
		GridBagConstraints gbc_lblTotalResources = new GridBagConstraints();
		gbc_lblTotalResources.anchor = GridBagConstraints.EAST;
		gbc_lblTotalResources.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalResources.gridx = 1;
		gbc_lblTotalResources.gridy = 2;
		rightPanel.add(lblTotalResources, gbc_lblTotalResources);
		
		JLabel lblTotalResourcesCount = new JLabel("0");
		GridBagConstraints gbc_lblTotalResourcesCount = new GridBagConstraints();
		gbc_lblTotalResourcesCount.anchor = GridBagConstraints.WEST;
		gbc_lblTotalResourcesCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalResourcesCount.gridx = 2;
		gbc_lblTotalResourcesCount.gridy = 2;
		rightPanel.add(lblTotalResourcesCount, gbc_lblTotalResourcesCount);
		
		JLabel lblResourcesProcessed = new JLabel("Resources Processed:");
		GridBagConstraints gbc_lblResourcesProcessed = new GridBagConstraints();
		gbc_lblResourcesProcessed.anchor = GridBagConstraints.EAST;
		gbc_lblResourcesProcessed.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourcesProcessed.gridx = 1;
		gbc_lblResourcesProcessed.gridy = 4;
		rightPanel.add(lblResourcesProcessed, gbc_lblResourcesProcessed);
		
		JLabel lblResourcesProcessedCount = new JLabel("0");
		GridBagConstraints gbc_lblResourcesProcessedCount = new GridBagConstraints();
		gbc_lblResourcesProcessedCount.anchor = GridBagConstraints.WEST;
		gbc_lblResourcesProcessedCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourcesProcessedCount.gridx = 2;
		gbc_lblResourcesProcessedCount.gridy = 4;
		rightPanel.add(lblResourcesProcessedCount, gbc_lblResourcesProcessedCount);
		
		JLabel lblResourcesProcessedPercent = new JLabel("0%");
		GridBagConstraints gbc_lblResourcesProcessedPercent = new GridBagConstraints();
		gbc_lblResourcesProcessedPercent.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourcesProcessedPercent.gridx = 3;
		gbc_lblResourcesProcessedPercent.gridy = 4;
		rightPanel.add(lblResourcesProcessedPercent, gbc_lblResourcesProcessedPercent);
		
		JLabel lblResourcesSkipped = new JLabel("Resources Skipped:");
		GridBagConstraints gbc_lblResourcesSkipped = new GridBagConstraints();
		gbc_lblResourcesSkipped.anchor = GridBagConstraints.EAST;
		gbc_lblResourcesSkipped.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourcesSkipped.gridx = 1;
		gbc_lblResourcesSkipped.gridy = 5;
		rightPanel.add(lblResourcesSkipped, gbc_lblResourcesSkipped);
		
		JLabel lblResourcesSkippedCount = new JLabel("0");
		GridBagConstraints gbc_lblResourcesSkippedCount = new GridBagConstraints();
		gbc_lblResourcesSkippedCount.anchor = GridBagConstraints.WEST;
		gbc_lblResourcesSkippedCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourcesSkippedCount.gridx = 2;
		gbc_lblResourcesSkippedCount.gridy = 5;
		rightPanel.add(lblResourcesSkippedCount, gbc_lblResourcesSkippedCount);
		
		JLabel lblResourcesSkippedPercent = new JLabel("0%");
		GridBagConstraints gbc_lblResourcesSkippedPercent = new GridBagConstraints();
		gbc_lblResourcesSkippedPercent.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourcesSkippedPercent.gridx = 3;
		gbc_lblResourcesSkippedPercent.gridy = 5;
		rightPanel.add(lblResourcesSkippedPercent, gbc_lblResourcesSkippedPercent);
		
		JLabel lblResourcesFailed = new JLabel("Resources Failed:");
		GridBagConstraints gbc_lblResourcesFailed = new GridBagConstraints();
		gbc_lblResourcesFailed.anchor = GridBagConstraints.EAST;
		gbc_lblResourcesFailed.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourcesFailed.gridx = 1;
		gbc_lblResourcesFailed.gridy = 6;
		rightPanel.add(lblResourcesFailed, gbc_lblResourcesFailed);
		
		JLabel lblResourcesFailedCount = new JLabel("0");
		GridBagConstraints gbc_lblResourcesFailedCount = new GridBagConstraints();
		gbc_lblResourcesFailedCount.anchor = GridBagConstraints.WEST;
		gbc_lblResourcesFailedCount.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourcesFailedCount.gridx = 2;
		gbc_lblResourcesFailedCount.gridy = 6;
		rightPanel.add(lblResourcesFailedCount, gbc_lblResourcesFailedCount);
		
		JLabel lblResourcesFailedPercent = new JLabel("0%");
		GridBagConstraints gbc_lblResourcesFailedPercent = new GridBagConstraints();
		gbc_lblResourcesFailedPercent.insets = new Insets(0, 0, 5, 5);
		gbc_lblResourcesFailedPercent.gridx = 3;
		gbc_lblResourcesFailedPercent.gridy = 6;
		rightPanel.add(lblResourcesFailedPercent, gbc_lblResourcesFailedPercent);
	}
	
	private Unarchiver genUnarchiver(File source, File dest) {
		try {
			Unarchiver unarchiver = new Unarchiver(source.toPath(), dest.toPath());
			return unarchiver;
		}
		catch(Throwable t) {
			return null;
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private BatchRunner<Pair<DecisionType, Callable<Void>>> genCopyBatches() {
		BatchRunner<Pair<DecisionType, Callable<Void>>> batchRunner = new BatchRunner("Unarchive", this.getUsableThreads());
		try {
			if(unarchiver != null) {
				if(unarchiver.isValid()) {
					for(Batch<Pair<DecisionType, Callable<Void>>> batch : unarchiver.getCopyBatches()) {
						batchRunner.addBatch(batch);
					}
				}
			}
		}
		catch(Throwable t) {
			Batch<Pair<DecisionType, Callable<Void>>> errBatch = new Batch<>("Error");
			errBatch.addRunnable(() -> {
				throw t;
			});
			batchRunner.addBatch(errBatch);
		}
		return batchRunner;
	}
	
	private BatchRunner genProcessBatches(File dir) {
		BatchRunner batchRunner = new BatchRunner("Process");
		if(dir != null) {
			//stuff
		}
		return batchRunner;
	}
	
	public static enum BatchResult {
		SUCCESS,
		FAILURE,
		SKIP
	}
	
	@Override
	public void update() {
		frame.invalidate();
		frame.repaint();
	}
	
	private int getUsableThreads() {
		return Math.clamp(threadSlider.getValue(), 1, Runtime.getRuntime().availableProcessors());
	}
	
}
