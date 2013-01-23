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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

interface JqtEvent
{
}

class JqtJobEvent implements JqtEvent
{
	JqtJobEvent(JqtJob relatedJob)
	{
		this.relatedJob = relatedJob;
	}
	
	public JqtJob relatedJob;
}

class JqtJobAddEvent extends JqtJobEvent
{
	JqtJobAddEvent(JqtJob relatedJob) {
		super(relatedJob);
	}	
}

class JqtJobRemoveEvent extends JqtJobEvent
{
	JqtJobRemoveEvent(JqtJob relatedJob) {
		super(relatedJob);
	}	
}

class JqtJobResetEvent extends JqtJobEvent
{
	JqtJobResetEvent(JqtJob relatedJob, SynchronousQueue<Boolean> rendezvousChannel) {
		super(relatedJob);
		this.rendezvousChannel = rendezvousChannel;
	}

	public SynchronousQueue<Boolean> rendezvousChannel;
}

class JqtJobStartEvent extends JqtJobEvent
{
	JqtJobStartEvent(JqtJob relatedJob) {
		super(relatedJob);
	}
}

class JqtJobTerminateEvent extends JqtJobEvent
{
	JqtJobTerminateEvent(JqtJob relatedJob, SynchronousQueue<Boolean> rendezvousChannel) {
		super(relatedJob);
		this.rendezvousChannel = rendezvousChannel;
	}

	public SynchronousQueue<Boolean> rendezvousChannel;
}

class JqtJobEndEvent extends JqtJobEvent
{
	int exitCode;
	
	JqtJobEndEvent(JqtJob relatedJob, int exitCode) {
		super(relatedJob);
		this.exitCode = exitCode;
	}
}

class JqtJobOutputEvent extends JqtJobEvent
{
	JqtJobOutputEvent(JqtJob relatedJob, String line) {
		super(relatedJob);
		this.line = line;
	}

	public String line;
}

class JqtJobProgressUpdateEvent extends JqtJobEvent
{
	JqtJobProgressUpdateEvent(JqtJob relatedJob, double progress) {
		super(relatedJob);
		this.progress = progress;
	}

	public double progress;
}

class JqtChangeNumProcessorsEvent implements JqtEvent
{
	JqtChangeNumProcessorsEvent(int numAvailableProcessors) {
		this.numProcessors = numAvailableProcessors;
	}

	public int numProcessors;
}

class JqtGetStatusEvent extends JqtJobEvent
{
	JqtGetStatusEvent(JqtJob relatedJob, SynchronousQueue<JqtJobStatus> rendezvousChannel) {
		super(relatedJob);
		this.rendezvousChannel = rendezvousChannel;
	}

	public SynchronousQueue<JqtJobStatus> rendezvousChannel;
}

class JqtGetStatusAllEvent implements JqtEvent
{
	JqtGetStatusAllEvent(SynchronousQueue<ArrayList<JqtJobInfo>> rendezvousChannel) {
		this.rendezvousChannel = rendezvousChannel;
	}

	public SynchronousQueue<ArrayList<JqtJobInfo>> rendezvousChannel;
}

class JqtGetEngineStatusEvent implements JqtEvent
{
	JqtGetEngineStatusEvent(SynchronousQueue<JqtEngine.JqtEngineStatus> rendezvousChannel) {
		this.rendezvousChannel = rendezvousChannel;
	}

	public SynchronousQueue<JqtEngine.JqtEngineStatus> rendezvousChannel;
}

class JqtPlayEvent implements JqtEvent
{
}

class JqtStopEvent implements JqtEvent
{
}

/**
 * Job context information used internally in the engine
 */
class JqtJobContext
{
	JqtJob relatedJob;
	JqtJobStatus jobStatus;
	JqtJobRunThread thread;
	
	JqtJobContext(JqtJob relatedJob)
	{
		this.relatedJob = relatedJob;
		this.jobStatus = new JqtJobStatus();
		thread = null;
	}
}

/**
 * Parse progress in Double from specified string
 */
class ProgressParser
{
	ArrayList<Pattern> patterns;
	private static ProgressParser instance = new ProgressParser();
	
	public static ProgressParser getInstance()
	{
		return instance;
	}
	
	private ProgressParser()
	{
		// TODO : Treat as configurable parameter ?
		patterns = new ArrayList<Pattern>();
		String regex = "Progress:(\\d+)";
		Pattern p = Pattern.compile(regex);
		patterns.add(p);
	}
	
