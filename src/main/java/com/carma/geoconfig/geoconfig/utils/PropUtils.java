package com.carma.geoconfig.geoconfig.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropUtils {

    private static PropUtils instance = null;
    private static Properties props = new Properties();

    static
    {
        try
        {
            InputStream in = new FileInputStream(Constants.getConfigFile());

            instance = new PropUtils(in);
            in.close();
        }
        catch (IOException  e)
        {
            e.printStackTrace();
        }
    }

    private PropUtils(){}

    /**
     *
     * @param inputStream
     * @throws java.io.IOException
     */
    private PropUtils(InputStream inputStream) throws IOException
    {
        try
        {
            props.load(inputStream);
        }finally {
			inputStream.close();
		}
       

    }


    /**
     *
     * @param prop
     * @param defaultVal
     * @return
     */
    public static String getVal(String prop, String defaultVal)
    {
        if (props.containsKey(prop))
        {
            return props.getProperty(prop, defaultVal).trim();
        }
        else
        {
            return defaultVal;
        }
    }


    /**
     *
     * @param prop
     * @return
     */
    public static String getVal(String prop)
    {
        if (props.containsKey(prop))
        {
            return (String)props.getProperty(prop).trim();
        }
        else
        {
            return null;
        }
    }



}
