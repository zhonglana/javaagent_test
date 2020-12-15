package com.lz.javaagent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class AgentMain {

    public static void agentmain(String args, Instrumentation inst){
        System.out.println("agentmain args: " + args);
        Class[] allLoadedClasses = inst.getAllLoadedClasses();
        for(Class clazz : allLoadedClasses){
            System.out.println(clazz.getName());
        }
    }

    public static void premain(String args, Instrumentation inst){
        System.out.println("premain args: " + args);

        // 添加Transformer
        inst.addTransformer(new MyClassTransformer());


        AgentBuilder.Transformer transformer = new AgentBuilder.Transformer() {
            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader) {
                return builder
                        .method(ElementMatchers.<MethodDescription>named("main")) // 拦截任意方法
                        .intercept(MethodDelegation.to(TimeInterceptor.class)); // 委托
            }
        };

        AgentBuilder.Listener listener = new AgentBuilder.Listener() {
            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, DynamicType dynamicType) {
                System.out.println("onTransformation: " + args);
            }

            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) { }

            public void onError(String typeName, ClassLoader classLoader, JavaModule module, Throwable throwable) { }

            public void onComplete(String typeName, ClassLoader classLoader, JavaModule module) {
                System.out.println("onComplete: " + args);
            }
        };

        new AgentBuilder
                .Default()
                .type(ElementMatchers.nameStartsWith("com.example.demo.DemoApplication")) // 指定需要拦截的类
                .transform(transformer)
                .with(listener)
                .installOn(inst);


    }
//
//
//    public static ElementMatcher<? super TypeDescription> buildMatch() {
//        ElementMatcher.Junction judge = new ElementMatcher.Junction() {
//            public boolean matches(Object target) {
//                return true;
//            }
//
//            public Junction or(ElementMatcher other) {
//                return new Conjunction(this, other);
//            }
//
//            public Junction and(ElementMatcher other) {
//                return new Disjunction(this, other);
//            }
//        };
//        judge = judge.and(not(isInterface())).and(not(isSetter()))
//                .and(nameStartsWithIgnoreCase("io.spring"))
//                .and(not(nameContainsIgnoreCase("util")))
//                .and(not(nameContainsIgnoreCase("interceptor")));
//
//        judge = judge.and(not(isSetter()));
//        return new ProtectiveShieldMatcher(judge);
//    }
}
