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

/**
 * Job table element
 */
public class JobTableElement{

	private JqtJobInfo jobInfo;
	private int jobId;
	
	public int getJobId() {
		return jobId;
	}

	public Boolean getSelected() {
		return selected;
	}

	public void setSelected(Boolean selected) {
		this.selected = selected;
	}

	public JqtJobInfo getJobInfo() {
		return jobInfo;
	}

	private Boolean selected;
	
	public JobTableElement(JqtJobInfo jobInfo, int jobId) {
		this.jobInfo = jobInfo;
		this.jobId = jobId;
		this.selected = false;
	}
}