	/**
	 * Get progress in Double from indicated string
	 * 
	 * @param line Input string
	 * @return Progress if line includes progress indicator otherwise null
	 */
	public Double getProgress(String line)
	{
		Double ret = null;
		for(Pattern p : patterns)
		{
			Matcher m = p.matcher(line);
			if(m.find())
			{
				ret = Double.parseDouble(m.group(1));
				break;
			}
		}
		return ret;
	}
}

/**
 * Thread class to run a Job 
 */
class JqtJobRunThread extends Thread{
	
	JqtJob job;
	JqtEngine engine;
	Process process;
	
	/**
	 * Constructor
	 * 
	 * @param job Job to run
	 */
	JqtJobRunThread(JqtJob job, JqtEngine engine){
		this.job = job;
		this.engine = engine;
		this.process = null;
	}
	
	/**
	 * Thread process
	 */
	public void run(){
		
		ProcessBuilder pb = null;
		
		ArrayList<String> command = new ArrayList<String>();
	
		command.add(job.exePath);
		command.addAll(job.firstArg);
		
		// Create process
		pb = new ProcessBuilder(command);
		//System.out.printf("Start Job : %s\n",command);
	    
	    pb.redirectErrorStream(true);
	    
	    pb.environment().putAll(job.env);	    
	    pb.directory(new File(job.directoryPath));
	    
	    try {
	        process = pb.start();
	        
	        InputStream is = process.getInputStream();
	        BufferedReader br = new BufferedReader(new InputStreamReader(is));
	        String line;
	        while ((line = br.readLine()) != null) {
	        	
	        	// Could affect another important events if too much stream output. 
	        	engine.pushEvent(new JqtJobOutputEvent(job, line));	        	
	        	
	        	if( line.length() == 0 ){
	        		continue;
	        	}
	        	Double progress = ProgressParser.getInstance().getProgress(line);
	        	if(progress != null)
	        	{
	        		engine.pushEvent(new JqtJobProgressUpdateEvent(job, progress));
	        	}
	        }
	        
	        int ret = process.waitFor();
	        process = null;
	        engine.pushEvent(new JqtJobEndEvent(job, ret));
	        
			//System.out.printf("End Job with exit code %d\n", ret);
	        
	    } catch (IOException e) {
	    	e.printStackTrace();
	    } catch (InterruptedException e) {
	    	e.printStackTrace();
	    }
	}
}

/**
 * Interfaces provided to external modules from engine
 */
interface JqtEngineInterface
{	
	/**
	 * Add job asynchronously
	 * This method is thread safe
	 * 
	 * @param job Job to add
	 */
	public void add(JqtJob job);

	/**
	 * Remove job asynchronously
	 * This method is thread safe
	 * 
	 * @param job Job to remove
	 */
	public void remove(JqtJob job);

	/**
	 * Reset job status synchronously
	 * This method is thread safe
	 * 
	 * @param job Job to reset
	 */
	public boolean reset(JqtJob job);
	
	/**
	 * Get job status synchronously
	 * This method is thread safe
	 * 
	 * @param job Job to get status
	 * @return Clone of the status. If job does not exist, null is returned.
	 */
	public JqtJobInfo getJobInfo(JqtJob job);
	
	/**
	 * Get job status list synchronously
	 * This method is thread safe
	 * 
	 * @return List of job status.
	 */
	public ArrayList<JqtJobInfo> getJobInfo();
	
	/**
	 * Change number of available processors
	 * This method is thread safe
	 * 
	 * @param numProcessors Number of available processors
	 */
	public void changeProcessors(int numProcessors);
	
	/**
	 * Get engine status synchronously
	 * 
	 * @return IDLE : Idling, PLAY : Playing
	 */
	public JqtEngine.JqtEngineStatus getStatus();
	
	/**
	 * Terminate job synchronously
	 * 
	 * @param job to terminate
	 * @return true if terminated, otherwise false
	 */
	public boolean terminate(JqtJob job);
	
	/**
	 * Play engine
	 */
	public void playEngine();
	
	/**
	 * Stop engine
	 */
	public void stopEngine();
}


/**
 * Job execution engine
 */
public class JqtEngine extends Thread implements JqtEngineInterface
{
	/**
	 * Engine status
	 */
	public enum JqtEngineStatus
	{
		IDLE,
		PLAYING
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
	
