package com.angcyo.contactspicker.util;

import android.text.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by angcyo on 2016-11-26.
 */

public class Reflect {

    /**
     * 从一个对象中, 获取指定的成员对象
     */
    public static Object getMember(Object target, String member) {
        return getMember(target.getClass(), target, member);
    }

    public static Object getMember(Class<?> cls, Object target, String member) {
        Object result = null;
        try {
            Field memberField = cls.getDeclaredField(member);
            memberField.setAccessible(true);
            result = memberField.get(target);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void setMember(Class<?> cls, Object target, String member, Object value) {
        try {
            Field memberField = cls.getDeclaredField(member);
            memberField.setAccessible(true);
            memberField.set(target, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setMember(Object target, String member, Object value) {
        setMember(target.getClass(), target, member, value);
    }

    /**
     * 获取调用堆栈上一级的方法名称
     */
    public static String getMethodName() {
        final StackTraceElement[] stackTraceElements = new Exception().getStackTrace();
        return stackTraceElements[1].getMethodName();
    }

    /**
     * 通过类对象，运行指定方法
     *
     * @param obj        类对象
     * @param methodName 方法名
     * @param params     参数值
     * @return 失败返回null
     */
    public static Object invokeMethod(Object obj, String methodName, Object[] params) {
        if (obj == null || TextUtils.isEmpty(methodName)) {
            return null;
        }

        Class<?> clazz = obj.getClass();
        try {
            Class<?>[] paramTypes = null;
            if (params != null) {
                paramTypes = new Class[params.length];
                for (int i = 0; i < params.length; ++i) {
                    paramTypes[i] = params[i].getClass();
                }
            }
            Method method = clazz.getMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(obj, params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 通过反射, 获取obj对象的 指定成员变量的值
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null || TextUtils.isEmpty(fieldName)) {
            return null;
        }

        Class<?> clazz = obj.getClass();
        while (clazz != Object.class) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (Exception e) {
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * 设置字段的值
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        if (obj == null || TextUtils.isEmpty(fieldName)) {
            return;
        }

        Class<?> clazz = obj.getClass();
        while (clazz != Object.class) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(obj, value);
                return;
            } catch (Exception e) {
            }
            clazz = clazz.getSuperclass();
        }
    }
}
