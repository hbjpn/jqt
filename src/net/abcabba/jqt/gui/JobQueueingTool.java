/**
 * 
 * Copyright (C) 2012-2013 Hiroyuki Baba, All Rights Reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License or any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR POURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.abcabba.jqt.gui;

import net.abcabba.jqt.core.*;

import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

//import JobRunThread.PeriodicTimer;

import java.lang.Runtime;
import java.net.URL;
import java.net.URLClassLoader;

public class JobQueueingTool extends JFrame implements ActionListener, MouseListener{
	
	private static final long serialVersionUID = 1L;
	
	static final String windowTitle = "Job Queueing Tool";
	
	public static OSType osType = getOsType();
	
	// Jqt Engine
    JqtEngine engine;
    
    javax.swing.Timer updateTimer = new javax.swing.Timer(1000, this);
    
    int nextJobId;
	
	// Job table
	JTable jobListTable;
	
	JMenuItem menuOpen;
	JMenuItem menuSave;
	JMenuItem menuSaveAs;
	
	JMenuItem menuAddGeneralJobs;
	JMenuItem menuClearCompletedErroredJobs;
	JMenuItem menuClearAllJobs;
	
	JMenuItem menuPlayPause;
	JMenuItem menuExit;
	JMenuItem menuAbout;
	
	JTextArea outputWindow = new JTextArea();
	
	String openFile = null;
	
	//DefaultTableModel jobListTableModel;
	//AbstractTableModel jobListTableModel;
	JobTableModel jobListTableModel;
	
	// For popup menu on table.
	JPopupMenu popup;
    JMenuItem menuPuClearSelectedJobs = new JMenuItem("Clear selected jobs");
    JMenuItem menuPuOpenDirectory = new JMenuItem("Open directory");
    JMenuItem menuPuOpenConfig = new JMenuItem("Open config file");
    JMenuItem menuPuRecalculateSelectedJobs = new JMenuItem("Recalculate selected jobs");
    
    JMenuItem menuPreference = new JMenuItem("Preference ...");
	
	//ArrayList<Job> jobList = new ArrayList<Job>();
	
	AddGeneralJobDialog addGeneralJobDialog = new AddGeneralJobDialog(this);
	
	PreferenceDialog preferenceDialog = new PreferenceDialog(this);
	
	Integer maxAvailableProcessors = Runtime.getRuntime().availableProcessors() - 1;
	public static Integer currentUsedProcessors = new Integer(0);
    
    /**
     * Job scheduler plugins
     */
    private ArrayList <JobSchedulerPluginBase> plugins;
    
    /**
     * Constructor
     */
	JobQueueingTool() {
		
		loadProperties();
		
        plugins = loadPlugins();
        
        engine = new JqtEngine(maxAvailableProcessors);
        engine.start();
        
        nextJobId = 0;
        updateTimer.setRepeats(true);
        updateTimer.start();
		
		addWindowListener(new WindowAdapter() { 
			//@Override 
			public void windowClosing(WindowEvent evt) { 
				System.out.println("windowClosing");
				try{
					saveProperties();
				}catch( IOException e ){
					e.printStackTrace();
				}
			}
		}); 
		
		/**
		 * DEbug
		 */
		System.out.printf("Max Jobs = %d\n", maxAvailableProcessors);

		
		Container c = getContentPane();
		
		/**
		 * Split Pane
		 */
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		c.add(splitPane);		
		
		//c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
	
		jobListTableModel = new JobTableModel();
		jobListTable = new JTable(jobListTableModel);
		jobListTable.setDefaultRenderer(Object.class, new StripeTableRenderer());
		jobListTable.getColumnModel().getColumn(JobTableModel.JOBID_COLUMN).setMaxWidth(20);
		jobListTable.getColumnModel().getColumn(JobTableModel.STATUS_COLUMN).setMaxWidth(80);
		jobListTable.getColumnModel().getColumn(JobTableModel.CONFIG_NAME_COLUMN).setPreferredWidth(120);
		jobListTable.getColumnModel().getColumn(JobTableModel.NP_COLUMN).setMaxWidth(80);
		jobListTable.getColumnModel().getColumn(JobTableModel.PARENT_COLUMN).setMaxWidth(60);		
		jobListTable.getColumnModel().getColumn(JobTableModel.PROGRESS_COLUMN).setPreferredWidth(80);
		
		jobListTable.addMouseListener(this);
		
		jobListTable.setDragEnabled(true);
		jobListTable.setTransferHandler(new JobDragTransferHandler());
		
		// Hide grid lines
		jobListTable.setShowGrid(false);
		jobListTable.setIntercellSpacing(new Dimension(0,0));
		
		// Modify row height
		jobListTable.setRowHeight(20);

		jobListTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){

			//@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				if( e.getValueIsAdjusting() ) return;
				
				int firstSelectedRow = jobListTable.getSelectedRow();
				if( firstSelectedRow >= 0 ){
					//outputWindow.setText(jobListTableModel.get(firstSelectedRow).getStdouts());					
				}
			}
			
		});
		
		JScrollPane sp = new JScrollPane(jobListTable);
		//sp.setPreferredSize(new Dimension(230, 80));
		//c.add(sp);
		splitPane.setTopComponent(sp);
		
		/**
		 * Output window
		 */
		//c.add(outputWindow);
		JScrollPane spOutputWindow = new JScrollPane(outputWindow);
		splitPane.setBottomComponent(spOutputWindow);
		

		//String[] data = { "A5", "B5", "C5" };
		//tm.addRow(data);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setOpenFile(null);
		setSize(800, 600);
		setVisible(true);
		
		
		/* Menu Bars */
		JMenuBar menuBar = new JMenuBar();
		JMenu menuFile = new JMenu("File");
		menuOpen = new JMenuItem("Open ...");
		menuSave = new JMenuItem("Save");
		menuSaveAs = new JMenuItem("Save As ...");
		menuExit = new JMenuItem("Exit");		
	
		JMenu menuJob = new JMenu("Job");
		menuAddGeneralJobs = new JMenuItem("Add General jobs ...");
		menuClearCompletedErroredJobs = new JMenuItem("Clear completed/errored jobs");
		menuClearAllJobs = new JMenuItem("Clear all jobs");
		
		JMenu menuControl = new JMenu("Control");
		menuPlayPause = new JMenuItem("Play");
		
		JMenu menuHelp = new JMenu("Help");
		menuAbout = new JMenuItem("About");

		menuFile.setMnemonic('F');
		menuOpen.setMnemonic('O');
		menuSave.setMnemonic('S');
		
		menuPreference.setMnemonic('P');
		menuExit.setMnemonic('x');
		menuJob.setMnemonic('J');
		menuAddGeneralJobs.setMnemonic('G');
		menuControl.setMnemonic('C');
		menuHelp.setMnemonic('H');

		menuOpen.addActionListener(this);
		menuSave.addActionListener(this);
		menuSaveAs.addActionListener(this);
		menuPreference.addActionListener(this);
		menuExit.addActionListener(this);
		menuAddGeneralJobs.addActionListener(this);
		menuClearCompletedErroredJobs.addActionListener(this);
		menuClearAllJobs.addActionListener(this);
		menuPlayPause.addActionListener(this);

		getRootPane().setJMenuBar(menuBar);
		menuBar.add(menuFile);
		menuFile.add(menuOpen);
		menuFile.add(menuSave);
		menuFile.add(menuSaveAs);
		menuFile.addSeparator();
		menuFile.add(menuPreference);
		menuFile.addSeparator();
		menuFile.add(menuExit);
		
		menuBar.add(menuJob);
		menuJob.add(menuAddGeneralJobs);
		menuJob.addSeparator();
		menuJob.add(menuClearCompletedErroredJobs);
		menuJob.add(menuClearAllJobs);
		
		menuBar.add(menuControl);
		menuControl.add(menuPlayPause);
		
		menuBar.add(menuHelp);
		menuHelp.add(menuAbout);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Job queueing tool");
		setVisible(true);

		
		/*
		 * Configuration for pop-up menu.
		 */
	    popup = new JPopupMenu();

	    popup.add(menuPuClearSelectedJobs);
	    popup.addSeparator();
	    popup.add(menuPuOpenDirectory);
	    popup.add(menuPuOpenConfig);
	    popup.addSeparator();
	    popup.add(menuPuRecalculateSelectedJobs);
	    
	    menuPuClearSelectedJobs.addActionListener(this);
	    menuPuOpenDirectory.addActionListener(this);
	    menuPuOpenConfig.addActionListener(this);
	    menuPuRecalculateSelectedJobs.addActionListener(this);
		
	
	}
	
	public void setOpenFile(String path)
	{
		openFile = path;
		setTitle(windowTitle + " : " + path);
	}
	
	public void saveJobs(String path)
	{
		
		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docbuilder = null;
		try {
			docbuilder = dbfactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Document document = docbuilder.newDocument();
		document.appendChild(document.createElement("jobs"));

		for(int r = 0; r < jobListTableModel.getRowCount(); ++r)
		{
			Node jobNode = jobListTableModel.get(r).getJobInfo().relatedJob.toXml(document);
			document.getLastChild().appendChild(jobNode);
		}
		
		File outXml = new File(path);

		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transFactory.newTransformer();
		} catch (TransformerConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		
		// OutputPropertiesFactory.S_KEY_INDENT_AMOUNTをセットする
		//transformer.setOutputProperty(
		//		OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "2");

		Result result = new StreamResult(outXml);

		try {
			transformer.transform(new DOMSource(document), result);
		} catch (TransformerException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		//System.out.println("ActionPerformed : [" + e.getActionCommand() + "]");
		if( e.getSource() == menuOpen)
		{
		    JFileChooser filechooser = new JFileChooser(".");
		    filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	
		    int selected = filechooser.showOpenDialog(this);
		    if (selected == JFileChooser.APPROVE_OPTION){
				File file = filechooser.getSelectedFile();
				
				if(openFile == null)
				{
					setOpenFile(file.getAbsolutePath());
					ArrayList<JqtJob> jobList = JqtJob.fromXml(openFile);
					for(JqtJob job : jobList)
					{
						addJob(job);
					}
				}else
				{	
					
				    int result = JOptionPane.showConfirmDialog(
				    		this, "Are you sure terminating current jobs ? ",
				            "Alert", JOptionPane.OK_CANCEL_OPTION);
				   
				    if(result == JOptionPane.OK_OPTION)
				    {
					        
						engine.stopEngine();
						ArrayList<JqtJobInfo> jobInfos = engine.getJobInfo();
						for(JqtJobInfo jobInfo : jobInfos)
						{
							boolean ret;
							ret = engine.terminate(jobInfo.relatedJob);
							System.out.println("Try to terminate " + jobInfo.relatedJob + ":" + ret);
							engine.remove(jobInfo.relatedJob);
							System.out.println("Try to remove " + jobInfo.relatedJob + ":" + ret);
						}
						
						this.jobListTableModel.removeAll();
						
						setOpenFile(file.getAbsolutePath());
						
						ArrayList<JqtJob> jobList = JqtJob.fromXml(openFile);
						for(JqtJob job : jobList)
						{
							addJob(job);
						}

						jobListTable.updateUI();
					}
				}
		    }
		}else if( e.getSource() == menuSaveAs )
		{
		    JFileChooser filechooser = new JFileChooser(".");
		    filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	
		    int selected = filechooser.showSaveDialog(this);
		    if (selected == JFileChooser.APPROVE_OPTION){
				File file = filechooser.getSelectedFile();
				setOpenFile(file.getAbsolutePath());
				saveJobs(openFile);
		    }
		}
		else if( e.getSource() == menuSave )
		{
			if(openFile != null)
			{
				saveJobs(openFile);
			}else
			{
			    JFileChooser filechooser = new JFileChooser(".");
			    filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
			    int selected = filechooser.showSaveDialog(this);
			    if (selected == JFileChooser.APPROVE_OPTION){
					File file = filechooser.getSelectedFile();
					setOpenFile(file.getAbsolutePath());
					saveJobs(openFile);
			    }
			}
			
		}else if( e.getSource() == menuAddGeneralJobs ){
			addGeneralJobDialog.setAlwaysOnTop(false);
			addGeneralJobDialog.setVisible(true);
		}else if( e.getSource() == menuExit )
		{
			this.processWindowEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
			
		}else if( e.getSource() == menuPlayPause ){
			if( engine.getStatus() == JqtEngine.JqtEngineStatus.IDLE ){ 
				engine.playEngine();
				menuPlayPause.setText("Pause");
			}else{
				engine.stopEngine();
				menuPlayPause.setText("Play");
			}
		}else if( e.getSource() == menuPreference ){
			preferenceDialog.maxAvailableProcessorTextField.setText(maxAvailableProcessors.toString());
			preferenceDialog.setModal(true);
			preferenceDialog.setVisible(true);
			
			if( preferenceDialog.lastOperation == preferenceDialog.OKButton ){
				maxAvailableProcessors = Integer.valueOf( preferenceDialog.maxAvailableProcessorTextField.getText() );
				engine.changeProcessors(maxAvailableProcessors);
			}
				
		}else if( e.getSource() == menuPuClearSelectedJobs ){
			clearSelectedJobs();
		}else if( e.getSource() == menuClearCompletedErroredJobs ){
			clearCompletedErroredJobs();
		}else if( e.getSource() == menuPuOpenDirectory ){
			JqtJob job = jobListTableModel.get(jobListTable.getSelectedRow()).getJobInfo().relatedJob;
			try{
				Desktop.getDesktop().open(new File(job.directoryPath));
			}catch(IOException exc){
				
			}
		}else if( e.getSource() == menuPuOpenConfig ){
			JqtJob job = jobListTableModel.get(jobListTable.getSelectedRow()).getJobInfo().relatedJob;
			try{
				String fullPath = new File(job.directoryPath, job.firstArg.get(0)).getPath();
				Desktop.getDesktop().open(new File(fullPath));
			}catch(IOException exc){
				
			}
		}else if( e.getSource() == menuPuRecalculateSelectedJobs ){
			for(int r : jobListTable.getSelectedRows())
			{
				engine.reset(jobListTableModel.get(r).getJobInfo().relatedJob);
			}
		}else if( e.getSource() == this.updateTimer )
		{
			for(int i = 0; i < jobListTable.getRowCount(); ++i)
			{
				JobTableElement element = jobListTableModel.get(i);
				JqtJob job = element.getJobInfo().relatedJob;
				element.setJobInfo(this.engine.getJobInfo(job));
			}
			jobListTable.updateUI();
		}
			
	}
	
	public void clearCompletedErroredJobs(){
		/*
		Job job;
		ArrayList<Integer> rows = new ArrayList<Integer>();
		for(int i = 0;i < jobListTableModel.getRowCount();++i){
			job = jobListTableModel.get(i);
			if( job.getStatus() == Job.statusComplete || job.getStatus() == Job.statusError ){
				rows.add(i);
			}
		}
		int[] targetRows = new int[rows.size()];
		for(int i = 0; i < rows.size(); ++i){
			targetRows[i] = rows.get(i).intValue();
		}
		jobListTableModel.removeRows(targetRows);
		*/
	}
	
	/*
	 * Clear currently selected jobs if possible.
	 */
	public void clearSelectedJobs(){
		int[] selectedRows = jobListTable.getSelectedRows();
		jobListTableModel.removeRows(selectedRows);
	}
	
	/**
	 * @param directoryPath
	 * @param firstArg
	 * @param env
	 */
	public void addJob(
			String directoryPath,
			ArrayList<String> firstArg,
			Hashtable<String, String> env,
			String exePath,
			int np)
	{
		JqtJob newJob = new JqtJob(directoryPath, firstArg, env, exePath, np);
		addJob(newJob);
	}
	
	/**
	 * @param newJob New job to add
	 */
	public void addJob(JqtJob newJob)
	{
		engine.add(newJob);
		JqtJobInfo jobInfo = engine.getJobInfo(newJob);
		
		JobTableElement elem = new JobTableElement(jobInfo, nextJobId);
		
		++nextJobId;
		
		//jobList.add( newJob );
		
		//jobListTableModel.addRow(new Object[]{true, newJob.status, newJob.directoryPath, newJob.configName, newJob.stdoutsTail});
		jobListTableModel.addRow( elem ); //new Object[]{true, newJob.status, newJob.directoryPath, newJob.configName, newJob.stdoutsTail});
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JobQueueingTool.osType = JobQueueingTool.getOsType();
		
		new JobQueueingTool();
	}
	
	/**
	 * OS type
	 */
	enum OSType{
		WINDOWS,
		LINUX,
		UNIX,
		MAC,
		INVALID_OS_TYPE
	}
	
	/**
	 * Get OS type
	 * 
	 * @return OS type
	 */
	private static OSType getOsType(){
		// Judge OS
		String osName = System.getProperty("os.name");
		// System.out.println(osName);
		OSType osType = OSType.INVALID_OS_TYPE;
		if( osName == "MAC OS X"){
			osType = OSType.MAC;
		}else if( osName.contains("Windows")){
			osType = OSType.WINDOWS;
		}else{
			osType = OSType.UNIX;
		}
		
		return osType;
	}


	/**
	 * Save the properties
	 * @throws IOException
	 */
	public void saveProperties() throws IOException {
		Properties properties = new Properties(); 

		properties.setProperty("DEFAULT_PROCESSORS", maxAvailableProcessors.toString());
		
		properties.setProperty("DEFAULT_GENERAL_SEARCH_DIR_PATH", addGeneralJobDialog.targetDirTextField.getText());
	      
		File saveFile = new File("personal.properties"); 
		OutputStream out = new FileOutputStream(saveFile);         

		properties.store(out, null); 
	 
		out.close(); 
	}
		
	/*
	 * Load properties from property file
	 */
	public void loadProperties(){
	      
	  Properties properties = new Properties();         
	  File file = new File("personal.properties");
	  
	  try{
		  InputStream in = new FileInputStream(file); 
		  properties.load(in);      
		  in.close();
	  
		  maxAvailableProcessors = Integer.valueOf(properties.getProperty("DEFAULT_PROCESSORS"));
		  
		  addGeneralJobDialog.targetDirTextField.setText(properties.getProperty("DEFAULT_GENERAL_SEARCH_DIR_PATH"));
	  }catch(Exception e){
		  ;
	  }
	  

 	}
	
	/*
	 * Method regarding MouseListener
	 */

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		ShowPopupMenu(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		ShowPopupMenu(e);
	}
	
	public void ShowPopupMenu(MouseEvent e){		
	    if (e.isPopupTrigger()) {
			menuPuOpenDirectory.setEnabled(jobListTable.getSelectedRowCount() == 1);
			menuPuOpenConfig.setEnabled(jobListTable.getSelectedRowCount() == 1);
	    	popup.show(e.getComponent(), e.getX(), e.getY());
	    }	
	}
	
	/*
	 * Static functions
	 */
	public static ArrayList<String> SplitArguments(String str){
		ArrayList<String> args = new ArrayList<String>();
		String[] argsArray = str.split(" ");
		for(int i = 0; i < argsArray.length; ++i){
			args.add(argsArray[i]);
		}
		return args;
		
	}
	
    /**
     * Load job scheduler plugins and store them to ArrayList
     * 
     * @return Loaded job scheduler plugins
     */
    public ArrayList<JobSchedulerPluginBase> loadPlugins() {
        ArrayList <JobSchedulerPluginBase> plugins =
                new ArrayList<JobSchedulerPluginBase>();
        
        String cpath = System.getProperty("user.dir") +
                File.separator + "plugins";
        
        System.out.println("# Loading plugins ... ");
        try {
            File f = new File(cpath);
            if( (!f.exists()) || (!f.isDirectory()))
            {
            	System.out.println("# Cannot access to plugings directory : " + cpath);
            }else
            {
	            String[] files = f.list();
	            for (int i = 0; i < files.length; i++) {
	                if (files[i].endsWith(".jar")) {
	                    File file = new File(cpath + File.separator +
	                            files[i]);
	                    JarFile jar = new JarFile(file);
	                    Manifest mf = jar.getManifest();
	                    Attributes att = mf.getMainAttributes();
	                    String cname = att.getValue("Plugin-Class");
	                    URL url = file.getCanonicalFile().toURI().toURL();
	                    URLClassLoader loader = new URLClassLoader(
	                            new URL[] { url });
	                    Class cobj = loader.loadClass(cname);
	                    Class[] ifnames = cobj.getInterfaces();
	                    for (int j = 0; j < ifnames.length; j++) {
	                        if (ifnames[j] == JobSchedulerPluginBase.class) {
	                            System.out.println("Load plugin : " + cname);
	                            JobSchedulerPluginBase plugin =
	                                (JobSchedulerPluginBase)cobj.newInstance();
	                            plugins.add(plugin);
	                            break;
	                        }
	                    }
	                }
	            }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("# " + plugins.size() + " plugins loaded.");
        return plugins;
    }
}
