/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author JBoss, a division of Red Hat.
 */
package com.arjuna.common.util;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import java.util.Properties;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

/**
 * Utility class providing access to build time and runtime configuration reporting functions.
 *
 * Replaces the old per-module Info (and in some cases Configuration and report) classes.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2009-10
 */
public class ConfigurationInfo
{
    /**
     * @see .getSourceId
     * @return the version, if known.
     */
    public static String getVersion() {
        return getSourceId();
    }

    /**
     * @return the version control tag of the source used, or "unknown".
     */
    public static String getSourceId() {
        return sourceId;
    }

    /**
     * @return the name (not path) of the properties file
     */
    public static String getPropertiesFile() {
        return propertiesFile;
    }

    /**
     * Print config info to stdout.
     * @param args unused
     */
    public static void main (String[] args)
    {
        // build time info:
        System.out.println("sourceId: "+getSourceId());
        System.out.println("propertiesFile: "+getPropertiesFile());

        // run time info (probably empty as beans only load on demand):
        String beans = BeanPopulator.printState();
        System.out.print(beans);
    }

    // initialize build time properties from data in the jar's META-INF/MANIFEST.MF
    private static void getBuildTimeProperties() {
        
        // our classloader's classpath may contain more than one .jar, each with a manifest.
        // we need to ensure we get our own .jar's manifest, even if the jar is not first on the path.
        String classFileName = ConfigurationInfo.class.getSimpleName()+".class";
        String pathToThisClass = ConfigurationInfo.class.getResource(classFileName).toString();
        int mark = pathToThisClass.indexOf("!") ;
        String pathToManifest = (pathToThisClass.substring(0,mark+1))+"/META-INF/MANIFEST.MF";

        InputStream is = null;
        try {
            is = new URL(pathToManifest).openStream();
            Manifest manifest = new Manifest(is);
            Attributes attributes = manifest.getMainAttributes();

            Attributes.Name name = new Attributes.Name("arjuna-properties-file");
            if(attributes.containsKey(name)) {
                propertiesFile = attributes.getValue(name);
            }

            name = new Attributes.Name("arjuna-scm-revision");
            if(attributes.containsKey(name)) {
                sourceId = attributes.getValue(name);
            }

        } catch(Exception exception) {
            exception.printStackTrace();
        } finally {
            if(is!= null) {
                try {
                    is.close();
                } catch(IOException e) {}
            }
        }
    }

    private static volatile String sourceId = "unknown";
    private static volatile String propertiesFile = "arjuna-properties.xml";

    static {
        getBuildTimeProperties();
    }
}