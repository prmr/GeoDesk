/*******************************************************************************
 * GeoDesk - Desktop application to view and edit geographic markers
 *
 *     Copyright (C) 2014 Martin P. Robillard, Jan Peter Stotz, and others
 *     
 *     See: http://martinrobillard.com/geodesk
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.openstreetmap.gui.jmapviewer.tiles;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * A generic class that processes a list of Runnable one-by-one using
 * one or more Thread-instances. The number of instances varies between
 * 1 and #WORKER_THREAD_MAX_COUNT (default: 8). If an instance is idle
 * more than #WORKER_THREAD_TIMEOUT seconds (default: 30), the instance
 * ends itself.
 *
 * @author Jan Peter Stotz
 */
public final class JobDispatcher 
{
    private static final JobDispatcher INSTANCE = new JobDispatcher();
    private static final int WORKER_THREAD_MAX_COUNT = 8;
    
    /**
     * Type of queue, FIFO if <code>false</code>, LIFO if <code>true</code>.
     */
    private static final boolean MODEL_LIFO = false;
    
    /**
     * Specifies the time span in seconds that a worker thread waits for new
     * jobs to perform. If the time span has elapsed the worker thread
     * terminates itself. Only the first worker thread works differently, it
     * ignores the timeout and will never terminate itself.
     */
    private static final int WORKER_THREAD_TIMEOUT = 30;

    private BlockingDeque<TileJob> aJobQueue = new LinkedBlockingDeque<TileJob>();
    
    /**
     * Total number of worker threads currently idle or active.
     */
    private int aWorkerThreadCount = 0;

    /**
     * Number of worker threads currently idle.
     */
    private int aWorkerThreadIdleCount = 0;

    /**
     * Just an id for identifying an worker thread instance.
     */
    private int aWorkerThreadId = 0;

    private JobDispatcher() 
    {
        addWorkerThread().aFirstThread = true;
    }
    
    /**
     * @return the singleton instance of the {@link JobDispatcher}
     */
    public static JobDispatcher getInstance() 
    {
        return INSTANCE;
    }    

    /**
     * Removes all jobs from the queue that are currently not being processed.
     */
    public void cancelOutstandingJobs()
    {
        aJobQueue.clear();
    }

    /**
     * Adds a job to the queue.
     * Jobs for tiles already contained in the are ignored (using a <code>null</code> tile
     * prevents skipping).
     *
     * @param pJob the the job to be added
     */
    public void addJob(TileJob pJob) 
    {
        try 
        {
            if(pJob.getTile() != null)
            {
                for(TileJob oldJob : aJobQueue) 
                {
                    if(oldJob.getTile() == pJob.getTile()) 
                    {
                        return;
                    }
                }
            }
            aJobQueue.put(pJob);
            if(aWorkerThreadIdleCount == 0 && aWorkerThreadCount < WORKER_THREAD_MAX_COUNT)
            {
                addWorkerThread();
            }
        } 
        catch (InterruptedException e) 
        {
        }
    }

    private JobThread addWorkerThread() 
    {
        JobThread jobThread = new JobThread(++aWorkerThreadId);
        synchronized(this) 
        {
            aWorkerThreadCount++;
        }
        jobThread.start();
        return jobThread;
    }

    private class JobThread extends Thread 
    {
        private Runnable aJob;
        private boolean aFirstThread = false;

        public JobThread(int pThreadId) 
        {
            super("OSMJobThread " + pThreadId);
            setDaemon(true);
            aJob = null;
        }

        @Override
        public void run() 
        {
            executeJobs();
            synchronized (INSTANCE) 
            {
                aWorkerThreadCount--;
            }
        }

        protected void executeJobs() 
        {
            while(!isInterrupted()) 
            {
                try 
                {
                    synchronized (INSTANCE)
                    {
                        aWorkerThreadIdleCount++;
                    }
                    if(MODEL_LIFO) 
                    {
                        if (aFirstThread)
                        {
                            aJob = aJobQueue.takeLast();
                        }
                        else
                        {
                            aJob = aJobQueue.pollLast(WORKER_THREAD_TIMEOUT, TimeUnit.SECONDS);
                        }
                    } 
                    else 
                    {
                        if(aFirstThread)
                        {
                            aJob = aJobQueue.take();
                        }
                        else
                        {
                            aJob = aJobQueue.poll(WORKER_THREAD_TIMEOUT, TimeUnit.SECONDS);
                        }
                    }
                } 
                catch (InterruptedException e1) 
                {
                    return;
                } 
                finally 
                {
                    synchronized (INSTANCE) 
                    {
                        aWorkerThreadIdleCount--;
                    }
                }
                if(aJob == null)
                {
                    return;
                }
                try 
                {
                    aJob.run();
                    aJob = null;
                } 
                catch(Exception e) 
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
