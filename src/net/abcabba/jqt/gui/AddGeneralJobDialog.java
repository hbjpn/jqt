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

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Map of extension and execution program
 */
class ScriptExecutionRule {
	
	private Hashtable<String, ArrayList<String>> extMap = new Hashtable<String, ArrayList<String>>();
	
	private static ScriptExecutionRule instance = new ScriptExecutionRule();
	
	private ScriptExecutionRule()
	{
		// TODO : Treat as configurable file ?
		
		// Windows
		extMap.put("ps1", AddGeneralJobDialog.parseArguments("powershell.exe -inputformat none -file $FILE_NAME"));
		extMap.put("bat", AddGeneralJobDialog.parseArguments("cmd.exe -c $FILE_NAME"));
		
		// Linux/Unix/Mac 
		extMap.put("sh", AddGeneralJobDialog.parseArguments("bash $FILE_NAME"));
		
		// Common
		extMap.put("py", AddGeneralJobDialog.parseArguments("python $FILE_NAME"));
		extMap.put("rb", AddGeneralJobDialog.parseArguments("ruby $FILE_NAME"));
	}
	
	public static ScriptExecutionRule getInstance()
	{
		return instance;
	}
	
	/**
	 * Get execution program from extension
	 * 
	 * @param ext Extension( excluding period )
	 * @return Execution program. If no map found, null is returned.
	 */
	public ArrayList<String> getExPath(String ext)
	{
		if(extMap.containsKey(ext))
		{
			return extMap.get(ext);
		}
		return null;
	}
}

class ScriptProp {
	public String directoryPath;
	public String configName;
	ScriptProp(String directoryPath, String configName){
		this.directoryPath = directoryPath;
		this.configName = configName;
	}
}

class JTwoElementPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	JTwoElementPanel(JComponent c0, JComponent c1){
		Dimension size = new Dimension(20,20);
		/*c0.setMaximumSize(size);
		c0.setMinimumSize(size);
		c0.setPreferredSize(size);
		c1.setMaximumSize(size);
		c1.setMinimumSize(size);
		c1.setPreferredSize(size);*/
		add(c0);
		add(c1);
		this.setPreferredSize(size);
	}
}

class JConfigPanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int rows;
	int cols;
	
	JConfigPanel(){
		rows = 0;
		cols = 2;
		
		//setLayout(new GridLayout(2, 2));
		setBorder(new EmptyBorder(8,8,8,8));
	}
	
	public void add2(JComponent left, JComponent right){
		
		++rows;
		setLayout(new GridLayout(rows, cols));
		
		left.setMaximumSize(new Dimension(1000,20));
		right.setMaximumSize(new Dimension(1000,20));
		add(left);
		add(right);
	}
}

/**
 * Dialog for adding generic job
 * 
 * @author baba
 *
 */
