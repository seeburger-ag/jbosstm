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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ActionStore.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.objectstore;

import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreType;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StateType;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

import com.arjuna.ats.arjuna.logging.tsLogger;

/**
 * The basic transaction log implementation. Uses the no file-level locking
 * implementation of the file system store since only a single user (the
 * coordinator) can ever be manipulating the action's state.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ActionStore.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 */

public class ActionStore extends ShadowNoFileLockStore
{

    public int typeIs ()
    {
        return ObjectStoreType.ACTION;
    }

    /**
     * @return current state of object. Assumes that genPathName allocates
     *         enough extra space to allow extra chars to be added. Action
     *         stores only store committed objects
     */

    public int currentState (Uid objUid, String tName)
            throws ObjectStoreException
    {
        int theState = StateStatus.OS_UNKNOWN;

        if (storeValid())
        {
            String path = genPathName(objUid, tName, StateType.OS_ORIGINAL);

            if (exists(path))
                theState = StateStatus.OS_COMMITTED;

            path = null;
        }

        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ActionStore.currentState("+objUid+", "+tName+") - returning "+
                    StateStatus.stateStatusString(theState));
        }

        return theState;
    }

    /**
     * Commit a previous write_state operation which was made with the SHADOW
     * StateType argument. This is achieved by renaming the shadow and removing
     * the hidden version.
     */

    public boolean commit_state (Uid objUid, String tName)
            throws ObjectStoreException
    {
        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ActionStore.commit_state(" + objUid + ", " + tName + ")");
        }

        boolean result = false;

        /* Bail out if the object store is not set up */

        if (!storeValid())
            return false;

        if (currentState(objUid, tName) == StateStatus.OS_COMMITTED)
            result = true;

        return result;
    }

    public boolean hide_state (Uid u, String tn) throws ObjectStoreException
    {
        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ActionStore.hide_state(" + u + ", " + tn + ")");
        }

        return false;
    }

    public boolean reveal_state (Uid u, String tn) throws ObjectStoreException
    {
        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ActionStore.reveal_state(" + u + ", " + tn + ")");
        }

        return false;
    }

    public InputObjectState read_committed (Uid storeUid, String tName)
            throws ObjectStoreException
    {
        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ActionStore.read_committed(" + storeUid + ", " + tName
                    + ")");
        }

        return super.read_committed(storeUid, tName);
    }

    public InputObjectState read_uncommitted (Uid u, String tn)
            throws ObjectStoreException
    {
        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ActionStore.read_uncommitted(" + u + ", " + tn + ")");
        }

        return null;
    }

    public boolean remove_committed (Uid storeUid, String tName)
            throws ObjectStoreException
    {
        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ActionStore.remove_committed(" + storeUid + ", " + tName
                    + ")");
        }

        return super.remove_committed(storeUid, tName);
    }

    public boolean remove_uncommitted (Uid u, String tn)
            throws ObjectStoreException
    {
        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ActionStore.remove_uncommitted(" + u + ", " + tn + ")");
        }

        return false;
    }

    public boolean write_committed (Uid storeUid, String tName,
            OutputObjectState state) throws ObjectStoreException
    {
        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ActionStore.write_committed(" + storeUid + ", " + tName
                    + ")");
        }

        return super.write_committed(storeUid, tName, state);
    }

    public boolean write_uncommitted (Uid u, String tn, OutputObjectState s)
            throws ObjectStoreException
    {
        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("ActionStore.write_uncommitted(" + u + ", " + tn + ", " + s
                    + ")");
        }

        return false;
    }

    public ActionStore(String locationOfStore)
    {
        this(locationOfStore, StateType.OS_SHARED);
    }

    public ActionStore(String locationOfStore, int shareStatus)
    {
        super(shareStatus);

        try
        {
            setupStore(locationOfStore);
        }
        catch (ObjectStoreException e) {
            tsLogger.arjLogger.warn(e);

            super.makeInvalid();

            throw new FatalError(e.toString(),
                    e);
        }
    }

    public ActionStore()
    {
        this(StateType.OS_SHARED);
    }

    public ActionStore(int shareStatus)
    {
        this(arjPropertyManager.getObjectStoreEnvironmentBean()
                    .getLocalOSRoot(), shareStatus);
    }

    protected synchronized boolean setupStore (String location)
            throws ObjectStoreException
    {
        if (!checkSync)
        {
            if (arjPropertyManager.getObjectStoreEnvironmentBean()
                    .isTransactionSync())
            {
                syncOn();
            }
            else
            {
                syncOff();
            }
        }

        checkSync = true;

        return super.setupStore(location);
    }

    private static boolean checkSync = false;

}
