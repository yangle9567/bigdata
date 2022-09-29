package com.bcld.utils;

import java.util.Collection;
import java.util.List;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;

public class MyBatisUtils {
    public static List<String> selectForStringList(Collection inputCollection, String propertyName) {
        List<String> list = (List<String>) CollectionUtils.collect(inputCollection, new BeanToPropertyValueTransformer(propertyName));
        if (list.size() == 0) {
            list.add("");
        }
        return list;
    }
}