public class AddGeneralJobDialog extends JDialog implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	JButton selectDirButton = new JButton("...");
	JTextField targetDirTextField = new JTextField("", 30);
	JButton getScriptsButton = new JButton("Search scripts");
	
	JTextField envHomesTextField = new JTextField("",30);
	JButton envHomesSelectDirButton = new JButton("...");
	
	JTextField exeTextField = new JTextField("",30);
	JButton exeSelectButton = new JButton("...");
	
	JComboBox mathcerStringComboBox = new JComboBox(new String[]{".*bat",".*ps1",".*sh",".*exe"});
	
	JTextField npTextField = new JTextField("1",3);
	JTextField argsTextField = new JTextField("",30);
	
	JButton clearAllButton = new JButton("Clear all");
	JButton clearSelectedButton = new JButton("Clear selected");
	JButton selectAllButton = new JButton("Select All");
	JButton deselectAllButton = new JButton("Deselect All");
	JButton invertSelectionButton = new JButton("Invert Selection");
	
	JButton evAddButton = new JButton("+");
	JButton evRemoveButton = new JButton("-");
	
	JButton addSelectedScriptsButton = new JButton("Add selected scripts to job queue");
	
	/*
	 * Scenario table
	 */
	JTable candidateScriptsTable;
	DefaultTableModel candidateScriptsTableModel;
	
	private static final int scenarioDirColumn = 0;
	private static final int configNameColumn = 1;
	
	/*
	 * Environmental variable table
	 */
	JTable environmentalVariableTable;
	DefaultTableModel environmentalVariableTableModel;
	
	private static final int envVariableColumn = 0;
	private static final int envValueColumn = 1;
	
	/**
	 * Parent Job table
	 */
	JTable parentJobTable;
	JobTableModel parentJobTableModel;

	
	/**
	 * Search files which match specified regex pattern from specified directory 
	 * including sub-directories.
	 * 
	 * @param searchFileRegex Regex pattern object to be searched
	 * @param basePath Path to the searching target directory path
	 * @param matchFileList Storage to the matched files
	 */	
	void searchFiles(Pattern searchFileRegex, String basePath, ArrayList<ScriptProp> matchedFileList){
	    File dir = new File(basePath);
	    File[] files = dir.listFiles();
	    
	    //boolean isAdded = false;

	    for (int i = 0; i < files.length; i++) {
	        File file = files[i];
	        if( file.isDirectory() ){
	        	searchFiles(searchFileRegex, file.getAbsolutePath(), matchedFileList);
	        }else{
	        	//if( isAdded == false ){
	    	    Matcher m = searchFileRegex.matcher(file.getName());
	    	    if( m.matches() ){
	    	    	matchedFileList.add(new ScriptProp(basePath, file.getName()));
	    	    	//isAdded = true;
	    	    }		    	    
	        	//}
	        }
	    }
	}

	public AddGeneralJobDialog(JFrame owner) {
		super(owner);		
		setTitle("Add General Jobs");
		setBounds(64, 64, 500, 700);
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		Container c = getContentPane();
	
		//c.setLayout(new GridLayout(3,1));
		c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));

		// Top panels
		JPanel scriptExplainPanel = new JPanel();
		JPanel envPanel = new JPanel();
		JPanel searchDirPanel = new JPanel();
		JPanel addButtonPanel = new JPanel();
		

		
		c.add(scriptExplainPanel);
		c.add(envPanel);
		c.add(searchDirPanel);
		c.add(addButtonPanel);
		
		/**
		 * envPanel
		 */
		envPanel.setLayout(new BoxLayout(envPanel,BoxLayout.Y_AXIS));
		envPanel.setBorder(
				new CompoundBorder(
						new TitledBorder("Environment"),
						new EmptyBorder(8,8,8,8)));


		
		
		JTabbedPane exeOptTabbedPane = new JTabbedPane();
		envPanel.add( exeOptTabbedPane );
		
		scriptExplainPanel.setLayout(new BoxLayout(scriptExplainPanel,BoxLayout.X_AXIS));
		//exePanel.setLayout(new BoxLayout(exePanel,BoxLayout.X_AXIS));
		/*scriptExplainPanel.setBorder(
				new CompoundBorder(
						new TitledBorder(""),
						new EmptyBorder(8,8,8,8)));*/
		
		scriptExplainPanel.setBorder(
						new EmptyBorder(16,16,16,16));
		
		scriptExplainPanel.add( new JLabel("<html>"+
				"Windows   : Script file must be invokable with command \"<font color=\"red\">cmd.exe /c {script file}</font>\"<br>"+
				"Linux/Mac : Script file must be invokable with command \"<font color=\"red\">bash {script file}\"</font></html>") );
		//scriptTypePanel.add( envHomesTextField );
		//scriptTypePanel.add( envHomesSelectDirButton );
		
		envHomesSelectDirButton.addActionListener(this);
		
		envHomesTextField.setMaximumSize(new Dimension(2000,24));		
		
