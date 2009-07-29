/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ExpiredEntryMonitor.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.recovery;

import java.text.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import com.arjuna.ats.arjuna.recovery.ExpiryScanner;
import com.arjuna.ats.arjuna.recovery.RecoveryEnvironment;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.common.util.logging.*;

/**
 * Threaded object to run {@link ExpiryScanner} implementations to scan 
 * the action store to remove items deemed expired by some algorithm.
 * Performs a scan at interval defined by the property 
 * com.arjuna.ats.arjuna.recovery.expiryScanInterval (hours).
 * ExpiryScanner implementations are registered as properties beginning with
 * "com.arjuna.ats.arjuna.recovery.expiryScanner".
 * <P>
 * Singleton, instantiated in the RecoveryManager. 
 * <P>
 *
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_1 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_1] - Expiry scan interval set to {0} seconds
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_2 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_2] - Expiry scan zero - not scanning
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_3 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_3] - No Expiry scanners loaded - not scanning
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_4 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_4] - ExpiredEntryMonitor - constructed
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_5 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_5] - ExpiredEntryMonitor - no scans on first iteration
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_6 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_6] - Loading expiry scanner {0}
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_7 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_7] - Attempt to load expiry scanner with null class name!
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_8 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_8] - Loading expiry scanner {0}
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_9 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_9] - Expiry scanner {0} does not conform to ExpiryScanner interface
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_10 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_10] - Loading expiry scanner: could not find class {0}
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_11 [com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_11] - {0} has inappropriate value ({1})
 */

public class ExpiredEntryMonitor extends Thread
{
    /**
    *  Start the monitor thread, if the properties make it appropriate
    */

  public static synchronized boolean startUp()
  {
      // ensure singleton
      if ( _theInstance != null )
      {
	  return false;
      }

      // process system properties -- n.b. we only do this once!

      if (!initialised) {
          initialise();
      }
      
      if ( _scanIntervalSeconds == 0 )
      {
	  // no scanning wanted
	      
	  if (tsLogger.arjLoggerI18N.debugAllowed())
	  {
	      tsLogger.arjLoggerI18N.debug( DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					    FacilityCode.FAC_CRASH_RECOVERY, 
					    "com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_2");
	  }
	  
	  return false;
      }

      if ( _expiryScanners.size() == 0 )
      {
	  // nothing to do
	  
	  if (tsLogger.arjLoggerI18N.isDebugEnabled())
	  {
	      tsLogger.arjLoggerI18N.debug( DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					    FacilityCode.FAC_CRASH_RECOVERY, 
					    "com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_3");
	  }
	  
	  return false;
      }
      
      // create, and thus launch the monitor

      _theInstance = new ExpiredEntryMonitor(_skipFirst);

      _theInstance.start();
      
      return true;
  }

    /**
     * terminate any currently active monitor thread, cancelling any further scans but waiting for the
     * thread to exit before returning
     */
  public synchronized static void shutdown()
  {
      if (_theInstance != null) {
          _theInstance.terminate();
          // now wait for it to finish
          try {
              _theInstance.join();
          } catch (InterruptedException e) {
              // ignore
          }
      }

      _theInstance = null;
  }

  private ExpiredEntryMonitor(boolean skipFirst)
  {
    if (tsLogger.arjLoggerI18N.isDebugEnabled())
    {
	tsLogger.arjLoggerI18N.debug( DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE,
				      FacilityCode.FAC_CRASH_RECOVERY, 
				      "com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_4");
    }
    _skipNext = skipFirst;
    _stop = false;

    this.setDaemon(true);
  }
    
  /**
   * performs periodic scans until a shutdwn is notified
   */
  public void run()
  {
    while( true )
    {
	if (tsLogger.arjLogger.isInfoEnabled())
	{
	    tsLogger.arjLogger.info("--- ExpiredEntryMonitor ----" + 
				      _theTimestamper.format(new Date()) + "----" );
	}
	
	if (_skipNext)
    {
        // make sure we skip at most one scan

        _skipNext = false;

        if (tsLogger.arjLoggerI18N.isInfoEnabled())
        {
            tsLogger.arjLoggerI18N.info("com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_5");
        }
    }
    else
	{
	    Enumeration scanners = _expiryScanners.elements();
	    
	    while ( scanners.hasMoreElements() )
	    {
		ExpiryScanner m = (ExpiryScanner)scanners.nextElement();

            // check for a shutdown request before starting a scan
            synchronized (this) {
                if (_stop) {
                    break;
                }
            }

            // ok go ahead and scan

		m.scan();
			
		if (tsLogger.arjLogger.isDebugEnabled())
		{
		    tsLogger.arjLogger.debug( DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					      FacilityCode.FAC_CRASH_RECOVERY,"  "); 
		    // bit of space if detailing
		}
        }
	}

	// wait for a bit to avoid catching (too many) transactions etc. that
	// are really progressing quite happily

	try
	{
        // check for shutdown request before we sleep
        synchronized (this) {
        if (_stop) {
            break;
        }
	    wait( _scanIntervalSeconds * 1000 );
        // check if we were woken because of a shutdown
        if (_stop) {
            break;
        }
        }
	}
	catch ( InterruptedException e1 )
	{
        // we should only get shut down by a shutdown request so ignore interrupts
	}
    }
  }

