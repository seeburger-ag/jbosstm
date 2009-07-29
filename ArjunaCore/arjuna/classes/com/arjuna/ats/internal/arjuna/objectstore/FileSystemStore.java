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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: FileSystemStore.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.objectstore;

import com.arjuna.ats.arjuna.ArjunaNames;
import com.arjuna.ats.arjuna.objectstore.*;
import com.arjuna.ats.arjuna.common.*;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.gandiva.ClassName;
import com.arjuna.ats.arjuna.gandiva.ObjectName;
import com.arjuna.ats.arjuna.utils.FileLock;
import com.arjuna.ats.arjuna.utils.Utility;

import java.util.Hashtable;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import java.lang.NumberFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.arjuna.common.util.logging.*;

/**
 * The basic class for file system object stores. This is not actually
 * an object store implementation, since other classes must provide
 * implementations of the abstract methods. It does provide implementations
 * of common methods though.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: FileSystemStore.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 *
 *
 * @message com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_1 [com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_1] - FileSystemStore::setupStore - cannot access root of object store: {0}
 * @message com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_2 [com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_2] - FileSystemStore.removeFromCache - no entry for {0}
 * @message com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_2a [com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_3] - FileSystemStore::allObjUids - could not pack Uid.
 * @message com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_3 [com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_3] - FileSystemStore::allObjUids - could not pack end of list Uid.
 * @message com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_4 [com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_4] - FileSytemStore::allTypes - could not pack entry string.
 * @message com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_5 [com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_5] - FileSystemStore::allTypes - could not pack end of list string.
 * @message com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_6 [com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_6] - FileSystemStore::setupStore - error from unpack object store.
 * @message com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_7 [com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_7] - FileSystemStore::allTypes - could not pack entry string.
 * @message com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_8 [com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_8] - FileSystemStore::createHierarchy - null directory name.
 * @message com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_20 [com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_20] - FileSystemStore.renameFromTo - from {0} not present. Possibly renamed by crash recovery.
 */

public abstract class FileSystemStore extends ObjectStoreImple
{

