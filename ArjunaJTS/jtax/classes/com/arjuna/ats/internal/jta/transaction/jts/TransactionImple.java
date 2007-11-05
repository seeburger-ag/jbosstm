/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
 * $Id: TransactionImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.jts;

import com.arjuna.ats.internal.jta.utils.*;
import com.arjuna.ats.internal.jta.utils.jts.StatusConverter;
import com.arjuna.ats.internal.jta.resources.jts.CleanupSynchronization;
import com.arjuna.ats.internal.jta.resources.jts.LocalCleanupSynchronization;
import com.arjuna.ats.internal.jta.resources.jts.orbspecific.LastResourceRecord;
import com.arjuna.ats.internal.jta.resources.jts.orbspecific.SynchronizationImple;
import com.arjuna.ats.internal.jta.resources.jts.orbspecific.XAResourceRecord;

import com.arjuna.ats.jta.resources.LastResourceCommitOptimisation;
import com.arjuna.ats.jta.utils.XAHelper;

import org.omg.CosTransactions.*;

import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator;

import com.arjuna.ats.jta.xa.*;
import com.arjuna.ats.jta.common.Configuration;
import com.arjuna.ats.jta.common.Environment;
import com.arjuna.ats.jta.logging.*;
import com.arjuna.ats.jts.common.jtsPropertyManager;

import com.arjuna.ats.internal.jta.xa.TxInfo;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ControlWrapper;

import com.arjuna.common.util.logging.*;

import javax.transaction.xa.*;

import com.arjuna.ats.arjuna.common.*;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;

import java.util.Hashtable;
import java.util.Enumeration;

import javax.transaction.RollbackException;
import java.lang.SecurityException;
import java.lang.IllegalStateException;
import org.omg.CosTransactions.SubtransactionsUnavailable;
import org.omg.CosTransactions.NoTransaction;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.UNKNOWN;

/**
 * An implementation of javax.transaction.Transaction.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: TransactionImple.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 *
 * @message com.arjuna.ats.internal.jta.transaction.jts.xaerror
 *          [com.arjuna.ats.internal.jta.transaction.jts.xaerror] {0} caught XA
 *          exception: {1}
 * @message com.arjuna.ats.internal.jta.transaction.jts.inactivetx
 *          [com.arjuna.ats.internal.jta.transaction.jts.inactivetx] Transaction
 *          is not active.
 * @message com.arjuna.ats.internal.jta.transaction.jts.invalidtx2
 *          [com.arjuna.ats.internal.jta.transaction.jts.invalidtx2] Invalid
 *          transaction.
 * @message com.arjuna.ats.internal.jta.transaction.jts.nox
 *          [com.arjuna.ats.internal.jta.transaction.jts.notx] No such
 *          transaction.
 * @message com.arjuna.ats.internal.jta.transaction.jts.wrongstatetx
 *          [com.arjuna.ats.internal.jta.transaction.jts.wrongstatetx] The
 *          current transaction does not match this transaction!
 * @message com.arjuna.ats.internal.jta.transaction.jts.nullparam
 *          [com.arjuna.ats.internal.jta.transaction.jts.nullparam] paramater is
 *          null!
 * @message com.arjuna.ats.internal.jta.transaction.jts.illegalstate
 *          [com.arjuna.ats.internal.jta.transaction.jts.illegalstate] - illegal
 *          resource state:
 * @message com.arjuna.ats.internal.jta.transaction.jts.syncproblem
 *          [com.arjuna.ats.internal.jta.transaction.jts.syncproble] - cleanup
 *          synchronization failed to register:
 * @message com.arjuna.ats.internal.jta.transaction.jts.lastResourceOptimisationInterface
 * 			[com.arjuna.ats.internal.jta.transaction.jts.lastResourceOptimisationInterface] - failed
 *          to load Last Resource Optimisation Interface
 *
 * @message com.arjuna.ats.internal.jta.transaction.jts.lastResource.multipleWarning
 *          [com.arjuna.ats.internal.jta.transaction.jts.lastResource.multipleWarning]
 *          Multiple last resources have been added to the current transaction.
 *          This is transactionally unsafe and should not be relied upon.
 *          Current resource is {0}
 * @message com.arjuna.ats.internal.jta.transaction.jts.lastResource.disallow
 *          [com.arjuna.ats.internal.jta.transaction.jts.lastResource.disallow]
 *          Adding multiple last resources is disallowed.
 *          Current resource is {0}
 * @message com.arjuna.ats.internal.jta.transaction.jts.lastResource.startupWarning
 *          [com.arjuna.ats.internal.jta.transaction.jts.lastResource.startupWarning]
 *          You have chosen to enable multiple last resources in the transaction manager.
 *          This is transactionally unsafe and should not be relied upon.
 */

