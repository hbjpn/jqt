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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;	

public class JobsTransfer implements Transferable, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1024747476176103975L;
	private ArrayList<JobTableElement> jobList;
	JobsTransfer(ArrayList<JobTableElement> jobList) {
		this.jobList = jobList;
	}
	
	public ArrayList<JobTableElement> getJobList(){
		return this.jobList;
	}


	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		// TODO Auto-generated method stub
	    DataFlavor[] f = new DataFlavor[1];
	    f[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType,"JobsTransfer");
	    return f;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		// TODO Auto-generated method stub
		return flavor.getHumanPresentableName().equals("JobsTransfer");
	}
}
