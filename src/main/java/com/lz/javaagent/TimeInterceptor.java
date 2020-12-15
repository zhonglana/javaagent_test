package com.lz.javaagent;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class TimeInterceptor  {

    @RuntimeType
    public static Object intercept(@Origin Method method, @SuperCall Callable<?> callable) throws Exception {
        String name = method.getName();
        if(!name.equals("main")){
            return callable.call();
        }
        long start = System.currentTimeMillis();
        try {
            // 原有函数执行
            return callable.call();
        } finally {
            System.out.println(method + ": took " + (System.currentTimeMillis() - start) + "ms");
        }
    }
}