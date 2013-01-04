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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JTable;
import javax.swing.TransferHandler;


public class JobDropTransferHandler extends TransferHandler{
	@Override
	public boolean canImport(TransferSupport support) {
		return true;		
	}
	
	@Override
	public boolean importData(TransferSupport support) {
		System.out.println("importData called.");
		JTable table = (JTable) support.getComponent();
		JobTableModel model = (JobTableModel)table.getModel();
		
		if (support.isDrop()) {
			int action = support.getDropAction();
			System.out.println("is drop. action = "+ action);
			if (action == COPY || action == MOVE || action == LINK) {
				System.out.println("in");
				Transferable t = support.getTransferable();
				JobsTransfer jobsTransfer = null;
				try {
					jobsTransfer = (JobsTransfer) t.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "JobsTransfer"));
				} catch (UnsupportedFlavorException e) {
					System.out.println("Unsupported Flavor");
					return false;
				} catch (IOException e) {
					System.out.println("IO exception");
					return false;
				} catch (NullPointerException e){
					System.out.println("nurupo");
					return false;
				}catch (Exception e){
					System.out.println("Exception");
					return false;
				} finally{
					System.out.println("finally");
				}
				
				if(jobsTransfer == null){
					System.out.println("addRow : jobsTransfer is null");
				}else{
					for( JobTableElement element : jobsTransfer.getJobList() ){
						if( model.getRow(element) == -1 ){
							model.addRow(element);
						}
					}
				}

			}
			return true;
		}
		return false;
	}
}
