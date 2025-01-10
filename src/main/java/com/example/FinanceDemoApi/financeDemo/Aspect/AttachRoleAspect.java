package com.example.FinanceDemoApi.financeDemo.Aspect;

import com.example.FinanceDemoApi.financeDemo.Annotations.AttachRole;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;

@Aspect
@Component
public class AttachRoleAspect {

    @Around("@annotation(attachRole)")
    public Object attachRoleToRequestBody(ProceedingJoinPoint joinPoint, AttachRole attachRole) throws Throwable {

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();


        String roleFromHeader = request.getHeader("Role");

        if (roleFromHeader != null) {

            Object[] args = joinPoint.getArgs();


            for (Object arg : args) {
                if (arg != null) {
                    try {

                        Field roleField = arg.getClass().getDeclaredField("role");
                        roleField.setAccessible(true);
                        roleField.set(arg, roleFromHeader);
                    } catch (NoSuchFieldException e) {


                        System.out.println(e);

                    }
                }
            }
        }

        // Proceed with the execution of the method
        return joinPoint.proceed(joinPoint.getArgs());

    }
}
