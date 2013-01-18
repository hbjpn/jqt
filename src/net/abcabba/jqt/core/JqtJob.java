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

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;

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
	

    public Node toXml(Document document) {	
	
    	Element jobElement = null;
	
		jobElement = document.createElement("job");

		{
			Element e = document.createElement("directoryPath");
			e.appendChild(document.createTextNode(this.directoryPath));
			jobElement.appendChild(e);
		}
		
		{
			Element e = document.createElement("exePath");
			e.appendChild(document.createTextNode(this.exePath));
			jobElement.appendChild(e);
		}
 
		jobElement.appendChild(document.createElement("firstArg"));
		for (String arg : firstArg)
		{
			Element e = document.createElement("arg");
			e.appendChild(document.createTextNode(arg));
			jobElement.getLastChild().appendChild(e);
		}
		
		jobElement.appendChild(document.createElement("env"));
		for(String key : this.env.keySet())
		{
			String value = this.env.get(key);
			Element e = document.createElement("entry");
			e.appendChild(document.createElement("key"));
			e.getLastChild().appendChild(document.createTextNode(key));
			e.appendChild(document.createElement("value"));
			e.getLastChild().appendChild(document.createTextNode(value));
			jobElement.getLastChild().appendChild(e);
		}
		
		jobElement.appendChild(document.createElement("np"));
		{
			Node e = jobElement.getLastChild();
			e.appendChild(document.createTextNode(Integer.toString(np)));	
		}
		
		return jobElement;
    }
    
	public static ArrayList<JqtJob> fromXml(String filePath)
	{
		ArrayList<JqtJob> jobs = new ArrayList<JqtJob>();
		
		File fileObject = new File(filePath);
		try{
			DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = docBuilder.parse(fileObject);
			
			NodeList jobsNodes = document.getElementsByTagName("jobs");
			Node jobsNode = jobsNodes.item(0);
			NodeList nodeList = jobsNode.getChildNodes();
			for(int i = 0; i < nodeList.getLength(); ++i)
			{
				Node jobNode = nodeList.item(i);
				System.out.println("----" + jobNode.getNodeName() + ":" + jobNode.getNodeValue());
				NodeList childNodes = jobNode.getChildNodes();
				
				Integer np = null;
				String directoryPath = null;
				String exePath = null;
				ArrayList<String> args = new ArrayList<String>();
				Hashtable<String, String> env = new Hashtable<String, String>();
				
				for(int j = 0; j < childNodes.getLength(); ++j)
				{	
					Node node = childNodes.item(j);
					System.out.println("nodeName:"+node.getNodeName());
					switch(node.getNodeName())
					{
					case "directoryPath":
						directoryPath = node.getFirstChild().getNodeValue();
						break;						
					case "exePath":
						exePath = node.getFirstChild().getNodeValue();
						break;
					case "firstArg":
						NodeList argsNode = node.getChildNodes();
						for(int ai = 0; ai < argsNode.getLength(); ++ai)
						{
							args.add(argsNode.item(ai).getFirstChild().getNodeValue());
						}
						break;
					case "env":
						NodeList entryNodes = node.getChildNodes();
						for(int ei = 0; ei < entryNodes.getLength(); ++ei)
						{
							NodeList entryContent = entryNodes.item(ei).getChildNodes();
							String key = null, value = null;
							for(int entryContentIdx = 0; entryContentIdx < entryContent.getLength(); ++entryContentIdx)
							{
								Node en = entryContent.item(entryContentIdx);
								switch(en.getNodeName())
								{
									case "key":
										key = en.getFirstChild().getNodeValue();
										break;
									case "value":
										value = en.getFirstChild().getNodeValue();
										break;
										
								}
							}
							env.put(key,  value);
							
						}
						break;
					case "np":
						np = Integer.parseInt(node.getFirstChild().getNodeValue());
						break;
					default:
						break;
					}
				}
				
				//node.getElementByTagName("np");
				System.out.println(
						directoryPath + "," +
						args + "," +
						env + "," +
						exePath + "," +
						np);
				JqtJob job = new JqtJob(directoryPath, args, env, exePath, np);
				jobs.add(job);
				
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return jobs;
	}
}