//		exePanel.setBorder(new CompoundBorder(new TitledBorder("Execution File"),new EmptyBorder(4,4,4,4)));
//		exePanel.add( exeTextField );
//		exePanel.add( exeSelectButton );
		
		exeSelectButton.addActionListener(this);
		
		exeTextField.setMaximumSize(new Dimension(2000,24));
		
		//JPanel exeOptPanel = new JPanel();
		JConfigPanel exeOptPanel = new JConfigPanel();
		JPanel evPanel = new JPanel();
		JPanel parentJobPanel = new JPanel();
		
		//exeOptPanel.setBorder(new CompoundBorder(new TitledBorder("Execution options"),new EmptyBorder(4,4,4,4)));
		//exeOptPanel.setLayout(new BoxLayout(exeOptPanel, BoxLayout.Y_AXIS));
		evPanel.setLayout(new BoxLayout(evPanel, BoxLayout.Y_AXIS));
		parentJobPanel.setLayout(new BoxLayout(parentJobPanel, BoxLayout.Y_AXIS));
		
		exeOptTabbedPane.addTab("Execution options", exeOptPanel);
		exeOptTabbedPane.addTab("Environment variables", evPanel);
		exeOptTabbedPane.addTab("Parent jobs", parentJobPanel);
		//JTable parentJobTable = new JTable(environmentalVariableTableModel);
		//exeOptPanel.add2(new JLabel("Parent Jobs"), parentJobTable);
		
		
		//rightExeOptPanel.setBorder(new CompoundBorder(
		//		new TitledBorder("Environment variables"),
		//		new EmptyBorder(8,8,8,8)
		//		));
		
		/**
		 * Parent job table
		 */
		parentJobTableModel = new JobTableModel(); 
		parentJobTable = new JTable(parentJobTableModel);
		parentJobTable.setDragEnabled(true);
		parentJobTable.setTransferHandler(new JobDropTransferHandler());
		parentJobTable.setFillsViewportHeight(true);
		/*parentJobTable.setDefaultRenderer(Object.class, new StripeTableRenderer());
		parentJobTable.getColumnModel().getColumn(JobTableModel.STATUS_COLUMN).setMaxWidth(80);
		parentJobTable.getColumnModel().getColumn(JobTableModel.CONFIG_NAME_COLUMN).setPreferredWidth(120);
		parentJobTable.getColumnModel().getColumn(JobTableModel.NP_COLUMN).setMaxWidth(80);
		parentJobTable.getColumnModel().getColumn(JobTableModel.PROGRESS_COLUMN).setPreferredWidth(80);*/
		parentJobPanel.add(new JScrollPane(parentJobTable));
		
		/**
		 * Environmental variable table
		 */
		String[] evColNames = { "Variable", "Value" };
		environmentalVariableTableModel = new DefaultTableModel(evColNames, 0){
			private static final long serialVersionUID = 2L;
			public Class<?> getColumnClass(int column){
				return String.class;
			}
		};
		environmentalVariableTable = new JTable(environmentalVariableTableModel);
		
		/* Example of adding row 
		environmentalVariableTableModel.addRow(new Object[]{"LTE_LOG_LEVEL","2"});
		environmentalVariableTableModel.addRow(new Object[]{"LTE_LOG_VALIDATION_PATTERN","007050"});
		environmentalVariableTableModel.addRow(new Object[]{"LTE_LOG_NODE_FILTER","1,43"});
		*/
		
		JScrollPane spEvTable = new JScrollPane(environmentalVariableTable);
		
		JPanel tmpPanel = new JPanel();

		evAddButton.addActionListener(this);
		evRemoveButton.addActionListener(this);
	    
		Dimension buttonSize = new Dimension(20,20);
		evAddButton.setMargin(new Insets(0,0,0,0));
		evRemoveButton.setMargin(new Insets(0,0,0,0));
	    evAddButton.setMaximumSize(buttonSize);
	    evRemoveButton.setMaximumSize(buttonSize);
	    evAddButton.setPreferredSize(buttonSize);
	    evRemoveButton.setPreferredSize(buttonSize);

	    tmpPanel.add(evAddButton);
	    tmpPanel.add(evRemoveButton);
	    evPanel.add(tmpPanel);
		evPanel.add(spEvTable);
		
		evAddButton.setAlignmentX(CENTER_ALIGNMENT);
		
		tmpPanel = new JPanel();

		/* Number of processors */
		exeOptPanel.add2(new JLabel("Reserved number of processors : "), npTextField);
		exeOptPanel.add2(new JLabel("Arguments : "), argsTextField);
		
		/**
		 * searchDirPanel
		 */
		searchDirPanel.setLayout(new BoxLayout(searchDirPanel, BoxLayout.Y_AXIS));
		
		JPanel selectDirPanel = new JPanel();
		JPanel getScenariosButtonPanel = new JPanel();
		JPanel candScenarioListPanel = new JPanel();
		JPanel editButtonPanel = new JPanel();
		
		searchDirPanel.add(selectDirPanel);
		searchDirPanel.add(getScenariosButtonPanel);
		searchDirPanel.add(candScenarioListPanel);
		searchDirPanel.add(editButtonPanel);
		
		searchDirPanel.setBorder(
				new CompoundBorder(
						new TitledBorder("Scripts"),
						new EmptyBorder(8,8,8,8)
						));
		
		/**
		 * selectDirPanel
		 */
		//selectDirPanel.setLayout(new FlowLayout());
		selectDirPanel.setLayout(new BoxLayout(selectDirPanel, BoxLayout.X_AXIS));
		//selectDirPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		selectDirPanel.setBorder(
				new CompoundBorder(
						new TitledBorder("Search Directory Path"),
						new EmptyBorder(8,8,8,8)));
		
		selectDirButton.addActionListener(this);
				

		selectDirPanel.add(targetDirTextField);		
		selectDirPanel.add(selectDirButton);
		
		targetDirTextField.setMaximumSize(new Dimension(2000,24));
		selectDirButton.setMaximumSize(new Dimension(24,24));
		
		/**
		 * getScenariosButtonPanel
		 */
		getScenariosButtonPanel.setLayout(new FlowLayout());
		
		mathcerStringComboBox.setEditable(true);

		getScenariosButtonPanel.add(new JLabel("Matching regex : "));
		getScenariosButtonPanel.add(mathcerStringComboBox);
		getScenariosButtonPanel.add(getScriptsButton);
		getScriptsButton.addActionListener(this);
		
		
		/**
		 * candScenarioListPanel
		 */
		
		candScenarioListPanel.setLayout(new BoxLayout(candScenarioListPanel, BoxLayout.Y_AXIS));
		
		String[] colNames = { "Directory Path", "Config Name"};

		candidateScriptsTableModel = new DefaultTableModel(colNames, 0){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Class<?> getColumnClass (int column){
				return getValueAt(0, column).getClass();
			}
		};
		
		candidateScriptsTable = new JTable(candidateScriptsTableModel);
		
		//candidateScenarioDirectoryTable.getColumnModel().getColumn(0).setMaxWidth(50);
		//candidateScenarioDirectoryTable.getColumnModel().getColumn(2).setPreferredWidth(120);
		
		JScrollPane sp = new JScrollPane(candidateScriptsTable);
		sp.setPreferredSize(new Dimension(500, 1000));
		candScenarioListPanel.add(sp);
		//candScenarioListPanel.add(clearAllButton);	
		
		/**
		 * editButtonPanel
		 */
		editButtonPanel.add( selectAllButton );
		editButtonPanel.add( deselectAllButton );
		editButtonPanel.add( invertSelectionButton );
		editButtonPanel.add( clearAllButton );
		editButtonPanel.add( clearSelectedButton );
		
		selectAllButton.addActionListener(this);
		deselectAllButton.addActionListener(this);
		invertSelectionButton.addActionListener(this);
		clearAllButton.addActionListener(this);
		clearSelectedButton.addActionListener(this);
		
		/**
		 * addButtonPanel
		 */
		addButtonPanel.setLayout(new BoxLayout(addButtonPanel, BoxLayout.X_AXIS));
		addButtonPanel.setBorder(new EmptyBorder(10,10,20,20));
		
		addSelectedScriptsButton.addActionListener(this);
		//addSelectedScenarioButton.setBackground(new Color(1.0f, 0.5f, 0.5f));		
	
		addButtonPanel.add(addSelectedScriptsButton);

	}
	
	/**
	 * @brief Parse arguments string and make array of each arguments 
	 * @param str Raw string including all of arguments
	 */
	public static ArrayList<String> parseArguments(String str){
		ArrayList<String> args = new ArrayList<String>();

		StringBuilder tmpStr = new StringBuilder();
		
		boolean inDQ = false;
		boolean escaped = false;
		for(int i=0;i < str.length();++i){
			if( str.charAt(i) == ' ' || str.charAt(i) == '	' ){
				if( escaped ){
					// Treat space as normal character
					tmpStr.append(str.charAt(i));
					escaped = false;
				}else if( inDQ ){
					// Treat space as normal character
					tmpStr.append(str.charAt(i));
				}else if( tmpStr.length() > 0){
					args.add(tmpStr.toString());
					tmpStr.delete(0, tmpStr.length());
					continue;
				}
			}else if( str.charAt(i) == '\\' ){
				if( escaped ){
					// backslash itself is escaped
					tmpStr.append(str.charAt(i));
					escaped = false;
				}else{
					escaped = true;
				}
			}else if( str.charAt(i) == '\"' ){
				if( escaped ){
					// double quotation is escaped. Treat it as normal character
					tmpStr.append(str.charAt(i));
					escaped = true;
				}else if( inDQ ){
					// End of strings placed between two double-quotations
					inDQ = false;
				}else{
					// Start of strings placed between two double-quotations
					inDQ = true;
				}
			}else{
				// Normal character
				
				if( escaped ){
					// Invalid escape. Ignore it.
					escaped = false;
				}
				
				tmpStr.append(str.charAt(i));	
			}
			
			// Here, tmpStr is not added to args in this loop.
			// If this is the last character, add tmpStr( if exist ) to args.
			if( i == str.length()-1 && tmpStr.length() > 0){
				args.add(tmpStr.toString());
				tmpStr.delete(0, tmpStr.length());				
			}
		}
		
		return args;
	}
	
	/**
	 * Get extension from filename
	 * @param fn File name
	 * @return Extension excluding period
	 */
	private String getExtension(String fn) {
	    int period = fn.lastIndexOf(".");
	    if (period != -1) {
	        return fn.substring(period + 1);
	    }
	    return fn;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if( e.getSource() == selectDirButton ){
		    JFileChooser filechooser = new JFileChooser(this.targetDirTextField.getText());
		    filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	
		    int selected = filechooser.showOpenDialog(this);
		    if (selected == JFileChooser.APPROVE_OPTION){
		      File file = filechooser.getSelectedFile();
		      targetDirTextField.setText(file.getAbsolutePath());
		    }
		}else if( e.getSource() == getScriptsButton ){
			ArrayList<ScriptProp> scriptsList = new ArrayList<ScriptProp>();
			String basePath = targetDirTextField.getText();
			String matcherString = (String)mathcerStringComboBox.getSelectedItem();
			this.searchFiles(Pattern.compile(matcherString), basePath, scriptsList);
			
			for(int i = 0;i < scriptsList.size(); ++i){
//				System.out.printf("[%2d] %s : %s\n", i,
//						scenarioDirectoryList.get(i).directoryPath,
//						scenarioDirectoryList.get(i).configName);
				
				candidateScriptsTableModel.addRow(
						new Object[]{
								scriptsList.get(i).directoryPath,
								scriptsList.get(i).configName
								}
						);
			}
			
			candidateScriptsTable.selectAll();
			
		}else if( e.getSource() == addSelectedScriptsButton ){
			JobQueueingTool owner = (JobQueueingTool)this.getOwner();
			
			for(int i = 0; i < candidateScriptsTableModel.getRowCount(); ++i){
				if( candidateScriptsTable.isRowSelected(i) ){
					String directoryPath = (String)candidateScriptsTableModel.getValueAt(i,scenarioDirColumn);
					String fileName = (String)candidateScriptsTableModel.getValueAt(i,configNameColumn);
					Hashtable<String, String> env = new Hashtable<String, String>();
					
					for(int envRow = 0; envRow < environmentalVariableTableModel.getRowCount(); ++envRow){
						String variable = (String) environmentalVariableTableModel.getValueAt(envRow, envVariableColumn);
						String value    = (String) environmentalVariableTableModel.getValueAt(envRow, envValueColumn);
						env.put(variable, value);
					}
					
					ArrayList<String> exePathList = ScriptExecutionRule.getInstance().getExPath(getExtension(fileName));
					if(exePathList == null)
					{
						exePathList = new ArrayList<String>();
						exePathList.add(fileName);
					}
					for(int k = 0; k < exePathList.size(); ++k)
					{
						String replacedStr = exePathList.get(k).replaceAll("\\$FILE_NAME", fileName);
						exePathList.set(k, replacedStr);
					}
					for(String s : exePathList)
					{
						System.out.println(s);
					}
					
					String exePath = exePathList.get(0);
					ArrayList<String> args = new ArrayList<String>();
					
					for(int k = 1; k < exePathList.size(); ++k)
					{
						args.add(exePathList.get(k));
					}
					args.addAll(parseArguments(argsTextField.getText()));
					

					int np = Integer.parseInt(npTextField.getText());					
					
					/*
					ArrayList<Job> parentJobList = new ArrayList<Job>();
					for(int r = 0; r < parentJobTableModel.getRowCount();++r){
						parentJobList.add(parentJobTableModel.get(r));
					}
					*/

					owner.addJob(directoryPath, args, env, exePath, np);
				}								
			}
			
			// If remove selected scenarios from candidates list, enable follwing lines.
			//for(int i = candidateScenarioDirectoryTableModel.getRowCount()-1; i >= 0; --i){
			//	if( candidateScenarioDirectoryTable.isRowSelected(i) ){
			//		candidateScenarioDirectoryTableModel.removeRow(i);
			//	}	
			//}
			
		}else if( e.getSource() == envHomesSelectDirButton ){
		    JFileChooser filechooser = new JFileChooser(envHomesTextField.getText());
		    filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	
		    int selected = filechooser.showOpenDialog(this);
		    if (selected == JFileChooser.APPROVE_OPTION){
		      File file = filechooser.getSelectedFile();
		      envHomesTextField.setText(file.getAbsolutePath());
		    }
		}else if( e.getSource() == exeSelectButton ){
		    JFileChooser filechooser = new JFileChooser(exeTextField.getText());
		    filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	
		    int selected = filechooser.showOpenDialog(this);
		    if (selected == JFileChooser.APPROVE_OPTION){
		      File file = filechooser.getSelectedFile();
		      exeTextField.setText(file.getAbsolutePath());
		    }		
		}else if( e.getSource() == selectAllButton ){
			candidateScriptsTable.selectAll();
		}else if( e.getSource() == deselectAllButton ){
			candidateScriptsTable.clearSelection();
		}else if( e.getSource() == invertSelectionButton ){
			for(int i = 0; i < candidateScriptsTable.getRowCount(); ++i){
				candidateScriptsTable.changeSelection(i,0,true,false);
			}
		}else if( e.getSource() == clearAllButton ){
			for(int i = candidateScriptsTableModel.getRowCount()-1; i >= 0; --i){
				candidateScriptsTableModel.removeRow(i);
			}			
			candidateScriptsTableModel.fireTableDataChanged();
		}else if( e.getSource() == clearSelectedButton ){
			for(int i = candidateScriptsTableModel.getRowCount()-1; i >= 0; --i){
				if( candidateScriptsTable.isRowSelected(i) ){
					candidateScriptsTableModel.removeRow(i);
				}
			}			
			candidateScriptsTableModel.fireTableDataChanged();
		}else if( e.getSource() == evAddButton ){
			environmentalVariableTableModel.addRow(new Object[]{"",""});
		}else if( e.getSource() == evRemoveButton ){
			for(int i = environmentalVariableTableModel.getRowCount()-1; i >= 0; --i){
				if( environmentalVariableTable.isRowSelected(i) ){
					environmentalVariableTableModel.removeRow(i);
				}
			}
		}
	}
}
