package com.cube.simple.aspect;

import java.util.Objects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.cube.simple.util.SHAUtil;

@Aspect
@Component
public class SHACryptoAspect {

	@Autowired
	SHAUtil shaUtil;
	
    @Pointcut("@annotation(com.cube.simple.aspect.SHAEncrypt)")
    private void encryptPointcut() {}

    @Before("encryptPointcut()")
    public void doEncrypt(JoinPoint jp) {
        for (Object arg : jp.getArgs()) {
            encryptFields(arg);
        }
    }

    private void encryptFields(Object target) {
        if (Objects.isNull(target)) return;
        ReflectionUtils.doWithFields(target.getClass(),
            field -> {
                field.setAccessible(true);
                Object val = field.get(target);
                if (val instanceof String) {
                    String enc = shaUtil.encrypt((String) val);
                    field.set(target, enc);
                }
            },
            field -> field.isAnnotationPresent(SHAData.class)
        );
    }
}
