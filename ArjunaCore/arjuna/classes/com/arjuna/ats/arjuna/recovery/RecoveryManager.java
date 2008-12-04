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
 * $Id: RecoveryManager.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.recovery;

import java.util.Vector;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;

import com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple;
import com.arjuna.ats.arjuna.utils.Utility;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.common.util.propertyservice.PropertyManagerFactory;

/**
 * @message com.arjuna.ats.arjuna.recovery.RecoveryManager_1 [com.arjuna.ats.arjuna.recovery.RecoveryManager_1] - Invalid recovery manager port specified {0}
 * @message com.arjuna.ats.arjuna.recovery.RecoveryManager_2 [com.arjuna.ats.arjuna.recovery.RecoveryManager_2] - Invalid recovery manager host specified {0}
 * @message com.arjuna.ats.arjuna.recovery.RecoveryManager_3 [com.arjuna.ats.arjuna.recovery.RecoveryManager_3] - Recovery manager bound to {0}:{1}
 * @message com.arjuna.ats.arjuna.recovery.RecoveryManager_4 [com.arjuna.ats.arjuna.recovery.RecoveryManager_4] - Connected to recovery manager on {0}:{1}
 * @message com.arjuna.ats.arjuna.recovery.RecoveryManager_5 [com.arjuna.ats.arjuna.recovery.RecoveryManager_5] - Invalid recovery manager port specified
 */
class ScanThread extends Thread
{

    public ScanThread (RecoveryManagerImple theImple, RecoveryScan callback)
    {
	super("RecoveryManagerScanThread");

	_theImple = theImple;
	_callback = callback;

	setDaemon(true);
    }

    public void run ()
    {
	_theImple.scan();

	if (_callback != null)
	    _callback.completed();
    }

    private RecoveryManagerImple _theImple;
    private RecoveryScan         _callback;
}

/**
 * The RecoveryManager daemon.
 */

public class RecoveryManager
{
    /**
     * In this mode the recovery manager runs periodically but may
     * also be driven through messages or via the scan operation if
     * it is embedded.
     */

    public static final int INDIRECT_MANAGEMENT = 0;

    /**
     * In this mode the recovery manager does not run periodically and
     * will only work if driven through messages or via the scan
     * operation if it is embedded.
     */

    public static final int DIRECT_MANAGEMENT = 1;

    /**
     * Obtain a reference to the RecoveryManager singleton. If it hasn't
     * been created yet then it will be. The manager will be created in the
     * INDIRECT_MANAGEMENT mode.
     *
     * @throw IllegalArgumentException thrown if the manager has already been
     * created in a different mode to that requested.
     *
     * @return the manager.
     */

    public static synchronized final RecoveryManager manager () throws IllegalArgumentException
    {
	return manager(RecoveryManager.INDIRECT_MANAGEMENT);
    }

    /**
     * Obtain a reference to the RecoveryManager singleton. If it hasn't
     * been created yet then it will be. The manager can be created in a
     * management mode defined by the parameter.
     *
     * @param mode the management mode for the manager.
     *
     * @throw IllegalArgumentException thrown if the manager has already been
     * created in a different mode to that requested.
     *
     * @return the manager.
     */

    public static synchronized final RecoveryManager manager (int mode) throws IllegalArgumentException
    {
	if (_recoveryManager == null)
	    _recoveryManager = new RecoveryManager(mode);
	else
	{
	    if (_recoveryManager.mode() != mode)
		throw new IllegalArgumentException();
	}

	return _recoveryManager;
    }

    /**
     * Delay the start of the recovery manager thread when creating an indirect recovery manager.
     */
    public static synchronized void delayRecoveryManagerThread()
    {
        delayRecoveryManagerThread = true ;
    }

    /**
     * Force a recovery scan now. This is a blocking operation
     * and will only return once the recovery scan has completed.
     */

    public final void scan ()
    {
	_theImple.scan();
    }

    /**
     * Force a recovery scan now. This is a non-blocking operation
     * and will return immediately. Notification of completion of the
     * scan is done through the RecoveryScan object.
     *
     * @param callback callback The callback mechanism used to
     * inform users that the scan has completed. If this is <code>null</code>
     * then no callback will happen and asynchronous scanning will occur.
     */

    public final void scan (RecoveryScan callback)
    {
	ScanThread st = new ScanThread(_theImple, callback);

	st.start();
    }

    /**
     * Stop the periodic recovery manager waiting for any recovery scan in progress to complete
     */

    public final void stop ()
    {
        stop(false);
    }

    /**
     * Stop the periodic recovery manager.
     * @param async false means wait for any recovery scan in progress to complete
     */

    public final void stop (boolean async)
    {
        _theImple.stop(async);
    }
    
    // does nothing when running embedded.
    
    public void waitForTermination ()
    {
        _theImple.waitForTermination();
    }
    
