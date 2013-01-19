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
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;


class StripeTableRenderer extends DefaultTableCellRenderer { 

	private static final long serialVersionUID = 1L;

	private static final Color evenColor = new Color(245, 245, 250);
	
	//private static final Paint evenRowPaint
	//	= new GradientPaint(0, 20, evenColor, 0, 0, new Color(255, 255, 255));

   private static final Paint paint 
       = new GradientPaint(0, 20, new Color(51, 204, 255), 0, 0, new Color(251, 255, 255));
   
   private static final Paint paint_complete
   = new GradientPaint(0, 20, new Color(0, 255, 0), 0, 0, new Color(200,255,200));
   
   int currentColumn;
   int currentRow;
   boolean curretntIsSelected;
   Object currentValue;
   JTable ownerTable;

   public Component getTableCellRendererComponent(JTable table, Object value, 
	                           boolean isSelected, boolean hasFocus, 
	                           int row, int column) {
	   ownerTable = table;
	   currentColumn = column;
	   currentRow = row;
	   curretntIsSelected = isSelected;
	   currentValue = value;
	
	   super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	   
	   //System.out.print("{"+row+","+column+"}");
	   
	   if( column == JobTableModel.PROGRESS_COLUMN || column == JobTableModel.ELLAPSED_COLUMN){
		   // Delegate to paintComponent function
		   
		   setHorizontalAlignment(RIGHT);
		   
	   }else{
		
		   if(isSelected) { 
			   //setForeground(table.getSelectionForeground()); 
			   //setBackground(table.getSelectionBackground()); 
		   }else{ 
			   //setForeground(table.getForeground()); 
			   //setBackground((row%2==0)?evenColor:table.getBackground());
			   //setBackground(table.getBackground());
		   } 
		   
		   setHorizontalAlignment(LEFT);
	   }
	   
	   return this; 

	   //this.get
	  } 
	  
	 protected void paintComponent(Graphics g) {
		 
		 //System.out.println("paintComponent called");
		 
		 Graphics2D g2 = (Graphics2D) g;
		 
	     if( currentRow % 2 == 0 ){
	    	 g2.setPaint(evenColor);
	    	 g.fillRect(0, 0, getWidth(),getHeight());
	     }
	     
		 
		 if( currentColumn == JobTableModel.PROGRESS_COLUMN ){
		 
			 String str = (String)this.currentValue;
			 String[] vals = str.split(" ",-1);
			 Double value = Double.parseDouble((vals[0].substring(0, vals[0].length()-1)));
			 double max = 100.0;
			 
		     if(value==null){		    	 
		         super.paintComponent(g);
		         return;
		     }
	     
		     if( value < 100.0 ){
		    	 g2.setPaint(paint);
		     }else{
		    	 g2.setPaint(paint_complete);
		     }
		     
		     double w=value/max*getWidth();
		     
		     g.fillRect(1, 1, (int)w-1,getHeight()-2);
		     
		 }else{
			 //Graphics2D g2 = (Graphics2D) g;
				 
			 //g2.setPaint(paint);
			 //g.fillRect(1, 1, getWidth()-1,getHeight()-2);
		 }
		 
	     super.paintComponent(g);
	 }
} 

class JobTableModel extends AbstractTableModel{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int JOBID_COLUMN = 0;
	public static final int STATUS_COLUMN = 1;
	public static final int SCENARIO_DIRECTORY_COLUMN = 2;
	public static final int EXECUTION_FILE_COLUMN = 3;
	public static final int CONFIG_NAME_COLUMN = 4;
	public static final int NP_COLUMN = 5;
	public static final int PARENT_COLUMN = 6;
	public static final int ELLAPSED_COLUMN = 7;
	public static final int PROGRESS_COLUMN = 8;


	private String[] columnNames = { "#", "Status", "Directory",
        "Execution file", "Arguments", "Processors", "Parent #","Time ellapsed","Progress", };
    
    private ArrayList<JobTableElement> jobList = new ArrayList<JobTableElement>();

    public int getColumnCount() {
      return columnNames.length;
    }

    public int getRowCount() {
      return jobList.size();
    }

