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
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JDBCStore.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.objectstore.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreAPI;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StateType;
import com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess;
import com.arjuna.ats.arjuna.state.InputBuffer;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputBuffer;
import com.arjuna.ats.arjuna.state.OutputObjectState;

/**
 * An object store implementation which uses a JDBC database for maintaining
 * object states. All states are maintained within a single table.
 *
 * It is assumed that only one object will use a given instance of the
 * JDBCStore. Hence, there is no need for synchronizations.
 */

public class JDBCStore implements ObjectStoreAPI
{
    @Override
    public void start() {}

    @Override
    public void stop() {}
    
    public boolean allObjUids (String s, InputObjectState buff) throws ObjectStoreException
    {
        return allObjUids(s, buff, StateStatus.OS_UNKNOWN);
    }

    /**
     * Does this store need to do the full write_uncommitted/commit protocol?
     *
     * @return <code>true</code> if full commit is needed, <code>false</code>
     * otherwise.
     */

    public boolean fullCommitNeeded ()
    {
        return true;
    }

    /**
     * Some object store implementations may be running with automatic
     * sync disabled. Calling this method will ensure that any states are
     * flushed to disk.
     */

    public void sync () throws java.io.SyncFailedException, ObjectStoreException
    {
    }

    /**
     * Is the current state of the object the same as that provided as the last
     * parameter?
     *
     * @param u The object to work on.
     * @param tn The type of the object.
     * @param st The expected type of the object.
     *
     * @return <code>true</code> if the current state is as expected,
     * <code>false</code> otherwise.
     */

    public boolean isType (Uid u, String tn, int st) throws ObjectStoreException
    {
        return ((currentState(u, tn) == st) ? true : false);
    }

    public String getStoreName()
    {
        if (storeValid())
            return getJDBCAccess().getClass().getName() + ":" + getTableName();
        else
            return "Invalid";
    }

