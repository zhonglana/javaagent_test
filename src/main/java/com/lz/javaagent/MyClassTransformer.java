package com.lz.javaagent;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import org.slf4j.MDC;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class MyClassTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer) {
        // 操作DemoApplication类
        if ("com/example/demo/DemoApplication".equals(className)) {
            try {
                // 从ClassPool获得CtClass对象
                final ClassPool classPool = ClassPool.getDefault();
                final CtClass clazz = classPool.get("com.example.demo.DemoApplication");
                String methodName = "main";
                CtMethod ctMethod = clazz.getDeclaredMethod(methodName);
                //这里对 java.util.Date.convertToAbbr() 方法进行了改写，在 return之前增加了一个 打印操作

                //TODO 更改原方法名
                String newMethodName = methodName + "$old";
                ctMethod.setName(newMethodName);
                //TODO 复制原方法
                //TODO 定义新方法为原方法名
                CtMethod newMethod = CtNewMethod.copy(ctMethod, methodName, clazz, null);
                //TODO 新方法调用原方法
                String methodBody = "{String s = Long.toHexString(111111111111111L);" +
                        "org.slf4j.MDC.put(\"traceId\", s);" +
                        newMethodName + "($$);" +
                        "System.out.println(org.slf4j.MDC.get(\"traceId\"));" +
                        "System.out.println(s);}";
                newMethod.setBody(methodBody);

                //TODO 类添加新方法
                clazz.addMethod(newMethod);
                // 返回字节码，并且detachCtClass对象
                byte[] byteCode = clazz.toBytecode();
                //detach的意思是将内存中曾经被javassist加载过的Date对象移除，如果下次有需要在内存中找不到会重新走javassist加载
                clazz.detach();
                return byteCode;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // 如果返回null则字节码不会被修改
        return null;
    }
}