  private synchronized void terminate()
  {
      _stop = true;
      notify();
  }

    private static void initialise()
    {
        /*
         * Read the system properties to set the configurable options
         */

        _scanIntervalSeconds = recoveryPropertyManager.getRecoveryEnvironmentBean().getExpiryScanInterval() * 60 * 60;

        if (tsLogger.arjLoggerI18N.debugAllowed())
        {
            tsLogger.arjLoggerI18N.debug( DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
                    FacilityCode.FAC_CRASH_RECOVERY,
                    "com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_1",
                    new Object[]{Integer.toString(_scanIntervalSeconds)});
        }

        if (_scanIntervalSeconds != 0)
        {

            // is it being used to skip the first time
            if ( _scanIntervalSeconds < 0 )
            {
                _skipFirst = true;
                _scanIntervalSeconds = - _scanIntervalSeconds;
            }

            loadScanners();
        }

        initialised = true;
    }

  private static void loadScanners()
  {
      _expiryScanners = new Vector();

    // search our properties
    Properties properties = arjPropertyManager.getPropertyManager().getProperties();
    
    if (properties != null)
    {
	Enumeration names = properties.propertyNames();
	
	while (names.hasMoreElements())
	{
	    String propertyName = (String) names.nextElement();
	    
	    if ( propertyName.startsWith(RecoveryEnvironment.SCANNER_PROPERTY_PREFIX) )
	    {
		loadScanner( properties.getProperty(propertyName));
	    }
	}
    }
  }
    
  private static void loadScanner( String className )
  {
      if (tsLogger.arjLoggerI18N.isDebugEnabled())
      {
	  tsLogger.arjLoggerI18N.debug( DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE,
					FacilityCode.FAC_CRASH_RECOVERY, 
					"com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_6", 
					new Object[]{className});
      }
      
      if (className == null)
      {
	  if (tsLogger.arjLoggerI18N.isWarnEnabled())
	      tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_7");
	  
	  return;
      }
      else
      {
          try
	  {
	      Class c = Thread.currentThread().getContextClassLoader().loadClass( className );
		
		try
		{
		   ExpiryScanner m = (ExpiryScanner) c.newInstance();
		   
		   if ( m.toBeUsed() )
		   {
			   _expiryScanners.add( m );
		   }
		   else
		   {
		       if (tsLogger.arjLoggerI18N.isDebugEnabled())
		       {
			   tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE,
							FacilityCode.FAC_CRASH_RECOVERY, 
							"com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_8",
							new Object[]{className});
		       }
		   }
		}
		catch (ClassCastException e)
		{
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
			{
			    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_9", 
							new Object[]{className});
			}
		}
		catch (IllegalAccessException e1)
		{
		    if (tsLogger.arjLoggerI18N.isWarnEnabled())
		    {
			tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_6", 
						    new Object[]{e1});
		    }
		}
		catch (InstantiationException e2)
		{
		    if (tsLogger.arjLoggerI18N.isWarnEnabled())
		    {
			tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_6", 
						    new Object[]{e2});
		    }
		}
		
		c = null;
	  }
	  catch (ClassNotFoundException e)
	  {
	      if (tsLogger.arjLoggerI18N.isWarnEnabled())
	      {
		  tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.ExpiredEntryMonitor_10", 
					      new Object[]{className});
	      }
	  }
      }
  }

    /**
     * flag which causes the next scan to be skipped if it is true. this is set from _skipFirst when a
     * monitor is created and rest to false each time a scan is considered.
     */
    private boolean _skipNext;

    /**
     * flag which causes the monitor thread to stop running when it is set to true
     */
    private boolean _stop;

    /**
     * list of scanners to be invoked by the monitor thread in order to check for expired log entries
     */
    private static Vector _expiryScanners;

    /**
     * flag which guards processing of properties ensuirng it is only performed once
     */
    private static boolean initialised = false;

    /**
     * the default scanning interval if the property file does not supply one
     */
    private static int _scanIntervalSeconds = 12 * 60 * 60;

    /**
     * a date format used to log the time for a scan
     */
    private static SimpleDateFormat _theTimestamper = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

    /**
     * a flag which if true causes the scanner to perform a scan when it is first starts or if false skip this
     * first scan. it can be set to true by supplying a negative scan interval in the property file.
     */
    private static boolean _skipFirst = false;

    /**
     * the currently active monitor instance or null if no scanner is active
     */
    private static ExpiredEntryMonitor _theInstance = null;
}