    public String getColumnName(int col) {
      return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
      //return data[row][col];
    	JobTableElement jobTableElement = jobList.get(row);
    	JqtJobInfo jobInfo = jobTableElement.getJobInfo();
    	JqtJob job = jobInfo.relatedJob;
    	if( col == JOBID_COLUMN ){
    		return Integer.toString(jobTableElement.getJobId());
    	}else if( col == STATUS_COLUMN){
    		return jobInfo.jobStatus.status;
    	}else if( col == SCENARIO_DIRECTORY_COLUMN){
    		return job.directoryPath;
    	}else if( col == EXECUTION_FILE_COLUMN){
    		return job.exePath;
    	}else if( col == CONFIG_NAME_COLUMN){
    		return job.firstArg;
    	}else if( col == NP_COLUMN){
    		return Integer.toString(job.np);
    	}else if( col == PARENT_COLUMN ){
    		return "-";
    		/*
    		if( job.parentJobList.size() == 0 ){
    			return "-";
    		}
    		StringBuffer buf = new StringBuffer();
    		for(Job parentJob : job.parentJobList ){
    			buf.append(Integer.toString(parentJob.jobId)+",");
    		}
    		return buf.toString();
    		*/
    	}else if( col == PROGRESS_COLUMN){
    		//return job.getProgress();
    		//return job.getStdoutsTail();
    		return Double.toString(jobInfo.jobStatus.progress) + "%";
    	}else if( col == ELLAPSED_COLUMN){
    		if( jobInfo.jobStatus.jobStartDate < 0 ){
    			return new String(" - ");
    		}
    		
    		long t;
    		
    		if( jobInfo.jobStatus.status == JqtJobStatus.RUNNING){
    			t = System.currentTimeMillis();
    		}else{
    			t = jobInfo.jobStatus.jobEndDate;
    		}
    		
    		
    		long timeCollapsed = t - jobInfo.jobStatus.jobStartDate;
    		long tmp = timeCollapsed;
    		long hour = tmp / (3600 * 1000);
    		tmp -= hour * 3600 * 1000;
    		long minu = tmp / (60 * 1000);
    		tmp -= minu * 60 * 1000;
    		long sec = tmp / 1000;
    		tmp -= sec * 1000;
    		long msec = tmp;
    		
    		return String.format("%d:%02d:%02d ", hour, minu, sec, msec);
    	}
    	
    	return null;
    }
    
    public JobTableElement get(int row){
    	return jobList.get(row);
    }
    
    public int getRow(JobTableElement element){
    	return jobList.indexOf(element);
    }

    /*
     * JTable uses this method to determine the default renderer/ editor for
     * each cell. If we didn't implement this method, then the last column
     * would contain text ("true"/"false"), rather than a check box.
     */
    public Class getColumnClass(int c) {
      return String.class;
      //return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's editable.
     */
    public boolean isCellEditable(int row, int col) {
      //Note that the data/cell address is constant,
      //no matter where the cell appears onscreen.
      return false;
    }

    /*
     * Don't need to implement this method unless your table's data can
     * change.
     */
    public void setValueAt(Object value, int row, int col) {
        System.out.println("Setting value at " + row + "," + col
            + " to " + value + " (an instance of "
            + value.getClass() + ")");
        
       JobTableElement element = jobList.get(row);
       if( col == 0 ){
    	   element.setSelected((Boolean)value);
       }
      
       fireTableCellUpdated(row, col);

       System.out.println("New value of data:");
       printDebugData();
    }
    
    public void addRow(JobTableElement element){
    	//data.
    	System.out.println("Job added : " + element.getJobInfo().jobStatus.status);
    	
    	jobList.add(element);
    	
    	//this.fireTableRowsUpdated(jobList.size()-1, jobList.size()-1);
    	fireTableDataChanged();
    }
    
    public void removeAll()
    {
    	jobList.clear();
    	fireTableDataChanged();
    }
    
    public void removeRows(int[] targetRemovedRows){
    	//data.
    	System.out.println("Remove jobs");
    	
    	// Sort indices of target removed rows in ascending
    	java.util.Arrays.sort(targetRemovedRows);
    	
    	//targetRemovedRows
		for(int i = targetRemovedRows.length-1; i >= 0; --i){
			// Remove ones which are not running
			int index = targetRemovedRows[i];
			//if( jobList.get(index).getStatus() != Job.statusRunning ){
				jobList.remove(index);
			//}
		}
    	//this.fireTableRowsUpdated(jobList.size()-1, jobList.size()-1);
    	fireTableDataChanged();
    }
    
    public int getStatusCount(String status){
    	
    	return 0;
    }

    private void printDebugData() {

    }

	public void jobStatusUpdated() {
		// TODO Auto-generated method stub
		fireTableDataChanged();
		
		/*
		int row = this.getRow((Job)e.getSource());
		if( row >= 0 ){
			if( e.getKind() == JobStatusUpdateEvent.PROGRESS_UPDATE ){
				fireTableCellUpdated(row, PROGRESS_COLUMN);
				fireTableCellUpdated(row, ELLAPSED_COLUMN);
			}else if( e.getKind() == JobStatusUpdateEvent.START_JOB ){
				fireTableCellUpdated(row, STATUS_COLUMN);
				fireTableCellUpdated(row, ELLAPSED_COLUMN);
			}else if( e.getKind() == JobStatusUpdateEvent.PERIODIC_TIMER){
				fireTableCellUpdated(row, ELLAPSED_COLUMN);
			}else if( e.getKind() == JobStatusUpdateEvent.COMPLETE_JOB ){
				fireTableCellUpdated(row, STATUS_COLUMN);
			}else if( e.getKind() == JobStatusUpdateEvent.COMPLETE_JOB_WITH_ERROR ){
				fireTableCellUpdated(row, STATUS_COLUMN);
			}
		}
		*/
	}
  }