    public boolean commit_state(Uid objUid, String tName)
            throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("JDBCStore.commit_state(" + objUid + ", " + tName + ")");
        }

        /* Bail out if the object store is not set up */

        if (!storeValid())
            return false;
        else
            return _theImple.commit_state(objUid, tName, getTableName());
    }

    public boolean hide_state(Uid objUid, String tName)
            throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("ShadowingStore.hide_state(" + objUid + ", " + tName + ")");
        }

        /* Bail out if the object store is not set up */

        if (storeValid())
            return _theImple.hide_state(objUid, tName, getTableName());
        else
            return false;
    }

    public boolean reveal_state(Uid objUid, String tName)
            throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("ShadowingStore.reveal_state(" + objUid + ", " + tName
                    + ")");
        }

        if (storeValid())
            return _theImple.reveal_state(objUid, tName, getTableName());
        else
            return false;
    }

    /*
    * Determine current state of object. State search is ordered
    * OS_UNCOMMITTED, OS_UNCOMMITTED_HIDDEN, OS_COMMITTED, OS_COMMITTED_HIDDEN
    */

    public int currentState(Uid objUid, String tName)
            throws ObjectStoreException
    {
        if (storeValid())
            return _theImple.currentState(objUid, tName, getTableName());
        else
            return StateStatus.OS_UNKNOWN;
    }

    /**
     * Read an uncommitted instance of State out of the object store. The
     * instance is identified by the unique id and type
     */

    public InputObjectState read_committed(Uid storeUid, String tName)
            throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("JDBCStore.read_committed(" + storeUid + ", "
                    + tName + ")");
        }

        return read_state(storeUid, tName, StateStatus.OS_COMMITTED);
    }

    public InputObjectState read_uncommitted(Uid storeUid, String tName)
            throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("JDBCStore.read_uncommitted(" + storeUid + ", " + tName
                    + ")");
        }

        return read_state(storeUid, tName, StateStatus.OS_UNCOMMITTED);
    }

    public boolean remove_committed(Uid storeUid, String tName)
            throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("JDBCStore.remove_committed(" + storeUid + ", " + tName
                    + ")");
        }

        return remove_state(storeUid, tName, StateStatus.OS_COMMITTED);
    }

    public boolean remove_uncommitted(Uid storeUid, String tName)
            throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("JDBCStore.remove_uncommitted(" + storeUid + ", " + tName
                    + ")");
        }

        return remove_state(storeUid, tName, StateStatus.OS_UNCOMMITTED);
    }

    public boolean write_committed(Uid storeUid, String tName,
                                   OutputObjectState state) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("JDBCStore.write_committed(" + storeUid + ", " + tName
                    + ")");
        }

        return write_state(storeUid, tName, state, StateStatus.OS_COMMITTED);
    }

    public boolean write_uncommitted(Uid storeUid, String tName,
                                     OutputObjectState state) throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("JDBCStore.write_uncommitted(" + storeUid + ", " + tName
                    + ", " + state + ")");
        }

        return write_state(storeUid, tName, state, StateStatus.OS_UNCOMMITTED);
    }

    public final boolean storeValid()
    {
        return _isValid;
    }

    /*
    * Given a type name return an ObjectState that contains all of the uids of
    * objects of that type
    */

    public boolean allObjUids(String tName, InputObjectState state, int match)
            throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("JDBCStore.allObjUids(" + tName + ", " + state + ", "
                    + match + ")");
        }

        if (storeValid())
            return _theImple.allObjUids(tName, state, match, getTableName());
        else
            return false;
    }

    public boolean allTypes(InputObjectState foundTypes)
            throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("JDBCStore.allTypes(" + foundTypes + ")");
        }

        if (storeValid())
            return _theImple.allTypes(foundTypes, getTableName());
        else
            return false;
    }

    public synchronized void packInto(OutputBuffer buff) throws IOException
    {
        buff.packString(getTableName());
    }

    public synchronized void unpackFrom(InputBuffer buff) throws IOException
    {
        setTableName(buff.unpackString());
    }

    protected InputObjectState read_state(Uid objUid, String tName, int ft)
            throws ObjectStoreException
    {
        if (!storeValid())
            return null;
        else
            return _theImple.read_state(objUid, tName, ft, getTableName());
    }

    /**
     * We don't actually delete the state entry, only change its type.
     */

    protected boolean remove_state(Uid objUid, String name, int ft)
            throws ObjectStoreException
    {
        if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("JDBCStore.remove_state("
                    + objUid + ", " + name + ", "
                    + StateType.stateTypeString(ft) + ")");
        }

        if (!storeValid())
            return false;
        else
            return _theImple.remove_state(objUid, name, ft, getTableName());
    }

    protected boolean write_state(Uid objUid, String tName,
                                  OutputObjectState state, int s) throws ObjectStoreException
    {
        if (!storeValid())
            return false;
        else
            return _theImple.write_state(objUid, tName, state, s,
                    getTableName());
    }

    protected JDBCStore(JDBCStoreEnvironmentBean jdbcStoreEnvironmentBean) throws ObjectStoreException
    {
        this.jdbcStoreEnvironmentBean = jdbcStoreEnvironmentBean;

        initialise();
    }

    /**
     * Get the JDBC default table name.
     *
     * @return The default table name.
     */
    protected String getDefaultTableName()
    {
        return _defaultTableName;
    }

    /**
     * Get the JDBC access class.
     *
     * @return The jdbc access variable.
     */
    protected JDBCAccess getJDBCAccess()
    {
        return _jdbcAccess;
    }

    /**
     * Set the JDBC access class.
     *
     * @param jdbcAccess
     *            The jdbc access variable.
     */
    protected void setJDBCAccess(JDBCAccess jdbcAccess)
    {
        _jdbcAccess = jdbcAccess;
    }

    /**
     * Get the JDBC table name.
     *
     * @return The table name.
     */
    protected String getTableName()
    {
        return _jdbcTableName;
    }

    /**
     * Set the JDBC table name.
     *
     * @param tableName
     *            The table name.
     */
    protected void setTableName(String tableName)
    {
        _jdbcTableName = tableName;
    }

    protected void initialise() throws ObjectStoreException
    {
        if(jdbcStoreEnvironmentBean.getJdbcUserDbAccess() == null) {
            throw new ObjectStoreException(tsLogger.i18NLogger.get_objectstore_JDBCStore_5());
        } else {
            setJDBCAccess(jdbcStoreEnvironmentBean.getJdbcUserDbAccess());
        }

        try
        {
            setupStore();
        }
        catch (Exception e)
        {
            tsLogger.i18NLogger.fatal_objectstore_JDBCStore_1(getJDBCAccess().toString(), getTableName());
            throw new ObjectStoreException(e);
        }

        _isValid = true;
    }

    /*
    * Try to create the original and shadow/hidden tables. If this fails, then
    * we will exit.
    */
    @SuppressWarnings("unchecked")
    protected void setupStore() throws Exception
    {
        String impleTableName = getDefaultTableName();
        final String jdbcAccessTableName = getJDBCAccess().tableName();
        if ((jdbcAccessTableName != null) && (jdbcAccessTableName.length() > 0))
        {
            impleTableName = jdbcAccessTableName;
        }

        setTableName(impleTableName);

        final String impleKey = getStoreName();

        synchronized (_theImples)
        {
            final Object currentImple = _theImples.get(impleKey);
            if (currentImple != null)
            {
                _theImple = (com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCImple) currentImple;
            }
            else
            {
                try
                {
                    /*
                    * This had better not be an Arjuna jdbc connection!
                    */
                    final Connection connection;

                    try
                    {
                        connection = getJDBCAccess().getConnection();
                    }
                    catch (final SQLException sqle)
                    {
                        tsLogger.i18NLogger.fatal_objectstore_JDBCStore_2("getConnection()", sqle);
                        throw sqle;
                    }

                    if (connection == null)
                    {
                        tsLogger.i18NLogger.fatal_objectstore_JDBCStore_1(getJDBCAccess().toString(), getTableName());
                        throw new SQLException("getConnection returned null");
                    }
                    boolean success = false;
                    try
                    {
                        connection.setAutoCommit(true);
                        final com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCImple jdbcImple;
                        try
                        {
                            final Class jdbcImpleClass = getJDBCClass(connection);
                            jdbcImple = (com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCImple) jdbcImpleClass
                                    .newInstance();
                        }
                        catch (final Exception ex)
                        {
                            tsLogger.i18NLogger.fatal_objectstore_JDBCStore_2(getJDBCAccess().toString(), ex);
                            throw ex;
                        }

                        if (!jdbcImple.initialise(connection, getJDBCAccess(),
                                impleTableName, jdbcStoreEnvironmentBean)) {
                            tsLogger.i18NLogger.warn_objectstore_JDBCStore_3();
                            throw new ObjectStoreException();
                        }
                        else
                        {
                            _theImples.put(impleKey, jdbcImple);
                            _theImple = jdbcImple;
                            success = true;
                        }
                    }
                    finally
                    {
                        if (!success)
                        {
                            try
                            {
                                connection.close();
                            }
                            catch (final SQLException sqle)
                            {
                            } // Ignore exception
                        }
                    }
                }
                catch (Exception e)
                {
                    tsLogger.logger.warn(e);
                    throw e;
                }
            }
            _isValid = true;
        }
    }

    /**
     * Attempt to load the database class. &nbsp;This method searches for a
     * class called <name>_<major>_<minor>, then <name>_<major> and finally
     * <dbName>
     *
     * @param conn
     *            A database connection.
     * @return The database class.
     * @throws ClassNotFoundException
     *             If no database class can be found.
     * @throws SQLException
     *             If the database connection cannot be interrogated.
     */
    @SuppressWarnings("unchecked")
    protected Class getJDBCClass(Connection conn)
            throws ClassNotFoundException, SQLException
    {
        DatabaseMetaData md = conn.getMetaData();

        String name = md.getDriverName();
        int major = md.getDriverMajorVersion();
        int minor = md.getDriverMinorVersion();

        /*
        * Check for spaces in the name - our implementation classes are always
        * just the first part of such names.
        */

        int index = name.indexOf(' ');

        if (index != -1)
            name = name.substring(0, index);

        name = name.replaceAll("-", "_");

        name = name.toLowerCase();

        final ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        final String packageName = getClass().getPackage().getName() + ".jdbc.";
        try
        {
            return classLoader.loadClass(packageName + name + "_" + major + "_"
                    + minor + "_driver");
        }
        catch (final ClassNotFoundException cnfe)
        {
        }
        try
        {
            return classLoader.loadClass(packageName + name + "_" + major
                    + "_driver");
        }
        catch (final ClassNotFoundException cnfe)
        {
        }
        return classLoader.loadClass(packageName + name + "_driver");
    }

    protected boolean supressEntry(String name)
    {
        return true;
    }

    /*
    * Instance specific data.
    */

    protected boolean _isValid;

    protected com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCImple _theImple;

    private JDBCAccess _jdbcAccess;

    private String _jdbcTableName;

    private static String _defaultTableName = "JBossTSTable";

    protected final JDBCStoreEnvironmentBean jdbcStoreEnvironmentBean;
    
    /*
    * Class data.
    */

    protected static final HashMap _theImples = new HashMap();
}
