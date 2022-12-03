package com.chrtc.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class InitMethodClass {

    public static  Map   generClassInstance(HashSet classSet) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Map  classInstanceMap=new HashMap();
        Iterator iterator = classSet.iterator();
        while (iterator.hasNext())
        {
            String className=(String) iterator.next();
            Class clz = Class.forName(className);
            Object obj = clz.newInstance();
            classInstanceMap.put(className,obj);
        };
        return classInstanceMap;
    }
}
