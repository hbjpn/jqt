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

import java.util.ArrayList;
import java.util.Hashtable;

public class JqtJob
{
	/**
	 * All fields are read-only
	 */
	public final String directoryPath;
	public final ArrayList<String> firstArg;
	public final Hashtable<String, String> env;
	public final String exePath;
	public final int np;

	public JqtJob(
			String directoryPath,
			ArrayList<String> firstArg,
			Hashtable<String, String> env,
			String exePath,
			int np)
	{
		this.directoryPath = directoryPath;
		this.firstArg = firstArg;
		this.env = env;
		this.exePath = exePath;
		this.np = np;
	}
}