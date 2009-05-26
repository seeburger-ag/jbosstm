/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JTATest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.jts.basic;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import com.arjuna.ats.jta.common.*;
import com.arjuna.ats.jta.utils.JTAHelper;

import com.arjuna.ats.internal.jts.ORBManager;

import com.arjuna.orbportability.*;

class TWorker extends Thread
{
    public TWorker (javax.transaction.Transaction tx, boolean first)
    {
        _tx = tx;
        _success = true;
        _first = first;
    }
    
    public void run ()
    {   
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();
        
        try
        {
            tm.resume(_tx);

            if (!_first)
            {
                while (tm.getStatus() == Status.STATUS_ACTIVE)
                {
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (final Exception ex)
                    {             
                    }
                }
            }
 
            tm.commit();
        }
        catch (final Exception ex)
        {
            ex.printStackTrace();
            
            _success = false;
        }
    }
    
    public final boolean success ()
    {
        return _success;
    }
    
    private Transaction _tx;
    private boolean _success;
    private boolean _first;
}

public class ThreadedCommit
{

    public static void main (String[] args)
    {
        ORB myORB = null;
        RootOA myOA = null;

        try
        {
            myORB = ORB.getInstance("test");
            myOA = OA.getRootOA(myORB);

            myORB.initORB(args, null);
            myOA.initOA();

            ORBManager.setORB(myORB);
            ORBManager.setPOA(myOA);
        }
        catch (final Exception e)
        {
            System.err.println("Initialisation failed: " + e);

            System.exit(0);
        }

        jtaPropertyManager.propertyManager
                .setProperty(
                        com.arjuna.ats.jta.common.Environment.JTA_TM_IMPLEMENTATION,
                        "com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple");
        jtaPropertyManager.propertyManager
                .setProperty(
                        com.arjuna.ats.jta.common.Environment.JTA_UT_IMPLEMENTATION,
                        "com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple");

        try
        {
            javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
                    .transactionManager();

            if (tm != null)
            {
                System.out.println("Starting top-level transaction.");

                tm.begin();

                javax.transaction.Transaction theTransaction = tm
                        .getTransaction();

                TWorker worker1 = new TWorker(theTransaction, true);               
                TWorker worker2 = new TWorker(theTransaction, false);
                
                worker1.start();
                worker2.start();
                
                worker1.join();
                worker2.join();
                
                if (worker1.success() && worker2.success())
                {
                    System.out.println("\nTest completed successfully.");
                }
                else
                    System.out.println("\nTest unsuccessful.");
                
                System.exit(0);
            }
            else
            {
                System.err
                        .println("Error - could not get transaction manager!");
                System.exit(0);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        myOA.destroy();
        myORB.shutdown();
    }

}