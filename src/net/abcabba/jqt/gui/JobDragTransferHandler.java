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

import java.awt.datatransfer.Transferable;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;


public class JobDragTransferHandler extends TransferHandler {
	public int getSourceActions(JComponent c){
		System.out.println("getSourceActions called.");
		return COPY;
	}
	
	@Override
	protected Transferable createTransferable(JComponent c) {
		System.out.println("createTransferable called.");
		JTable table = (JTable) c;
		JobTableModel model = (JobTableModel)table.getModel();
		
		ArrayList<JobTableElement> jobList = new ArrayList<JobTableElement>();
		int[] selRows = table.getSelectedRows();
		System.out.println("selRows = ");
		for(int i = 0; i < selRows.length; ++i){
			System.out.println(selRows[i]);
		}
		for(int i = 0; i < selRows.length; ++i){
			int r = selRows[i];
			
			// return first job
			jobList.add(model.get(r));
		}
		
		
		return ( new JobsTransfer(jobList) );
		
	}
}