public class TransactionImple implements javax.transaction.Transaction,
		com.arjuna.ats.jta.transaction.Transaction
{

	public TransactionImple () throws SubtransactionsUnavailable
	{
		this(new AtomicTransaction());

		_theTransaction.begin();

		try
		{
			TwoPhaseCoordinator theTx = null;

			try
			{
				/*
				 * If this is an imported transaction and we have just performed
				 * interposition, then register a purely local Synchronization.
				 * This gets us over a performance issue with JacORB.
				 */

				theTx = (TwoPhaseCoordinator) BasicAction.Current();

				if (theTx != null)  // TM is local
					theTx.addSynchronization(new LocalCleanupSynchronization(this));
				else
					registerSynchronization(new CleanupSynchronization(this));
			}
			catch (ClassCastException ex)
			{
				/*
				 * Not a local/interposed transaction.
				 */

				registerSynchronization(new CleanupSynchronization(this));
			}
		}
		catch (Exception ex)
		{
			/*
			 * Could set rollback only, but let's take the memory leak hit for
			 * now.
			 */

			jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.syncproblem", ex);
		}
	}

	/**
	 * Overloads Object.equals()
	 */

	public boolean equals (Object obj)
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.OPERATORS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.equals");
		}

		if (obj == null)
			return false;

		if (obj == this)
			return true;

		if (obj instanceof TransactionImple)
		{
			if (_theTransaction != null)
			{
				return _theTransaction.equals(((TransactionImple) obj)._theTransaction);
			}
		}

		return false;
	}

	/**
	 * Return -1 if we fail.
	 */

	public int hashCode ()
	{
		if (_theTransaction != null)
			return _theTransaction.hashCode();
		else
			return -1;
	}

	public ControlWrapper getControlWrapper ()
	{
		if (_theTransaction != null)
			return _theTransaction.getControlWrapper();
		else
			return null;
	}

	/**
	 * The JTA specification is vague on whether the calling thread can have any
	 * transaction associated with it. It does say that it need not have the
	 * same transaction as this one. We could call suspend prior to making these
	 * calls, but for now we do nothing, and simply treat it like a Control.
	 */

	/**
	 * We will never throw a HeuristicRollbackException because if we get a
	 * HeuristicRollback from a resource, and can successfully rollback the
	 * other resources, this is then the same as having simply been forced to
	 * rollback the transaction during phase 1. The OTS interfaces do not allow
	 * a differentiation.
	 */

	public void commit () throws javax.transaction.RollbackException,
			javax.transaction.HeuristicMixedException,
			javax.transaction.HeuristicRollbackException,
			java.lang.SecurityException, javax.transaction.SystemException,
			java.lang.IllegalStateException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.commit");
		}

		if (_theTransaction != null)
		{
			/*
			 * Call end on any suspended resources. If this fails, then the
			 * transaction will be rolled back.
			 */

			try
			{
				if (!endSuspendedRMs())
					_theTransaction.rollbackOnly();

				_theTransaction.end(true);
			}
			catch (org.omg.CosTransactions.WrongTransaction wt)
			{
				throw new IllegalStateException(
						jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.wrongstatetx"));
			}
			catch (org.omg.CosTransactions.NoTransaction e1)
			{
				throw new IllegalStateException(
						jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.notx"));
			}
			catch (org.omg.CosTransactions.HeuristicMixed e2)
			{
				throw new javax.transaction.HeuristicMixedException();
			}
			catch (org.omg.CosTransactions.HeuristicHazard e3)
			{
				throw new javax.transaction.HeuristicMixedException();
			}
			catch (TRANSACTION_ROLLEDBACK e4)
			{
				throw new RollbackException(e4.toString());
			}
			catch (org.omg.CORBA.NO_PERMISSION e5)
			{
				throw new SecurityException();
			}
			catch (INVALID_TRANSACTION e6)
			{
				throw new IllegalStateException(
						jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.invalidtx2"));
			}
			catch (org.omg.CORBA.SystemException e7)
			{
				throw new javax.transaction.SystemException();
			}
			finally
			{
				TransactionImple.removeTransaction(this);
			}
		}
		else
			throw new IllegalStateException(
					jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.inactivetx"));
	}

	/**
	 * @message com.arjuna.ats.internal.jta.transaction.jts.endsuspendfailed1
	 *          [com.arjuna.ats.internal.jta.transaction.jts.endsuspendfailed1]
	 *          Ending suspended RMs failed when rolling back the transaction!
	 * @message com.arjuna.ats.internal.jta.transaction.jts.endsuspendfailed2
	 *          [com.arjuna.ats.internal.jta.transaction.jts.endsuspendfailed2]
	 *          Ending suspended RMs failed when rolling back the transaction,
	 *          but transaction rolled back.
	 */

	public void rollback () throws java.lang.IllegalStateException,
			java.lang.SecurityException, javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.rollback");
		}

		if (_theTransaction != null)
		{
			/*
			 * Call end on any suspended resources. If this fails, then there's
			 * not a lot else we can do because the transaction is about to roll
			 * back anyway!
			 */

			boolean endSuspendedFailed = !endSuspendedRMs();

			if (endSuspendedFailed)
			{
				if (jtaLogger.loggerI18N.isWarnEnabled())
				{
					jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.endsuspendfailed1");
				}
			}

			try
			{
				_theTransaction.abort();
			}
			catch (org.omg.CosTransactions.WrongTransaction e1)
			{
				throw new IllegalStateException(
						jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.wrongstatetx"));
			}
			catch (org.omg.CORBA.NO_PERMISSION e2)
			{
				throw new SecurityException();
			}
			catch (INVALID_TRANSACTION e3)
			{
				throw new IllegalStateException(
						jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.invalidtx2"));
			}
			catch (NoTransaction e4)
			{
				throw new IllegalStateException(
						jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.notx"));
			}
			catch (org.omg.CORBA.SystemException e5)
			{
				throw new javax.transaction.SystemException();
			}
			finally
			{
				TransactionImple.removeTransaction(this);
			}

			if (endSuspendedFailed)
				throw new IllegalStateException(
						jtaLogger.logMesg.getString("com.arjuna.ats.internal.jta.transaction.jts.endsuspendfailed2"));
		}
		else
			throw new IllegalStateException(
					jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.inactivetx"));
	}

	public void setRollbackOnly () throws java.lang.IllegalStateException,
			javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.setRollbackOnly");
		}

		if (_theTransaction != null)
		{
			try
			{
				_theTransaction.rollbackOnly();
			}
			catch (org.omg.CosTransactions.NoTransaction e3)
			{
				throw new IllegalStateException(
						jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.notx"));
			}
			catch (org.omg.CORBA.SystemException e4)
			{
				throw new javax.transaction.SystemException();
			}
		}
		else
			throw new IllegalStateException(
					jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.inactivetx"));
	}

	public int getStatus () throws javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.getStatus");
		}

		int status = javax.transaction.Status.STATUS_NO_TRANSACTION;

		if (_theTransaction != null)
		{
			try
			{
				return StatusConverter.convert(_theTransaction.get_status());
			}
			catch (org.omg.CORBA.SystemException e2)
			{
				throw new javax.transaction.SystemException();
			}
		}

		return status;
	}

	/**
	 * @message com.arjuna.ats.internal.jta.transaction.jts.syncerror
	 *          [com.arjuna.ats.internal.jta.transaction.jts.syncerror]
	 *          Synchronizations are not allowed!
	 */

	public void registerSynchronization (javax.transaction.Synchronization sync)
			throws javax.transaction.RollbackException,
			java.lang.IllegalStateException, javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.registerSynchronization");
		}

		if (sync == null)
			throw new javax.transaction.SystemException(
					"TransactionImple.registerSynchronization - "
							+ jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.nullparam"));

		if (_theTransaction != null)
		{
			try
			{
				SynchronizationImple s = new SynchronizationImple(sync);

				_theTransaction.registerSynchronization(s.getSynchronization());
			}
			catch (TRANSACTION_ROLLEDBACK e2)
			{
				throw new javax.transaction.RollbackException(e2.toString());
			}
			catch (org.omg.CosTransactions.Inactive e3)
			{
				throw new IllegalStateException(
						jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.inactivetx"));
			}
			catch (org.omg.CosTransactions.SynchronizationUnavailable e4)
			{
				throw new IllegalStateException(
						jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.syncerror"));
			}
			catch (INVALID_TRANSACTION e5)
			{
				throw new IllegalStateException(
						jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.invalidtx2"));
			}
			catch (org.omg.CORBA.SystemException e6)
			{
				throw new javax.transaction.SystemException();
			}
		}
		else
			throw new IllegalStateException(
					jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.inactivetx"));
	}

	public boolean enlistResource (XAResource xaRes) throws RollbackException,
			IllegalStateException, javax.transaction.SystemException
	{
		return enlistResource(xaRes, null);
	}

	/**
	 * @message com.arjuna.ats.internal.jta.transaction.jts.starterror
	 *          [com.arjuna.ats.internal.jta.transaction.jts.starterror] {0}
	 *          returned XA error {1} for transaction {2}
	 * @message com.arjuna.ats.internal.jta.transaction.jts.regerror
	 *          [com.arjuna.ats.internal.jta.transaction.jts.regerror] {0} could
	 *          not register transaction: {1}
	 * @message com.arjuna.ats.internal.jta.transaction.jts.markedrollback
	 * 			[com.arjuna.ats.internal.jta.transaction.jts.markedrollback] Could not
	 * 			enlist resource because the transaction is marked for rollback.
	 */

	public boolean enlistResource (XAResource xaRes, Object[] params)
			throws RollbackException, IllegalStateException,
			javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.enlistResource ( "
					+ xaRes + " )");
		}

		if (xaRes == null)
			throw new javax.transaction.SystemException(
					"TransactionImple.enlistResource - "
							+ jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.nullparam"));

		int status = getStatus();

		switch (status)
		{
		case javax.transaction.Status.STATUS_MARKED_ROLLBACK:
			throw new RollbackException("TransactionImple.enlistResource - "
										+ jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.markedrollback"));
		case javax.transaction.Status.STATUS_ACTIVE:
			break;
		default:
			throw new IllegalStateException(
					jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.inactivetx"));
		}

		XAModifier theModifier = null;

		if (params != null)
		{
			if (params.length >= XAMODIFIER + 1)
			{
				if (params[XAMODIFIER] instanceof XAModifier)
				{
					theModifier = (XAModifier) params[XAMODIFIER];
				}
			}
		}

		try
		{
			/*
			 * For each transaction we maintain a list of resources registered
			 * with it. Each element on this list also contains a list of
			 * threads which have registered this resource, and what their XID
			 * was for that registration.
			 */

			TxInfo info = null;

			/*
			 * Have we seen this specific resource instance before? Do this
			 * trawl first before checking the RM instance later. Saves time.
			 */

			try
			{
				synchronized (this)
				{
					info = (TxInfo) _resources.get(xaRes);

					if (info == null)
					{
						/*
						 * Null info means it's not in the main resources list,
						 * but may be in the duplicates.
						 */

						info = (TxInfo) _duplicateResources.get(xaRes);
					}
				}

				if (info != null)
				{
					switch (info.getState())
					{
					case TxInfo.ASSOCIATION_SUSPENDED:
					{
						/*
						 * Have seen resource before, so do a resume. The
						 * Resource instance will still be registered with the
						 * transaction though.
						 */

						xaRes.start(info.xid(), XAResource.TMRESUME);

						info.setState(TxInfo.ASSOCIATED);

						synchronized (this)
						{
							_suspendCount--;
						}

						return true; // already registered resource with this
						// transaction!
					}
					case TxInfo.ASSOCIATED:
					{
						/*
						 * Already active on this transaction.
						 */

						return true;
					}
					case TxInfo.NOT_ASSOCIATED:
					{
						/*
						 * Resource was associated, but was presumably delisted.
						 */
						xaRes.start(info.xid(), XAResource.TMJOIN);

						info.setState(TxInfo.ASSOCIATED);

						return true;
					}
					default:
					{
						// Note: this exception will be caught by our catch
						// block
						throw new IllegalStateException(
								"TransactionImple.enlistResource - "
										+ jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.illegalstate")
										+ info.getState());
					}
					}
				}
			}
			catch (IllegalStateException ex)
			{
				throw ex; // we threw it in the first place
			}
			catch (XAException exp)
			{
				if (info != null)
					info.setState(TxInfo.FAILED);

				if (jtaLogger.loggerI18N.isWarnEnabled())
				{
					jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.xaerror", new Object[]
					{ "TransactionImple.enlistResource", XAHelper.printXAErrorCode(exp) });
				}

				return false;
			}

			//	    if (threadIsActive(xaRes))
			//		return true; // this thread has already registered a resource for
			// this db

			/*
			 * We definitely haven't seen this specific resource instance
			 * before, but that doesn't mean that we haven't seen the RM it is
			 * connected to.
			 */

			Xid xid = null;
			TxInfo existingRM = isNewRM(xaRes);

			if (existingRM == null)
			{
				/*
				 * New RM, so create xid with new branch.
				 */

				boolean branchRequired = true;

				synchronized (this)
				{
					if (_resources.size() == 0)// first ever, so no need for
					// branch
					{
						//branchRequired = false;
						branchRequired = true;
					}
				}

				xid = createXid(branchRequired, theModifier);

				boolean associatedWork = false;
				int retry = 20;

				/*
				 * If another process has (or is about to) create the same
				 * transaction association then we will probably get a failure
				 * during start with XAER_DUPID. We know this must be due to
				 * another server, since we keep track of our own registrations.
				 * So, if this happens we create a new transaction branch and
				 * try again.
				 *
				 * To save time we could always just create branches by default.
				 *
				 * Is there a benefit to a zero branch?
				 */

				while (!associatedWork)
				{
					try
					{
                        if (_xaTransactionTimeoutEnabled)
                        {
                            int timeout = _theTransaction.getTimeout();

                            if (timeout > 0)
                            {
                                try
                                {
                                    xaRes.setTransactionTimeout(timeout);
                                }
                                catch (XAException te)
                                {
                                    if (jtaLogger.loggerI18N.isWarnEnabled())
                                    {
                                        jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.timeouterror", new Object[]
                                        { "TransactionImple.enlistResource", XAHelper.printXAErrorCode(te), xid });
                                    }
                                }
                            }
                        }

						xaRes.start(xid, XAResource.TMNOFLAGS);

						associatedWork = true;

						_resources.put(xaRes, new TxInfo(xid));
					}
					catch (XAException e)
					{
						// transaction already created by another server

						/* We get this from Oracle instead of DUPID. */
						if (e.errorCode == XAException.XAER_RMERR)
						{

							if (retry > 0)
								xid = createXid(true, theModifier);

							retry--;
						}
						else
							if (e.errorCode == XAException.XAER_DUPID)
							{
								if (retry > 0)
									xid = createXid(true, theModifier);

								retry--;
							}
							else
							{
								/*
								 * Can't do start, so set transaction to
								 * rollback only.
								 */

								if (jtaLogger.loggerI18N.isWarnEnabled())
								{
									jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.starterror", new Object[]
									{ "TransactionImple.enlistResource - XAResource.start", XAHelper.printXAErrorCode(e), xid });
								}

								markRollbackOnly();

								throw e;
							}

						if (retry < 0)
						{
							if (jtaLogger.loggerI18N.isWarnEnabled())
							{
								jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.starterror", new Object[]
								{ "TransactionImple.enlistResource - XAResource.start", XAHelper.printXAErrorCode(e), xid });
							}

							markRollbackOnly();

							throw new UNKNOWN();
						}
					}
				}
			}
			else
			{
				/*
				 * Have seen this RM before, so ignore this instance. The first
				 * registered RM instance will be used to drive the transaction
				 * completion. We add it to the duplicateResource list so we can
				 * delist it correctly later though.
				 */

				/*
				 * Re-create xid.
				 */

				xid = existingRM.xid();

				try
				{
					xaRes.start(xid, XAResource.TMJOIN);
				}
				catch (XAException ex)
				{
					if (jtaLogger.loggerI18N.isWarnEnabled())
					{
						jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.xaerror", new Object[]
						{ "TransactionImple.enlistResource - xa_start: ", XAHelper.printXAErrorCode(ex) });
					}

					markRollbackOnly();

					throw ex;
				}

				/*
				 * Add to duplicate resources list so we can keep track of it
				 * (particularly if we later have to delist).
				 */

				_duplicateResources.put(xaRes, new TxInfo(xid));

				return true;
			}

			/*
			 * Control and Coordinator should be set, or we would not have
			 * gotten this far!
			 */

			final XAResourceRecord res ;
                        if ((xaRes instanceof LastResourceCommitOptimisation) ||
                                ((LAST_RESOURCE_OPTIMISATION_INTERFACE != null) && LAST_RESOURCE_OPTIMISATION_INTERFACE.isInstance(xaRes)))
                        {
                            if (lastResourceCount == 1)
                            {
                                if (jtaLogger.loggerI18N.isWarnEnabled())
                                {
                                    if (ALLOW_MULTIPLE_LAST_RESOURCES)
                                    {
                                        jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.lastResource.multipleWarning", new Object[] {xaRes});
                                    }
                                    else
                                    {
                                        jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.lastResource.disallow", new Object[] {xaRes});
                                    }
                                }
                            }

                            if ((lastResourceCount++ == 0) || ALLOW_MULTIPLE_LAST_RESOURCES)
                            {
                                res = new LastResourceRecord(this, xaRes, xid,
                                        params) ;
                            }
                            else
                            {
                                markRollbackOnly() ;
                                return false ;
                            }
                        }
                        else
                        {
                            res = new XAResourceRecord(this, xaRes, xid,
                                    params);
                        }

			RecoveryCoordinator recCoord = _theTransaction.registerResource(res.getResource());

			//	    if (recCoord == null)
			//		throw new BAD_OPERATION();

			res.setRecoveryCoordinator(recCoord);

			return true;
		}
		catch (Exception e)
		{
			/*
			 * Some exceptional condition arose and we probably could not enlist
			 * the resouce. So, for safety mark the transaction as rollback
			 * only.
			 */

			markRollbackOnly();

			return false;
		}
	}

	/*
	 * Do we have to unregister resources? Assume not as it would not make much
	 * sense otherwise!
	 */

	/**
	 * @message com.arjuna.ats.internal.jta.transaction.jts.unknownres
	 *          [com.arjuna.ats.internal.jta.transaction.jts.unknownres] {0}
	 *          attempt to delist unknown resource!
	 * @message com.arjuna.ats.internal.jta.transaction.jts.delistfailed
	 *          [com.arjuna.ats.internal.jta.transaction.jts.delistfailed]
	 *          Delist of resource failed with: {0}
	 * @message com.arjuna.ats.internal.jta.transaction.jts.ressusp resource
	 *          [com.arjuna.ats.internal.jta.transaction.jts.ressusp resource]
	 *          is already suspended!
	 */

	public boolean delistResource (XAResource xaRes, int flags)
			throws IllegalStateException, javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.delistResource ( "
					+ xaRes + " )");
		}

		if (xaRes == null)
			throw new javax.transaction.SystemException(
					"TransactionImple.delistResource - "
							+ jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.nullparam"));

		int status = getStatus();

		switch (status)
		{
		case javax.transaction.Status.STATUS_ACTIVE:
			break;
		case javax.transaction.Status.STATUS_MARKED_ROLLBACK:
			break;
		default:
			throw new IllegalStateException(
					jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.inactivetx"));
		}

		TxInfo info = null;

		try
		{
			synchronized (this)
			{
				info = (TxInfo) _resources.get(xaRes);

				if (info == null)
					info = (TxInfo) _duplicateResources.get(xaRes);
			}

			if (info == null)
			{
				if (jtaLogger.loggerI18N.isWarnEnabled())
				{
					jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.unknownres", new Object[]
					{ "TransactionImple.delistResource" });
				}

				return false;
			}
			else
			{
				boolean optimizedRollback = false;

				try
				{
					/*
					 * If we know the transaction is going to rollback, then we
					 * can try to rollback the RM now. Just an optimisation.
					 */

					if (status == javax.transaction.Status.STATUS_MARKED_ROLLBACK)
					{
						if (XAUtils.canOptimizeDelist(xaRes))
						{
							xaRes.end(info.xid(), XAResource.TMFAIL);
							xaRes.rollback(info.xid());

							info.setState(TxInfo.OPTIMIZED_ROLLBACK);

							optimizedRollback = true;
						}
					}
				}
				catch (Exception e)
				{
					// failed, so try again when transaction does rollback
				}

				switch (info.getState())
				{
				case TxInfo.ASSOCIATED:
				{
					if ((flags & XAResource.TMSUCCESS) != 0)
					{
						xaRes.end(info.xid(), XAResource.TMSUCCESS);
						info.setState(TxInfo.NOT_ASSOCIATED);
					}
					else
					{
						if ((flags & XAResource.TMSUSPEND) != 0)
						{
							xaRes.end(info.xid(), XAResource.TMSUSPEND);
							info.setState(TxInfo.ASSOCIATION_SUSPENDED);

							synchronized (this)
							{
								_suspendCount++;
							}
						}
						else
						{
							xaRes.end(info.xid(), XAResource.TMFAIL);
							info.setState(TxInfo.FAILED);
						}
					}
				}
					break;
				case TxInfo.ASSOCIATION_SUSPENDED:
				{
					if ((flags & XAResource.TMSUCCESS) != 0)
					{
						// Oracle barfs if we don't resume first, despite what
						// XA says!

						if (XAUtils.mustEndSuspendedRMs(xaRes))
							xaRes.start(info.xid(), XAResource.TMRESUME);

						xaRes.end(info.xid(), XAResource.TMSUCCESS);
						info.setState(TxInfo.NOT_ASSOCIATED);

						synchronized (this)
						{
							_suspendCount--;
						}
					}
					else
					{
						if ((flags & XAResource.TMSUSPEND) != 0)
						{
							// Note: this exception will be caught by our catch
							// block

							throw new IllegalStateException(
									"TransactionImple.delistResource - "
											+ jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.ressusp"));
						}
						else
						{
							xaRes.end(info.xid(), XAResource.TMFAIL);
							info.setState(TxInfo.FAILED);

							synchronized (this)
							{
								_suspendCount--;
							}
						}
					}
				}
					break;
				default:
					if (!optimizedRollback)
						throw new IllegalStateException(
								"TransactionImple.delistResource - "
										+ jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.illegalstate")
										+ info.getState());
				}

				info = null;

				return true;
			}
		}
		catch (IllegalStateException ex)
		{
			throw ex;
		}
		catch (XAException exp)
		{
			if (info != null)
				info.setState(TxInfo.FAILED);

			/*
			 * For safety mark the transaction as rollback only.
			 */

			markRollbackOnly();

			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.xaerror", new Object[]
				{ "TransactionImple.delistResource", XAHelper.printXAErrorCode(exp) });
			}

			return false;
		}
		catch (Exception e)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.delistfailed", new Object[]
				{ e });
			}

			/*
			 * Some exception occurred and we probably could not delist the
			 * resource. So, for safety mark the transaction as rollback only.
			 */

			markRollbackOnly();

			return false;
		}
	}

	public final Uid get_uid ()
	{
		return _theTransaction.get_uid();
	}

	public String toString ()
	{
		if (_theTransaction == null)
			return "TransactionImple < jts, NoTransaction >";
		else
		{
			return "TransactionImple < jts, " + _theTransaction.get_uid()
					+ " >";
		}
	}

	public int getXAResourceState (XAResource xaRes)
	{
		int state = TxInfo.UNKNOWN;

		if (xaRes != null)
		{
			TxInfo info = (TxInfo) _resources.get(xaRes);

			if (info == null)
			{
				info = (TxInfo) _duplicateResources.get(xaRes);
			}

			if (info != null)
				state = info.getState();
		}

		return state;
	}

	/**
	 * Creates if does not exist and adds to our internal mapping table.
	 *
	 * @message com.arjuna.ats.internal.jta.transaction.jts.nottximple
	 *          [com.arjuna.ats.internal.jta.transaction.jts.nottximple] Current
	 *          transaction is not a TransactionImple
	 */

	static final TransactionImple getTransaction ()
	{
		TransactionImple tx = null;

		ControlWrapper otx = OTSImpleManager.current().getControlWrapper();

		if (otx != null)
		{
            synchronized (TransactionImple._transactions)
            {
				try
				{
					tx = (TransactionImple) TransactionImple._transactions.get(otx.get_uid());

					if (tx == null)
					{
						/*
						 * If it isn't active then don't add it to the
						 * hashtable.
						 */

						tx = new TransactionImple(new AtomicTransaction(otx));

						try
						{
							if (tx.getStatus() == javax.transaction.Status.STATUS_ACTIVE)
							{
								putTransaction(tx);
							}
						}
						catch (Exception ex)
						{
							// shouldn't happen!
						}
					}
				}
				catch (ClassCastException ex)
				{
					if (jtaLogger.loggerI18N.isWarnEnabled())
					{
						jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.nottximple");
					}
				}
			}
		}

		return tx;
	}

	public final void shutdown ()
	{
		removeTransaction(this);
	}

	protected TransactionImple (AtomicTransaction tx)
	{
		_theTransaction = tx;

		if (tx != null)
		{
			_resources = new Hashtable();
			_duplicateResources = new Hashtable();
		}
		else
		{
			_resources = null;
			_duplicateResources = null;
		}

		_suspendCount = 0;

		try
		{
			if (getStatus() == javax.transaction.Status.STATUS_ACTIVE)
			{
				TwoPhaseCoordinator theTx = null;

				try
				{
					/*
					 * If this is an imported transaction and we have just
					 * performed interposition, then register a purely local
					 * Synchronization. This gets us over a performance issue
					 * with JacORB.
					 */

					theTx = (TwoPhaseCoordinator) BasicAction.Current();

					if (theTx != null)
						theTx.addSynchronization(new LocalCleanupSynchronization(
								this));
					else
						registerSynchronization(new CleanupSynchronization(this));
				}
				catch (ClassCastException ex)
				{
					/*
					 * Not a local/interposed transaction.
					 */

					registerSynchronization(new CleanupSynchronization(this));
				}
			}
		}
		catch (Exception ex)
		{
			/*
			 * Could set rollback only, but let's take the possible memory leak
			 * hit for now.
			 */

			jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.syncproblem", ex);
		}
		_xaTransactionTimeoutEnabled = getXATransactionTimeoutEnabled() ;
	}

	protected void commitAndDisassociate ()
			throws javax.transaction.RollbackException,
			javax.transaction.HeuristicMixedException,
			javax.transaction.HeuristicRollbackException,
			java.lang.SecurityException, javax.transaction.SystemException,
			java.lang.IllegalStateException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.commitAndDisassociate");
		}

		if (_theTransaction != null)
		{
			try
			{
				_theTransaction.commit(true);
			}
			catch (org.omg.CosTransactions.WrongTransaction wt)
			{
				throw new IllegalStateException(
						jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.wrongstatetx"));
			}
			catch (org.omg.CosTransactions.NoTransaction e1)
			{
				throw new IllegalStateException(
						jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.notx"));
			}
			catch (org.omg.CosTransactions.HeuristicMixed e2)
			{
				throw new javax.transaction.HeuristicMixedException();
			}
			catch (org.omg.CosTransactions.HeuristicHazard e3)
			{
				throw new javax.transaction.HeuristicMixedException();
			}
			catch (TRANSACTION_ROLLEDBACK e4)
			{
				throw new RollbackException(e4.toString());
			}
			catch (org.omg.CORBA.NO_PERMISSION e5)
			{
				throw new SecurityException();
			}
			catch (INVALID_TRANSACTION e6)
			{
				throw new IllegalStateException(
						jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.invalidtx2"));
			}
			catch (org.omg.CORBA.SystemException e7)
			{
				throw new javax.transaction.SystemException();
			}
			finally
			{
				TransactionImple.removeTransaction(this);
			}
		}
		else
			throw new IllegalStateException(
					jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.inactivetx"));
	}

	protected void rollbackAndDisassociate ()
			throws java.lang.IllegalStateException,
			java.lang.SecurityException, javax.transaction.SystemException
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.rollbackAndDisassociate");
		}

		if (_theTransaction != null)
		{
			try
			{
				_theTransaction.rollback();
			}
			catch (org.omg.CosTransactions.WrongTransaction e1)
			{
				throw new IllegalStateException(
						jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.wrongstatetx"));
			}
			catch (org.omg.CORBA.NO_PERMISSION e2)
			{
				throw new SecurityException();
			}
			catch (INVALID_TRANSACTION e3)
			{
				throw new IllegalStateException(
						jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.invalidtx2"));
			}
			catch (NoTransaction e4)
			{
				throw new IllegalStateException(
						jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.notx"));
			}
			catch (org.omg.CORBA.SystemException e5)
			{
				throw new javax.transaction.SystemException();
			}
			finally
			{
				TransactionImple.removeTransaction(this);
			}
		}
		else
			throw new IllegalStateException(
					jtaLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.inactivetx"));
	}

	/**
	 * If this is an imported JCA transaction, then this method will return the
	 * Xid we should pretend to be. Otherwise it'll return null and we will generate
	 * our own Xid.
	 *
	 * @return null for a pure ATS transaction, otherwise a valid JCA imported Xid.
	 */

	protected Xid baseXid ()
	{
		return null;
	}

	/*
	 * Add and remove transactions from list.
	 */

	protected static final void putTransaction (TransactionImple tx)
	{
		TransactionImple._transactions.put(tx.get_uid(), tx);
	}

	protected static final void removeTransaction (TransactionImple tx)
	{
		TransactionImple._transactions.remove(tx.get_uid());
	}

	/**
	 * If there are any suspended RMs then we should call end on them before the
	 * transaction is terminated.
	 *
	 * @message com.arjuna.ats.internal.jta.transaction.jts.xaenderror
	 *          [com.arjuna.ats.internal.jta.transaction.jts.xaenderror]
	 *          Could not call end on a suspended resource!
	 */

	protected boolean endSuspendedRMs ()
	{
		boolean result = true;

		if (_suspendCount > 0)
		{
			Enumeration el = _resources.keys();

			/*
			 * Loop over all registered resources. Those that are in a suspended
			 * state must have end called on them. If this fails, then we will
			 * eventually roll back the transaction, but we will continue down
			 * the list to try to end any other suspended resources.
			 */

			if (el != null)
			{
				try
				{
					while (el.hasMoreElements())
					{
						/*
						 * Get the XAResource in case we have to call end on it.
						 */

						XAResource xaRes = (XAResource) el.nextElement();
						TxInfo info = (TxInfo) _resources.get(xaRes);

						if (info.getState() == TxInfo.ASSOCIATION_SUSPENDED)
						{
							if (XAUtils.mustEndSuspendedRMs(xaRes))
								xaRes.start(info.xid(), XAResource.TMRESUME);

							xaRes.end(info.xid(), XAResource.TMSUCCESS);
							info.setState(TxInfo.NOT_ASSOCIATED);
						}
					}
				}
				catch (XAException ex)
				{
					if (jtaLogger.loggerI18N.isWarnEnabled())
					{
						jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.xaenderror");
					}

					result = false;
				}
			}

			/*
			 * do the same again for duplicate resources
			 */

			el = _duplicateResources.keys();

			if (el != null)
			{
				try
				{
					while (el.hasMoreElements())
					{
						/*
						 * Get the XAResource in case we have to call end on it.
						 */

						XAResource xaRes = (XAResource) el.nextElement();
						TxInfo info = (TxInfo) _duplicateResources.get(xaRes);

						if (info.getState() == TxInfo.ASSOCIATION_SUSPENDED)
						{
							if (XAUtils.mustEndSuspendedRMs(xaRes))
								xaRes.start(info.xid(), XAResource.TMRESUME);

							xaRes.end(info.xid(), XAResource.TMSUCCESS);
							info.setState(TxInfo.NOT_ASSOCIATED);
						}
					}
				}
				catch (XAException ex)
				{
					if (jtaLogger.loggerI18N.isWarnEnabled())
					{
						jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.xaenderror");
					}

					result = false;
				}
			}

            _suspendCount = 0;
		}

		return result;
	}

	/**
	 * If this thread has already registered a resource for the same db then
	 * don't use this copy. For some databases it would actually be ok for us to
	 * use the resource (at least to do an xa_start equivalent on it), but for
	 * Oracle 8.1.6 it causes their JDBC driver to crash!
	 *
	 * @message com.arjuna.ats.internal.jta.transaction.jts.threaderror
	 *          [com.arjuna.ats.internal.jta.transaction.jts.threaderror] Active
	 *          thread error:
	 */

	private final boolean threadIsActive (XAResource xaRes)
	{
		Thread t = Thread.currentThread();

		try
		{
			Enumeration el = _resources.keys();

			if (el != null)
			{
				while (el.hasMoreElements())
				{
					XAResource x = (XAResource) el.nextElement();

					if (x.isSameRM(xaRes))
					{
						TxInfo info = (TxInfo) _resources.get(x);

						if (info.thread() == t)
							return true;
					}
				}
			}

			el = _duplicateResources.keys();

			if (el != null)
			{
				while (el.hasMoreElements())
				{
					XAResource x = (XAResource) el.nextElement();

					if (x.isSameRM(xaRes))
					{
						TxInfo info = (TxInfo) _resources.get(x);

						if (info.thread() == t)
							return true;
					}
				}
			}
		}
		catch (Exception e)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.threaderror", e);
			}

			throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString());
		}

		return false;
	}

	/**
	 * isNewRM returns an existing TxInfo for the same RM, if present. Null
	 * otherwise.
	 *
	 * @message com.arjuna.ats.internal.jta.transaction.jts.rmerror
	 *          [com.arjuna.ats.internal.jta.transaction.jts.rmerror] An error
	 *          occurred while checking if this is a new resource manager:
	 */

	private final TxInfo isNewRM (XAResource xaRes)
	{
		try
		{
			synchronized (this)
			{
				Enumeration el = _resources.keys();

				if (el != null)
				{
					while (el.hasMoreElements())
					{
						XAResource x = (XAResource) el.nextElement();

						if (x.isSameRM(xaRes))
						{
							return (TxInfo) _resources.get(x);
						}
					}
				}

				el = _duplicateResources.keys();

				if (el != null)
				{
					while (el.hasMoreElements())
					{
						XAResource x = (XAResource) el.nextElement();

						if (x.isSameRM(xaRes))
						{
							return (TxInfo) _duplicateResources.get(x);
						}
					}
				}
			}
		}
		catch (XAException ex)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.xaerror", new Object[]
				{ "TransactionImple.isNewRM", XAHelper.printXAErrorCode(ex) });
			}

			throw new com.arjuna.ats.arjuna.exceptions.FatalError(ex.toString());
		}
		catch (Exception e)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.rmerror", e);
			}

			throw new com.arjuna.ats.arjuna.exceptions.FatalError(e.toString());
		}

		return null;
	}

	private final Xid createXid (boolean branch, XAModifier theModifier)
	{
		Xid jtaXid = baseXid();

		if (jtaXid != null)
			return jtaXid;

		try
		{
			com.arjuna.ats.arjuna.xa.XID jtsXid = _theTransaction.get_xid(branch);
			com.arjuna.ats.arjuna.xa.XID tempXid = new com.arjuna.ats.arjuna.xa.XID();

			tempXid.formatID = jtsXid.formatID;
			tempXid.gtrid_length = jtsXid.gtrid_length;
			tempXid.bqual_length = jtsXid.bqual_length;
			System.arraycopy(jtsXid.data, 0, tempXid.data, 0, tempXid.data.length);

			jtaXid = new com.arjuna.ats.jta.xa.XidImple(tempXid);

			if (theModifier != null)
			{
				try
				{
					jtaXid = theModifier.createXid((com.arjuna.ats.jta.xa.XidImple) jtaXid);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			return jtaXid;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * This method calls setRollbackOnly and catches any exceptions it may throw
	 * and issues a warning. We use this in places wherew we need to force the
	 * outcome of the transaction but already have an exception to throw back to
	 * the application, so a failure here will only be masked.
	 *
	 * @message com.arjuna.ats.internal.jta.transaction.jts.rollbackerror
	 *          [com.arjuna.ats.internal.jta.transaction.jts.rollbackerror] {0}
	 *          could not mark the transaction as rollback only: {1}
	 */

	private final void markRollbackOnly ()
	{
		try
		{
			if (_theTransaction != null)
			{
				try
				{
					_theTransaction.rollbackOnly();
				}
				catch (org.omg.CosTransactions.NoTransaction e3)
				{
					// ok
				}
				catch (org.omg.CORBA.SystemException e3)
				{
					throw new javax.transaction.SystemException();
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();

			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.rollbackerror", new Object[]
				{ "TransactionImple.markRollbackOnly -", ex });
			}
		}
	}

	private static boolean getXATransactionTimeoutEnabled()
	{
		final Boolean xaTransactionTimeoutEnabled = Configuration.getXATransactionTimeoutEnabled() ;
		if (xaTransactionTimeoutEnabled != null)
		{
			return xaTransactionTimeoutEnabled.booleanValue() ;
		}
		return XA_TRANSACTION_TIMEOUT_ENABLED ;
	}

	protected AtomicTransaction _theTransaction;

	private Hashtable _resources;
	private Hashtable _duplicateResources;
	private int _suspendCount;
	private final boolean _xaTransactionTimeoutEnabled ;

        /**
         * Count of last resources seen in this transaction.
         */
        private int lastResourceCount ;

        /**
         * Do we allow multiple last resources?
         */
        private static final boolean ALLOW_MULTIPLE_LAST_RESOURCES ;

	private static final boolean XA_TRANSACTION_TIMEOUT_ENABLED ;
	private static final Class LAST_RESOURCE_OPTIMISATION_INTERFACE ;

	static
	{
		final String xaTransactionTimeoutEnabled = jtsPropertyManager.propertyManager.getProperty(Environment.XA_TRANSACTION_TIMEOUT_ENABLED) ;
		if (xaTransactionTimeoutEnabled != null)
		{
			XA_TRANSACTION_TIMEOUT_ENABLED = Boolean.valueOf(xaTransactionTimeoutEnabled).booleanValue() ;
		}
		else
		{
			XA_TRANSACTION_TIMEOUT_ENABLED = true ;
		}
		final String lastResourceOptimisationInterfaceName = jtsPropertyManager.propertyManager.getProperty(Environment.LAST_RESOURCE_OPTIMISATION_INTERFACE) ;
		Class lastResourceOptimisationInterface = null ;
		if (lastResourceOptimisationInterfaceName != null)
		{
			try
			{
				lastResourceOptimisationInterface = Thread.currentThread().getContextClassLoader().loadClass(lastResourceOptimisationInterfaceName) ;
			}
			catch (final Throwable th)
			{
				if (jtaLogger.loggerI18N.isWarnEnabled())
				{
					jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.lastResourceOptimisationInterface",
						new Object[] {lastResourceOptimisationInterfaceName}, th);
				}
			}
		}
		LAST_RESOURCE_OPTIMISATION_INTERFACE = lastResourceOptimisationInterface ;

                final String allowMultipleLastResources = jtsPropertyManager.getPropertyManager().getProperty(Environment.ALLOW_MULTIPLE_LAST_RESOURCES) ;
                ALLOW_MULTIPLE_LAST_RESOURCES = (allowMultipleLastResources == null ? false : Boolean.valueOf(allowMultipleLastResources).booleanValue()) ;
                if (ALLOW_MULTIPLE_LAST_RESOURCES && jtaLogger.loggerI18N.isWarnEnabled())
                {
                    jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.transaction.jts.lastResource.startupWarning");
                }
	}

    private static ConcurrentHashMap _transactions = new ConcurrentHashMap();

}