    public FileSystemStore (String locationOfStore, int ss)
    {
	super(ss);

	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE,
				     "FileSystemStore.FileSystemStore("+locationOfStore+")");
	}

	fullStoreName = null;
	localStoreRoot = null;
	isValid = true;

	try
	{
	    setupStore(locationOfStore);
	}
	catch (ObjectStoreException e)
	{
	    isValid = false;

	    tsLogger.arjLogger.warn(e.getMessage());

	    throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString(), e);
	}
    }

    /**
     * This constructor assumes that everything for the objectstore
     * is setup in the associated ObjectName. Sensible defaults are
     * used otherwise.
     */

    public FileSystemStore (ObjectName objName)
    {
	super(objName);

	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "FileSystemStore.FileSystemStore("+objName+")");
	}

	fullStoreName = null;
	localStoreRoot = null;
	isValid = true;

	try
	{
	    setupStore("");
	}
	catch (ObjectStoreException e)
	{
	    isValid = false;

	    tsLogger.arjLogger.warn(e.getMessage());

	    throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString(), e);
	}
    }

    public String getStoreName ()
    {
	return localStoreRoot;
    }

    /*
     * read an uncommitted instance of State out of the object store.
     * The instance is identified by the unique id and type
     */

    public InputObjectState read_committed (Uid storeUid, String tName) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "FileSystemStore.read_committed("+storeUid+", "+tName+")");
	}

	return read_state(storeUid, tName, ObjectStore.OS_ORIGINAL);
    }

    public InputObjectState read_uncommitted (Uid storeUid, String tName) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "FileSystemStore.read_uncommitted("+storeUid+", "+tName+")");
	}

	return read_state(storeUid, tName, ObjectStore.OS_SHADOW);
    }

    public boolean remove_committed (Uid storeUid, String tName) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE,
				     "FileSystemStore.remove_committed("+storeUid+", "+tName+")");
	}

	return remove_state(storeUid, tName, ObjectStore.OS_ORIGINAL);
    }

    public boolean remove_uncommitted (Uid storeUid, String tName) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
       	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE,
				     "FileSystemStore.remove_uncommitted("+storeUid+", "+tName+")");
	}

	return remove_state(storeUid, tName, ObjectStore.OS_SHADOW);
    }

    public boolean write_committed (Uid storeUid, String tName, OutputObjectState state) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE,
				     "FileSystemStore.write_committed("+storeUid+", "+tName+")");
	}

	return write_state(storeUid, tName, state, ObjectStore.OS_ORIGINAL);
    }

    public boolean write_uncommitted (Uid storeUid, String tName, OutputObjectState state) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE,
				     "FileSystemStore.write_uncommitted("+storeUid+", "+tName+", "+state+")");
	}

	return write_state(storeUid, tName, state, ObjectStore.OS_SHADOW);
    }

    public final synchronized boolean storeValid ()
    {
	return isValid;
    }

    public final synchronized void makeInvalid ()
    {
	isValid = false;
    }

    public final synchronized void makeValid ()
    {
	isValid = true;
    }

    /**
     * Given a type name initialise the <code>state</code> to contains all of
     * the Uids of objects of that type
     */

    public boolean allObjUids (String tName, InputObjectState state, int match) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
       	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE,
				     "FileSystemStore.allObjUids("+tName+", "+state+", "+match+")");
	}

	String directory = null;
	OutputObjectState store = new OutputObjectState();

	/*
	 * If typename starts with a '/' then skip over it.
	 */

	if ((tName != null) && (tName.charAt(0) == File.separatorChar))
	{
	    String s = tName.substring(1, tName.length());
	    directory = new String(fullStoreName + s);
	}
	else
	    directory = new String(fullStoreName + tName);

	File f = new File(directory);
	String[] entry = f.list();

	if ((entry != null) && (entry.length > 0))
	{
	    for (int i = 0; i < entry.length; i++)
	    {
		try
		{
		    Uid aUid = new Uid(entry[i], true);

		    if (!aUid.valid() || (aUid.equals(Uid.nullUid())))
		    {
			String revealed = revealedId(entry[i]);

			// don't want to give the same id twice.

			if (present(revealed, entry))
			    aUid = null;
			else
			    aUid = new Uid(revealed);
		    }

		    if ((aUid != null) && (aUid.valid()))
		    {
			if ((aUid.notEquals(Uid.nullUid())) && ((match == ObjectStore.OS_UNKNOWN) ||
								(isType(aUid, tName, match))))
			{
			    aUid.pack(store);
			}
		    }
		}
		catch (NumberFormatException e)
		{
		    /*
		     * Not a number at start of file.
		     */
		}
		catch (IOException e)
		{
		    throw new ObjectStoreException(tsLogger.log_mesg.getString("com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_2a"), e);
		}
	    }
	}

	try
	{
	    Uid.nullUid().pack(store);
	}
	catch (IOException e)
	{
	    throw new ObjectStoreException(tsLogger.log_mesg.getString("com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_3"), e);
	}

	state.setBuffer(store.buffer());

	store = null;

	return true;
    }

    public boolean allTypes (InputObjectState foundTypes) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
				     FacilityCode.FAC_OBJECT_STORE, "FileSystemStore.allTypes("+foundTypes+")");
	}

	boolean result = true;
	String directory = new String(fullStoreName);
	File f = new File(directory);
	String[] entry = f.list();

	if (entry == null)
	    return true;

	OutputObjectState store = new OutputObjectState();

	for (int i = 0; i < entry.length; i++)
	{
	    if (!supressEntry(entry[i]))
	    {
		File tmpFile = new File(directory+File.separator+entry[i]);

		if (tmpFile.isDirectory())
		{
		    try
		    {
			store.packString(entry[i]);

			result = allTypes(store, entry[i]);
		    }
		    catch (IOException e)
		    {
			throw new ObjectStoreException(tsLogger.log_mesg.getString("com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_4"), e);
		    }
		}

		tmpFile = null;
	    }
	}

	try
	{
	    store.packString("");
	}
	catch (IOException e)
	{
	    throw new ObjectStoreException(tsLogger.log_mesg.getString("com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_5"), e);
	}

	foundTypes.setBuffer(store.buffer());

	return result;
    }

    public synchronized void packInto (OutputBuffer buff) throws IOException
    {
	if (localStoreRoot != null)
	{
	    if (localStoreRoot.compareTo("") == 0)
	    {
		buff.packString(null);
		return;
	    }
	}

	buff.packString(localStoreRoot);
    }

    public synchronized void unpackFrom (InputBuffer buff) throws IOException
    {
	try
	{
	    setupStore(buff.unpackString());
	}
	catch (ObjectStoreException e)
	{
	    throw new IOException(tsLogger.log_mesg.getString("com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_6"));
	}
    }

    public ClassName className ()
    {
	return ArjunaNames.Implementation_ObjectStore_FileSystemStore();
    }

    public static ClassName name ()
    {
	return ArjunaNames.Implementation_ObjectStore_FileSystemStore();
    }

    protected abstract InputObjectState read_state (Uid u, String tn, int s) throws ObjectStoreException;
    protected abstract boolean remove_state (Uid u, String tn, int s) throws ObjectStoreException;
    protected abstract boolean write_state (Uid u, String tn, OutputObjectState buff, int s) throws ObjectStoreException;

    /**
     * Turn file sync on and off.
     */

    protected synchronized final void syncOn ()
    {
	FileSystemStore.doSync = true;
    }

    protected synchronized final void syncOff ()
    {
	FileSystemStore.doSync = false;
    }

    /**
     * Are synchronous write enabled?
     */

    protected synchronized final boolean synchronousWrites ()
    {
	return FileSystemStore.doSync && syncWrites;
    }

    /**
     * Lock the file in the object store.
     */

    protected synchronized boolean lock (File fd, int lmode, boolean create)
    {
	FileLock fileLock = new FileLock(fd);

	return fileLock.lock(lmode, create);
    }

    /**
     * Unlock the file in the object store.
     */

    protected synchronized boolean unlock (File fd)
    {
	FileLock fileLock = new FileLock(fd);

	return fileLock.unlock();
    }

    /**
     * Unlock and close the file. Note that if the unlock fails we set
     * the return value to false to indicate an error but rely on the
     * close to really do the unlock.
     */

    protected boolean closeAndUnlock (File fd, FileInputStream ifile, FileOutputStream ofile)
    {
        if (tsLogger.arjLogger.debugAllowed())
        {
            tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE,
                                     FacilityCode.FAC_OBJECT_STORE, "FileSystemStore.closeAndUnlock("+fd+", "+ifile+", "+ofile+")");
        }

        boolean closedOk = unlock(fd);

        try
        {
            if (ifile != null)
                ifile.close();
            else
                ofile.close();
        }
        catch (Exception e)
        {
            closedOk = false;
        }

        return closedOk;
    }

    protected File openAndLock (String fname, int lmode, boolean create) throws ObjectStoreException
    {
        if (tsLogger.arjLogger.debugAllowed())
        {
            tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE,
                                     FacilityCode.FAC_OBJECT_STORE, "FileSystemStore.openAndLock("+fname+", "+FileLock.modeString(lmode)+", "+create+")");
        }

        //      File fd = (File) FdCache(fname);
        File fd = null;

        if (fd == null)
        {
            fd = new File(fname);

            if (!fd.exists())
            {
                if (createHierarchy(fname))
                {
                    if (!lock(fd, lmode, create))
                    {
                        return null;
                    }
                    else
                        return fd;
                }
                else
                    throw new ObjectStoreException("FileSystemStore.openAndLock failed to create hierarchy "+fname);
            }

            if (!lock(fd, lmode, create))
                fd = null;
        }

        return fd;
    }

    /*
     * Renaming on Unix works if the file to rename to already exists.
     * However, on Windows if the file exists then rename fails! So, we
     * need to delete the file to rename to before we can rename. But, we
     * must ensure that we don't get into any race conditions, so we
     * exclusively lock the file to be deleted first. Failure scenarios:
     *
     * (i) if we crash after deleting, but before we have had a chance to
     *     rename the file, then the lock file will exist and prevent anyone
     *     from deleting the shadow.
     *
     * (ii) if we crash after renaming and before the lock file is removed
     *      then subsequent lock attempts will fail, and the sys admin will
     *      have to resolve. But consistency will be maintained!
     *
     * When JDK 1.4 supports appears we will use the file-locking facility
     * it provides.
     *
     * We have to use locks at deletion, but an implementation such as
     * ShadowNoFileLockStore can still get away with no locking elsewhere
     * to improve performance.
     */

    protected synchronized final boolean renameFromTo (File from, File to)
    {
        if (!isWindows)
            return from.renameTo(to);
        else
        {
            //      FileLock fl = new FileLock(to);

            if (!from.exists())
            {
                /*
                 * from is in the cache, but not on disk. So, either
                 *
                 * (i) two different users are using the same state and
                 *     should have been using the OS_SHARED flag.
                 *
                 * or
                 *
                 * (ii) crash recovery has recovered the state.
                 *
                 * If (ii) we can't force OS_SHARED on all users for the
                 * minority case. So, assume (ii) and issue a warning.
                 */

                removeFromCache(from.toString());

                if (tsLogger.arjLoggerI18N.isWarnEnabled())
                {
                    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_20",
                                                new Object[]{from});
                }

                return true;
            }

            /*
             * Let let crash recovery deal with this!
             */

            //      if (fl.lock(FileLock.F_WRLCK))
            {
                to.delete();

                boolean res = from.renameTo(to);

                //              fl.unlock();

                return true;
            }
            /*
            else
            {
                if (tsLogger.arjLoggerI18N.isWarnEnabled())
                {
                    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore_21",
                                                new Object[]{to});
                }

                return false;
                }*/
        }
    }

    protected FileSystemStore (int ss)
    {
	super(ss);

	fullStoreName = null;
	localStoreRoot = null;
	isValid = true;
    }

    protected boolean allTypes (OutputObjectState foundTypes, String root) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_OBJECT_STORE,
				     "FileSystemStore.allTypes("+foundTypes+", "+root+")");
	}

	boolean result = true;
	String directory = new String(fullStoreName + File.separator + root);
	File f = new File(directory);
	String[] entry = f.list();

	if ((entry != null) && (entry.length > 0))
	{
	    for (int i = 0; i < entry.length; i++)
	    {
		if (!supressEntry(entry[i]))
		{
		    try
		    {
			File tmpFile = new File(directory+File.separator+entry[i]);

			if (tmpFile.isDirectory())
			{
			    String pack = truncate(entry[i]);

                if ( pack.length() > 0 )
                {
			        foundTypes.packString(root+File.separator+pack);

			        result = allTypes(foundTypes, root+File.separator+pack);
                }
			}

			tmpFile = null;
		    }
		    catch (IOException e)
		    {
			throw new ObjectStoreException(tsLogger.log_mesg.getString("com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_7"), e);
		    }
		}
	    }
	}

	return result;
    }

    /**
     * @return the file name for the state of the object
     * identified by the Uid and TypeName. If the StateType argument
     * is OS_SHADOW then the Uid part of the name includes # characters.
     * The magic number SLOP below is the number of extra characters needed
     * to make up the entire path.
     */

    protected String genPathName (Uid objUid,
				  String tName, int ostype) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_OBJECT_STORE,
				     "FileSystemStore.genPathName("+objUid+", "+tName+", "+ostype+")");
	}

	String storeName = locateStore(getStoreName());
	String cPtr = null;
	String fname = null;
	String os = objUid.fileStringForm();  // convert all ':' to '_' to be portable across file systems.

	if ((tName == null) || (tName.length() == 0))
	    cPtr = "";
	else
	{
	    cPtr = tName;

	    /*
	     * Convert Unix separators to 'other', i.e., Windows!
	     */

	    if (FileSystemStore.rewriteSeparator && (cPtr.indexOf(FileSystemStore.unixSeparator) != -1))
	    {
		cPtr = cPtr.replace(FileSystemStore.unixSeparator, File.separatorChar);
	    }
	}

	/*
	 * storeName always ends in '/' so we can remove any
	 * at the start of the type name.
	 */

	if (cPtr.charAt(0) == File.separatorChar)
	    cPtr = cPtr.substring(1, cPtr.length());

	if (cPtr.charAt(cPtr.length() -1) != File.separatorChar)
	    fname = storeName + cPtr + File.separator + os;
	else
	    fname = storeName + cPtr + os;

	/*
	 * Make sure we don't end in a '/'.
	 */

	if (fname.charAt(fname.length() -1) == File.separatorChar)
	    fname = fname.substring(0, fname.length() -2);

	return fname;
    }

    protected synchronized boolean setupStore (String localOSRoot) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_OBJECT_STORE, "FileSystemStore.setupStore("+localOSRoot+")");
	}

	isValid = true;

	if (localOSRoot == null)
	    localOSRoot = "";

	localStoreRoot = localOSRoot;
	fullStoreName = locateStore(localStoreRoot);

	/* The root of the objectstore must exist and be writable */

	if ((fullStoreName == null) || !createHierarchy(fullStoreName))
	{
	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
	    {
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_1",
					    new Object[]{fullStoreName});
	    }

	    isValid = false;
	}

	return isValid;
    }

    protected boolean supressEntry (String name)
    {
	if ((name.compareTo(".") == 0) || (name.compareTo("..") == 0))
	    return true;
	else
	    return false;
    }

    protected String truncate (String value)
    {
	return value;
    }

    /**
     * Attempt to build up the object store in the file system dynamically.
     * This creates directories as required as new types are added to the
     * store. Note that we made sure the root object store was created and
     * writable at construction time.
     *
     * WARNING: on a multi-processor box it is possible that multiple threads may
     * try to create the same hierarchy at the same time. What will tend to happen
     * is that one thread will succeed and the other(s) will fail. However, if a failed
     * thread just assumes that the directory is being created by another thread, then
     * we're in trouble, because although this may be the case, the directory structure
     * may not have actually been created - it may still be in the process of being
     * created. So, we have to err on the side of caution and try to create the directory
     * a few times. (This can happen across processes too.)
     */

    protected synchronized final boolean createHierarchy (String path) throws ObjectStoreException
    {
	if (tsLogger.arjLogger.debugAllowed())
	{
       	    tsLogger.arjLogger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PROTECTED,
				     FacilityCode.FAC_OBJECT_STORE, "FileSystemStore.createHierarchy("+path+")");
	}

	if ((path != null) && (path.length() > 0))
	{
	    File f = null;

	    /*
	     * Is string a complete directory list, or is it an
	     * absolute file name?
	     */

	    if (path.charAt(path.length() -1) != File.separatorChar)
	    {
		int index = path.lastIndexOf(File.separator);

		if (index <= 0)
		    return true;
		else
		    f = new File(path.substring(0, index));
	    }
	    else
		f = new File(path);

	    int retryLimit = FileSystemStore.createRetry;

	    do
	    {
		if (f.exists())
		{
		    return true;
		}
		else
		{
		    if (!f.mkdirs())
		    {
			retryLimit--;

			if (retryLimit == 0)
			    return false;

			try
			{
			    Thread.currentThread().sleep(FileSystemStore.createTimeout);
			}
			catch (Exception ex)
			{
			}
		    }
		    else
			return true;
		}
	    } while (!f.exists() && (retryLimit > 0));

	    return f.exists();
	}
	else
	    throw new ObjectStoreException(tsLogger.log_mesg.getString("com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_8"));
    }

    /**
     * If this object store implementation is exclusively working on a set of
     * object states, then this method will check a file cache first.
     * Otherwise, we must go back to the file system each time to check the
     * status of the file.
     *
     * If we add a FileDescriptor cache a la the C++ version then we would
     * be able to get rid of the state cache and simply check to see if we
     * had a fd for it.
     */

    protected final boolean exists (String path)
    {
	if (super.shareStatus == ObjectStore.OS_UNSHARED)
	{
	    if (FileSystemStore.fileCache.get(path) != null)
		return true;
	}

	/*
	 * If here then we need to check the file system. If there, we will
	 * put it into the cache (if appropriate).
	 */

	File f = new File(path);
	boolean doesExist = f.exists();

	if (doesExist)
	    addToCache(path);

	return doesExist;
    }

    protected final void addToCache (String fname)
    {
	if (super.shareStatus == ObjectStore.OS_UNSHARED)
	{
	    FileSystemStore.fileCache.put(fname, fname);
	}
    }

    protected final void removeFromCache (String fname)
    {
	removeFromCache(fname, true);
    }

    /**
     * Print a warning if the file to be removed is not in the cache.
     *
     * @since 2.1.1.
     */

    protected final void removeFromCache (String fname, boolean warn)
    {
	if (super.shareStatus == ObjectStore.OS_UNSHARED)
	{
	    if ((FileSystemStore.fileCache.remove(fname) == null) && warn)
	    {
		if (tsLogger.arjLoggerI18N.isWarnEnabled())
		{
		    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.objectstore.FileSystemStore_2",
						new Object[]{fname});
		}
	    }
	}
    }

    private final boolean present (String id, String[] list)
    {
	for (int i = 0; i < list.length; i++)
	{
	    if (list[i].equals(id))
		return true;
	}

	return false;
    }

    static final char unixSeparator = '/';

    static boolean rewriteSeparator = false;

    // allow derived classes to specify sync on a per instance basis

    protected boolean syncWrites = true;

    private String  fullStoreName;
    private String  localStoreRoot;
    private boolean isValid;

    /*
     * These values should be determined via something like pathconf
     */

    private static final int MAXPNAMELEN = 255;
    private static final int MAXNAMELENGTH = 255;
    private static final int SLOP = 9;

    // global values (some of which may be reset on a per instance basis).

    private static boolean   doSync = true;
    private static Hashtable fileCache = new Hashtable();
    private static int       createRetry = 100;
    private static int       createTimeout = 100;

    static
    {
        FileSystemStore.doSync = arjPropertyManager.getObjectStoreEnvironmentBean().isObjectStoreSync();

        if (File.separatorChar != FileSystemStore.unixSeparator)
            rewriteSeparator = true;

        createRetry = arjPropertyManager.getObjectStoreEnvironmentBean().getHierarchyRetry();

        if (createRetry < 0)
            createRetry = 100;

        createTimeout = arjPropertyManager.getObjectStoreEnvironmentBean().getHierarchyTimeout();

        if (createTimeout < 0)
            createTimeout = 100;
    }

    private static boolean isWindows = Utility.isWindows();

}
