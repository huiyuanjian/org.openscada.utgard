/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2007 inavare GmbH (http://inavare.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openscada.opc.dcom.list.impl;

import java.net.UnknownHostException;

import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.core.JIArray;
import org.jinterop.dcom.core.JICallObject;
import org.jinterop.dcom.core.JIClsid;
import org.jinterop.dcom.core.JIFlags;
import org.jinterop.dcom.core.JIInterfacePointer;
import org.jinterop.dcom.core.JIPointer;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.win32.ComFactory;
import org.openscada.opc.dcom.common.impl.BaseCOMObject;
import org.openscada.opc.dcom.common.impl.EnumGUID;
import org.openscada.opc.dcom.common.impl.Helper;
import org.openscada.opc.dcom.list.ClassDetails;
import org.openscada.opc.dcom.list.Constants;

import rpc.core.UUID;

/**
 * This class implements the IOPCServerList (aka OPCEnum) service.
 * @author Jens Reimann &lt;jens.reimann@inavare.net&gt;
 *
 */
public class OPCServerList extends BaseCOMObject
{
    public OPCServerList ( IJIComObject listObject ) throws JIException
    {
        super ( (IJIComObject)listObject.queryInterface ( Constants.IOPCServerList_IID ) );
    }

    public JIClsid getCLSIDFromProgID ( String progId ) throws JIException
    {
        JICallObject callObject = new JICallObject ( getCOMObject ().getIpid (), true );
        callObject.setOpnum ( 2 );

        callObject.addInParamAsString ( progId, JIFlags.FLAG_REPRESENTATION_STRING_LPWSTR );
        callObject.addOutParamAsType ( UUID.class, JIFlags.FLAG_NULL );

        try
        {
            Object[] result = getCOMObject ().call ( callObject );
            return JIClsid.valueOf ( ( (UUID) ( result[0] ) ).toString () );
        }
        catch ( JIException e )
        {
            if ( e.getErrorCode () == 0x800401F3 )
            {
                return null;
            }
            throw e;
        }
    }

    /**
     * Return details about a serve class
     * @param clsId A server class
     * @throws JIException
     */
    public ClassDetails getClassDetails ( JIClsid clsId ) throws JIException
    {
        if ( clsId == null )
        {
            return null;
        }
        
        JICallObject callObject = new JICallObject ( getCOMObject ().getIpid (), true );
        callObject.setOpnum ( 1 );

        callObject.addInParamAsUUID ( clsId.getCLSID (), JIFlags.FLAG_NULL );

        callObject.addOutParamAsObject ( new JIPointer ( new JIString ( JIFlags.FLAG_REPRESENTATION_STRING_LPWSTR ) ),
                JIFlags.FLAG_NULL );
        callObject.addOutParamAsObject ( new JIPointer ( new JIString ( JIFlags.FLAG_REPRESENTATION_STRING_LPWSTR ) ),
                JIFlags.FLAG_NULL );

        Object[] result = Helper.callRespectSFALSE ( getCOMObject (), callObject );

        ClassDetails cd = new ClassDetails ();
        cd.setClsId ( clsId.getCLSID () );
        cd.setProgId ( ( (JIString) ( (JIPointer)result[0] ).getReferent () ).getString () );
        cd.setDescription ( ( (JIString) ( (JIPointer)result[1] ).getReferent () ).getString () );

        return cd;
    }

    /*
     HRESULT EnumClassesOfCategories(
     [in]                       ULONG        cImplemented,
     [in,size_is(cImplemented)] CATID        rgcatidImpl[],
     [in]                       ULONG        cRequired,
     [in,size_is(cRequired)]    CATID        rgcatidReq[],
     [out]                      IEnumGUID ** ppenumClsid
     );
     */

    public EnumGUID enumClassesOfCategories ( String[] implemented, String[] required ) throws IllegalArgumentException, UnknownHostException, JIException
    {
        UUID[] u1 = new UUID[implemented.length];
        UUID[] u2 = new UUID[required.length];

        for ( int i = 0; i < implemented.length; i++ )
        {
            u1[i] = new UUID ( implemented[i] );
        }

        for ( int i = 0; i < required.length; i++ )
        {
            u2[i] = new UUID ( required[i] );
        }

        return enumClassesOfCategories ( u1, u2 );
    }

    public EnumGUID enumClassesOfCategories ( UUID[] implemented, UUID[] required ) throws IllegalArgumentException, UnknownHostException, JIException
    {
        // ** CALL
        JICallObject callObject = new JICallObject ( getCOMObject ().getIpid (), true );
        callObject.setOpnum ( 0 );

        // ** IN
        callObject.addInParamAsInt ( implemented.length, JIFlags.FLAG_NULL );
        if ( implemented.length == 0 )
        {
            callObject.addInParamAsPointer ( new JIPointer ( null ), JIFlags.FLAG_NULL );
        }
        else
        {
            callObject.addInParamAsArray ( new JIArray ( implemented, true ), JIFlags.FLAG_NULL );
        }

        callObject.addInParamAsInt ( required.length, JIFlags.FLAG_NULL );
        if ( required.length == 0 )
        {
            callObject.addInParamAsPointer ( new JIPointer ( null ), JIFlags.FLAG_NULL );
        }
        else
        {
            callObject.addInParamAsArray ( new JIArray ( required, true ), JIFlags.FLAG_NULL );
        }

        // ** OUT
        callObject.addOutParamAsType ( JIInterfacePointer.class, JIFlags.FLAG_NULL );

        // ** RESULT
        Object result[] = Helper.callRespectSFALSE ( getCOMObject (), callObject );

        return new EnumGUID ( ComFactory.createCOMInstance ( getCOMObject (), (JIInterfacePointer)result[0] ) );
    }
}
