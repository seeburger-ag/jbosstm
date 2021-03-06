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
package com.arjuna.webservices.wsba;

import java.text.MessageFormat;
import java.util.Map;

import javax.xml.namespace.QName;

import com.arjuna.webservices.logging.WSTLogger;
import com.arjuna.webservices.util.Enumerated;
import com.arjuna.webservices.util.InvalidEnumerationException;

/*
 * <xsd:simpleType name="StateType">
 *   <xsd:restriction base="xsd:QName">
 *     <xsd:enumeration value="wsba:Active"/>
 *     <xsd:enumeration value="wsba:Canceling"/>
 *     <xsd:enumeration value="wsba:Canceling-Active"/>
 *     <xsd:enumeration value="wsba:Canceling-Completing"/>
 *     <xsd:enumeration value="wsba:Completing"/>
 *     <xsd:enumeration value="wsba:Completed"/>
 *     <xsd:enumeration value="wsba:Closing"/>
 *     <xsd:enumeration value="wsba:Compensating"/>
 *     <xsd:enumeration value="wsba:Faulting"/>
 *     <xsd:enumeration value="wsba:Faulting-Active"/>
 *     <xsd:enumeration value="wsba:Faulting-Compensating"/>
 *     <xsd:enumeration value="wsba:Exiting"/>
 *     <xsd:enumeration value="wsba:Ended"/>
 *   </xsd:restriction>
 * </xsd:simpleType>
 */
/**
 * Class representing state enumerations.
 * @author kevin
 */
public class State extends Enumerated
{
    /**
     * Serial version UID for serialisation.
     */
    
    /**
     * The Active state.
     */
    public static final State STATE_ACTIVE = new State("Active") ;
    /**
     * The Canceling state.
     */
    public static final State STATE_CANCELING = new State("Canceling") ;
    /**
     * The Canceling-Active state.
     */
    public static final State STATE_CANCELING_ACTIVE = new State("Canceling-Active") ;
    /**
     * The Canceling-Completing state.
     */
    public static final State STATE_CANCELING_COMPLETING = new State("Canceling-Completing") ;
    /**
     * The Completing state.
     */
    public static final State STATE_COMPLETING = new State("Completing") ;
    /**
     * The Completed state.
     */
    public static final State STATE_COMPLETED = new State("Completed") ;
    /**
     * The Closing state.
     */
    public static final State STATE_CLOSING = new State("Closing") ;
    /**
     * The Compensating state.
     */
    public static final State STATE_COMPENSATING = new State("Compensating") ;
    /**
     * The Faulting state.
     */
    public static final State STATE_FAULTING = new State("Faulting") ;
    /**
     * The Faulting-Active state.
     */
    public static final State STATE_FAULTING_ACTIVE = new State("Faulting-Active") ;
    /**
     * The Faulting-Compensating state.
     */
    public static final State STATE_FAULTING_COMPENSATING = new State("Faulting-Compensating") ;
    /**
     * The Exiting state.
     */
    public static final State STATE_EXITING = new State("Exiting") ;
    /**
     * The Ended state.
     */
    public static final State STATE_ENDED = new State("Ended") ;
    
    /**
     * The map of enumerations.
     */
    private static final Map ENUM_MAP = generateMap(new Enumerated[] {
        STATE_ACTIVE, STATE_CANCELING, STATE_CANCELING_ACTIVE,
        STATE_CANCELING_COMPLETING, STATE_COMPLETING, STATE_COMPLETED,
        STATE_CLOSING, STATE_COMPENSATING, STATE_FAULTING,
        STATE_FAULTING_ACTIVE, STATE_FAULTING_COMPENSATING, STATE_EXITING,
        STATE_ENDED
    }) ;
    
    /**
     * Construct the state enumeration with the specified value.
     * @param localName The localName of the state enumeration.
     */
    private State(final String value)
    {
        super(getQualifiedName(value)) ;
    }
    
    /**
     * Get the value of this enumeration.
     * @return the value.
     */
    public QName getValue()
    {
        return (QName)getKey() ;
    }
    
    /**
     * Resolve the enumeration for the specified value.
     * @param value The value.
     * @return The enumeration.
     * @throws InvalidEnumerationException if the value is not valid.
     */
    protected Enumerated resolveEnum(final Object value)
        throws InvalidEnumerationException
    {
        return toState((QName)value) ;
    }
    
    /**
     * Return the enumeration for the specified value.
     * @param name The name.
     * @return The enumeration.
     * @throws InvalidEnumerationException if the name is not valid.
     */
    public static State toState(final String name)
        throws InvalidEnumerationException
    {
        return toState(getQualifiedName(name)) ;
    }
    
    /**
     * Return the enumeration for the specified value.
     * @param qName The qName value.
     * @return The enumeration.
     * @throws InvalidEnumerationException if the value is not valid.
     */
    public static State toState(final QName qName)
        throws InvalidEnumerationException
    {
        final Object state = ENUM_MAP.get(qName) ;
        if (state == null)
        {
            throw new InvalidEnumerationException(WSTLogger.i18NLogger.get_webservices_wsba_State_1(qName)) ;
        }
        return (State)state ;
    }
    
    /**
     * Get the qualified name.
     * @param value The name to qualify.
     * @return The qualified name.
     */
    private static QName getQualifiedName(final String name)
    {
        return new QName(BusinessActivityConstants.WSBA_NAMESPACE,
                name, BusinessActivityConstants.WSBA_PREFIX) ;
    }
}
