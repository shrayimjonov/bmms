package uz.paykap.aop.common;

import uz.devops.bmcore.enumeration.RequestType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogRequest {

    RequestType type() default RequestType.CHECK;

    String name() default "log-request";

    int amountRatio() default 100;

    int currency() default 860;

}
