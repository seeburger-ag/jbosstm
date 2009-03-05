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

import com.arjuna.ats.jta.common.*;

import com.arjuna.ats.internal.jts.ORBManager;

import com.arjuna.orbportability.*;

public class SuspendResume
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
        catch (Exception e)
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

                if (theTransaction != null)
                {
                    tm.commit();

                    tm.resume(theTransaction);
                }
                else
                {
                    System.err.println("Error - could not get transaction!");
                    tm.rollback();
                    System.exit(0);
                }

                System.out.println("\nTest completed successfully.");
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