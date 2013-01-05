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

package net.abcabba.jqt.core;

/**
 * Job status information
 */
public class JqtJobStatus
{
	public static String WAITING = "WAITING";
	public static String RUNNING = "RUNNING";
	public static String COMPLETE = "COMPLETE";
	public static String ERROR = "ERROR";
	
	public JqtJobStatus()
	{
		reset();
	}
	
	public void reset()
	{
		this.progress = 0.0;
		this.status = WAITING;
		this.jobStartDate = -1;
		this.jobEndDate = -1;
	}
	
	public JqtJobStatus clone()
	{
		JqtJobStatus obj = new JqtJobStatus();
		obj.progress = progress;
		obj.status = status;
		obj.jobStartDate = jobStartDate;
		obj.jobEndDate = jobEndDate;
		return obj;
	}
	public double progress;
	public String status;	
	public long jobStartDate;
	public long jobEndDate;

}