	public static OSType osType = getOsType();
	
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
	
	int numAvailableProcessors;
	int numUsedProcessors;
	
	LinkedHashMap<JqtJob, JqtJobContext> jobContextList = new LinkedHashMap<JqtJob, JqtJobContext>();
	LinkedBlockingQueue<JqtEvent> eventQueue = new LinkedBlockingQueue<JqtEvent>();
	
	JqtEngineStatus engineStatus;
	
	String crlf = System.getProperty("line.separator");
	
	/**
	 * Constructor 
	 * 
	 * @param numAvailableProcessors Number of available processors
	 */
	public JqtEngine(int numAvailableProcessors)
	{	
		this.numAvailableProcessors = numAvailableProcessors;
		this.numUsedProcessors = 0;
		this.engineStatus = JqtEngineStatus.IDLE;
	}
	
	/**
	 * Push event
	 * This method is thread safe
	 * 
	 * @param event Event to be pushed
	 * @throws InterruptedException 
	 */
	public void pushEvent(JqtEvent event) throws InterruptedException
	{
		eventQueue.put(event);
	}
	
	/**
	 * Add job asynchronously
	 * This method is thread safe
	 * 
	 * @param job Job to add
	 */
	@Override
	public void add(JqtJob job)
	{
		eventQueue.add(new JqtJobAddEvent(job));
	}
	