    /**
     * Suspend the recovery manager. If the recovery manager is in the process of
     * doing recovery scans then it will be suspended afterwards, in order to
     * preserve data integrity.
     *
     * @param async false means wait for the recovery manager to finish any scans before returning.
     */

    public void suspendScan (boolean async)
    {
	_theImple.suspendScan(async);
    }

    public void resumeScan ()
    {
	_theImple.resumeScan();
    }

    /**
     * Start the recovery manager thread.
     */
    public void startRecoveryManagerThread()
    {
        _theImple.start() ;
    }

    /**
     * Add a recovery module to the system.
     *
     * @param module module The module to add.
     */

    public final void addModule (RecoveryModule module)
    {
	_theImple.addModule(module);
    }

    /**
     * Remove a recovery module from the system.
     *
     * @param module The module to remove.
     * @param waitOnScan true if the remove operation should wait for any in-progress scan to complete
     */

    public final void removeModule (RecoveryModule module, boolean waitOnScan)
    {
	_theImple.removeModule(module, waitOnScan);
    }

    /**
     * Obtain a snapshot list of available recovery modules.
     * @return a snapshot list of the currently installed recovery modules
     */

    public final Vector getModules ()
    {
	return _theImple.getModules();
    }

    /**
     * Indicates what mode (INDIRECT_MANAGEMENT or DIRECT_MANAGEMENT)
     * the recovery manager is configured for.
     *
     * @return the management mode.
     */

    public final int mode ()
    {
	return _mode;
    }

    public static InetAddress getRecoveryManagerHost() throws UnknownHostException
    {
        PropertyManager pm = PropertyManagerFactory.getPropertyManager("com.arjuna.ats.propertymanager", "recoverymanager");

        if ( pm == null )
            return InetAddress.getLocalHost();

        String hostPropName = com.arjuna.ats.arjuna.common.Environment.RECOVERY_MANAGER_ADDRESS;
        String host = pm.getProperty(hostPropName);

        return Utility.hostNameToInetAddress(host, "com.arjuna.ats.arjuna.recovery.RecoveryManager_2");
    }

    public static int getRecoveryManagerPort()
    {
        PropertyManager pm = PropertyManagerFactory.getPropertyManager("com.arjuna.ats.propertymanager", "recoverymanager");

        if (pm == null)
            return 0;

        String portPropName = com.arjuna.ats.arjuna.common.Environment.RECOVERY_MANAGER_PORT;
        Integer port = Utility.lookupBoundedIntegerProperty(pm, portPropName, null,
                    "com.arjuna.ats.arjuna.recovery.RecoveryManager_1",
                    0, Utility.MAX_PORT);

        if (port == null)
        {
            String portStr = pm.getProperty(portPropName);

           /*
            * if the property files specified a value for the port which is invalid throw a fatal error. An empty value or no value
            * corresponds to any port
            */
            if (portStr == null || portStr.length() == 0)
                port = 0;
            else
                throw new com.arjuna.ats.arjuna.exceptions.FatalError(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.recovery.RecoveryManager_5"));
        }

        return port;
    }

    /**
     * Obtain a client connection to the recovery manager
     *
     * @return a bound client socket connection to the recovery manager
     * @throws IOException
     */
    public static Socket getClientSocket () throws IOException
    {
        Socket socket = new Socket(getRecoveryManagerHost(), getRecoveryManagerPort());

        if (tsLogger.arjLogger.isInfoEnabled())
        {
            tsLogger.arjLoggerI18N.info("com.arjuna.ats.arjuna.recovery.RecoveryManager_4",
                    new Object[]{socket.getInetAddress().getHostAddress(), socket.getLocalPort()});
        }

        return socket;
    }

    /**
     * Run the RecoveryManager. See Administration manual for details.
     */

    public static void main (String[] args)
    {
	boolean testMode = false;

	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-help") == 0)
	    {
		System.out.println("Usage: com.arjuna.ats.arjuna.recovery.RecoveryManager [-help] [-test] [-version]");
		System.exit(0);
	    }
	    if (args[i].compareTo("-version") == 0)
	    {
		System.out.println("Version " + com.arjuna.ats.arjuna.common.Configuration.version());
		System.exit(0);
	    }
	    if (args[i].compareTo("-test") == 0)
	    {
		testMode = true;
	    }
	}

	try
	{
	    manager();

	    if (testMode)
		System.out.println("Ready");

	    manager().waitForTermination();
	}
	catch (Throwable e)
	{
	    e.printStackTrace();
	}
    }

    private RecoveryManager (int mode)
    {
	if ((mode == RecoveryManager.INDIRECT_MANAGEMENT) && !delayRecoveryManagerThread)
	    _theImple = new RecoveryManagerImple(true);
	else
	    _theImple = new RecoveryManagerImple(false);

	_mode = mode;
    }

    private RecoveryManagerImple _theImple = null;
    private int _mode;

    private static RecoveryManager _recoveryManager = null;
    private static boolean delayRecoveryManagerThread ;
}