	/**
	 * Terminate job synchronously
	 * This method is thread safe
	 * 
	 * @param job Job to terminate
	 * @return true if terminated, otherwise false
	 */
	@Override
	public boolean terminate(JqtJob job)
	{	
		// TODO Auto-generated method stub
		SynchronousQueue<Boolean> rendezvousChannel
			= new SynchronousQueue<Boolean>();
		eventQueue.add(new JqtJobTerminateEvent(job, rendezvousChannel));
		
		boolean ret = false;
		try {
			ret = rendezvousChannel.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * Remove job asynchronously
	 * This method is thread safe
	 * 
	 * @param job Job to remove
	 */
	@Override
	public void remove(JqtJob job)
	{
		eventQueue.add(new JqtJobRemoveEvent(job));
	}
	
	/**
	 * Reset job status
	 * This method is thread safe
	 * 
	 * @param job Job to reset
	 * @return true if job is reset, otherwise false.
	 */
	public boolean reset(JqtJob job)
	{
		// TODO Auto-generated method stub
		SynchronousQueue<Boolean> rendezvousChannel
			= new SynchronousQueue<Boolean>();
		eventQueue.add(new JqtJobResetEvent(job, rendezvousChannel));
		
		boolean ret = false;
		try {
			ret = rendezvousChannel.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
	
	/**
	 * Change number of available processors
	 * This method is thread safe
	 * 
	 * @param numProcessors Number of available processors
	 */
	@Override
	public void changeProcessors(int numProcessors)
	{
		eventQueue.add(new JqtChangeNumProcessorsEvent(numProcessors));
	}
	
	/**
	 * Get job status synchronously
	 * This method is thread safe
	 * 
	 * @param job Job to get context
	 * @return Clone of the context. If job does not exist, null is returned.
	 */
	@Override
	public JqtJobInfo getJobInfo(JqtJob job) {
		// TODO Auto-generated method stub
		SynchronousQueue<JqtJobStatus> rendezvousChannel
			= new SynchronousQueue<JqtJobStatus>();
		eventQueue.add(new JqtGetStatusEvent(job, rendezvousChannel));
		
		JqtJobInfo jobInfo = new JqtJobInfo();
		try {
			jobInfo.relatedJob = job;
			jobInfo.jobStatus = rendezvousChannel.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jobInfo;
	}
	
	/**
	 * Get job status synchronously
	 * This method is thread safe
	 * 
	 * @param job Job to get context
	 * @return Clone of the context. If job does not exist, null is returned.
	 */
	@Override
	public ArrayList<JqtJobInfo> getJobInfo() {
		// TODO Auto-generated method stub
		SynchronousQueue<ArrayList<JqtJobInfo>> rendezvousChannel
			= new SynchronousQueue<ArrayList<JqtJobInfo>>();
		eventQueue.add(new JqtGetStatusAllEvent(rendezvousChannel));
		
		ArrayList<JqtJobInfo> infoList = null;
		try {
			infoList = rendezvousChannel.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return infoList;
	}
	
	/**
	 * Get job status synchronously
	 * This method is thread safe
	 * 
	 * @param job Job to get context
	 * @return Clone of the context. If job does not exist, null is returned.
	 */
	@Override
	public JqtEngineStatus getStatus() {
		// TODO Auto-generated method stub
		SynchronousQueue<JqtEngineStatus> rendezvousChannel
			= new SynchronousQueue<JqtEngineStatus>();
		eventQueue.add(new JqtGetEngineStatusEvent(rendezvousChannel));
		
		JqtEngineStatus engineStatus = null;
		try {
			engineStatus = rendezvousChannel.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return engineStatus;
	}
	
	@Override
	public void playEngine() {
		// TODO Auto-generated method stub
		eventQueue.add(new JqtPlayEvent());
	}
	
	@Override
	public void stopEngine() {
		// TODO Auto-generated method stub
		eventQueue.add(new JqtStopEvent());
	}
	
	/**
	 * Invoke jobs
	 * 
	 * This method must be called only from event dispatch thread
	 */
	private void invokeJob()
	{
		if(this.engineStatus == JqtEngineStatus.IDLE)
		{
			return;
		}
		
		for(JqtJobContext context : jobContextList.values())
		{
			if(context.jobStatus.status == JqtJobStatus.WAITING)
			{
				if(numUsedProcessors + context.relatedJob.np <= numAvailableProcessors)
				{
					numUsedProcessors += context.relatedJob.np;
					context.jobStatus.status = JqtJobStatus.RUNNING;
					context.jobStatus.jobStartDate = System.currentTimeMillis();
					context.thread = new JqtJobRunThread(context.relatedJob, this);
					context.thread.start();
				}
			}
		}
	}
	
	/**
	 * Event dispatch loop
	 * @throws InterruptedException 
	 */
	public void run()
	{
		while(true)
		{
			// All process is serialized
			
			JqtEvent event = null;
			try {
				event = eventQueue.take();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			if(event instanceof JqtJobAddEvent)
			{
				JqtJobAddEvent e = (JqtJobAddEvent)event;
				jobContextList.put(e.relatedJob, new JqtJobContext(e.relatedJob));
				
				invokeJob();
			}
			else if(event instanceof JqtJobRemoveEvent)
			{
				JqtJobRemoveEvent e = (JqtJobRemoveEvent)event;
				jobContextList.remove(e.relatedJob);
			}
			else if(event instanceof JqtJobResetEvent)
			{
				JqtJobResetEvent e = (JqtJobResetEvent)event;
				JqtJobContext context = jobContextList.get(e.relatedJob);
				
				Boolean ret = false;
				
				if(context.jobStatus.status == JqtJobStatus.COMPLETE ||
				   context.jobStatus.status == JqtJobStatus.ERROR)
				{
					context.jobStatus.reset();
					ret = true;
				}
				try{
					e.rendezvousChannel.put(ret);
				}catch(Exception e1)
				{
					e1.printStackTrace();
				}
				
				invokeJob();
			}
			else if(event instanceof JqtJobOutputEvent)
			{
				JqtJobOutputEvent e = (JqtJobOutputEvent)event;
				
				JqtJobContext context = jobContextList.get(e.relatedJob);
				if(context != null)
				{
					context.jobStatus.stdout += (e.line + this.crlf);
				}
			}
			else if(event instanceof JqtJobProgressUpdateEvent)
			{
				JqtJobProgressUpdateEvent e = (JqtJobProgressUpdateEvent)event;
				
				JqtJobContext context = jobContextList.get(e.relatedJob);
				if(context != null)
				{
					context.jobStatus.progress = e.progress;
				}
				
			}else if(event instanceof JqtJobEndEvent)
			{
				JqtJobEndEvent e = (JqtJobEndEvent)event;
				
				JqtJobContext context = jobContextList.get(e.relatedJob);
				
				if(context != null)
				{
					context.jobStatus.status =
							e.exitCode == 0 ? JqtJobStatus.COMPLETE : JqtJobStatus.ERROR;
					context.jobStatus.jobEndDate = System.currentTimeMillis();
					numUsedProcessors -= e.relatedJob.np;
				}
				
				invokeJob();
			}else if(event instanceof JqtChangeNumProcessorsEvent)
			{
				JqtChangeNumProcessorsEvent e = (JqtChangeNumProcessorsEvent)event;
				numAvailableProcessors = e.numProcessors;
				
				invokeJob();
			}else if(event instanceof JqtGetStatusEvent)
			{
				JqtGetStatusEvent e = (JqtGetStatusEvent)event;
				JqtJobContext context = jobContextList.get(e.relatedJob);
				try{
					if(context != null)
					{
						e.rendezvousChannel.put(context.jobStatus.clone());
					}else
					{
						e.rendezvousChannel.put(null);
					}
				}catch(Exception e1)
				{
					e1.printStackTrace();
				}
			}else if(event instanceof JqtGetStatusAllEvent)
			{
				JqtGetStatusAllEvent e = (JqtGetStatusAllEvent)event;
				ArrayList<JqtJobInfo> statusList = new ArrayList<JqtJobInfo>();
				for(JqtJobContext context : jobContextList.values())
				{
					JqtJobInfo info = new JqtJobInfo();
					info.relatedJob = context.relatedJob;
					info.jobStatus = context.jobStatus.clone(); 
					statusList.add(info);
				}
				try{
					e.rendezvousChannel.put(statusList);
				}catch(Exception e1)
				{
					e1.printStackTrace();
				}
			}else if(event instanceof JqtGetEngineStatusEvent)
			{
				JqtGetEngineStatusEvent e = (JqtGetEngineStatusEvent)event;
				try{
					e.rendezvousChannel.put(engineStatus);
				}catch(Exception e1)
				{
					e1.printStackTrace();
				}
			}else if(event instanceof JqtPlayEvent)
			{
				this.engineStatus = JqtEngineStatus.PLAYING;
				invokeJob();
			}else if(event instanceof JqtStopEvent)
			{
				this.engineStatus = JqtEngineStatus.IDLE;
			}else if(event instanceof JqtJobTerminateEvent)
			{
				JqtJobTerminateEvent e = (JqtJobTerminateEvent)event;
				JqtJobContext context = jobContextList.get(e.relatedJob);
				System.out.println("Terminating job " + e.relatedJob + ":" + context);
				Boolean ret = false;
				if(context.thread != null && context.thread.process != null)
				{
					context.thread.process.destroy();	
					ret = true;
				}
				try{
					e.rendezvousChannel.put(ret);
				}catch(Exception e1)
				{
					e1.printStackTrace();
				}
				
				invokeJob();
			}
		}
	}
	
	public static String timeString(long timeCollapsed)
	{
		//System.out.println(timeCollapsed);
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
	
	public static void main(String arg[])
	{

		JqtEngine engine = new JqtEngine(7);
		
		
		ArrayList<JqtJob> jobList = new ArrayList<JqtJob>();
		
		for(int i = 0; i < 40; ++i)
		{
			ArrayList<String> jobArg = new ArrayList<String>();
			JqtJob job = new JqtJob(".",
							jobArg,
							new Hashtable<String,String>(),
							"./a.sh", 1);
			jobList.add(job);
			engine.add(job);
		}
		
		engine.start();
		
		engine.playEngine();
		
		for(int i = 0; i < 1000000; ++i)
		{
			System.out.print("\033[2J"); 
			System.out.print("\033[1;1H");
			System.out.println("------ Job status ------");
			ArrayList<JqtJobInfo> infoList = engine.getJobInfo();
			/*for(JqtJob job: jobList)
			{
				JqtJobStatus jobStatus = engine.getJobStatus(job);
				System.out.println(jobStatus.progress);
			}*/
			for(JqtJobInfo jobInfo : infoList)
			{
				String ps = "";
				for(int p = 1; p <= (int)(jobInfo.jobStatus.progress); ++p)
				{
					ps += "|";
				}
				
				String ellapsedTimeStr = "-";
				
				if(jobInfo.jobStatus.status == JqtJobStatus.RUNNING)
				{
					long ellapsedTime = System.currentTimeMillis() - jobInfo.jobStatus.jobStartDate;
					ellapsedTimeStr = timeString(ellapsedTime);
				}else{
					ellapsedTimeStr = timeString(jobInfo.jobStatus.jobEndDate - jobInfo.jobStatus.jobStartDate);	
				}
				
				System.out.printf("%5.1f%% %10s %10s %s\n",
						jobInfo.jobStatus.progress, jobInfo.jobStatus.status, 
						ellapsedTimeStr, ps);
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			engine.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
