package com.cube.simple.aspect;

import java.util.Collection;
import java.util.Objects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.cube.simple.util.AESUtil;

@Aspect
@Component
public class AESCryptoAspect {

	@Autowired
	AESUtil aesUtil;
	
    // 암호화가 필요한 메서드에 붙이는 어노테이션
    @Pointcut("@annotation(com.cube.simple.aspect.AESEncrypt)")
    private void encryptPointcut() {}

    // 복호화가 필요한 메서드에 붙이는 어노테이션
    @Pointcut("@annotation(com.cube.simple.aspect.AESDecrypt)")
    private void decryptPointcut() {}

    // --------------------------
    // Before Advice: 입력 객체 필드 암호화
    // --------------------------
    @Before("encryptPointcut()")
    public void doEncrypt(JoinPoint jp) {
        for (Object arg : jp.getArgs()) {
            encryptFields(arg);
        }
    }

    // --------------------------
    // AfterReturning Advice: 반환 객체 필드 복호화
    // --------------------------
    @AfterReturning(pointcut = "decryptPointcut()", returning = "retVal")
    public Object doDecrypt(JoinPoint jp, Object retVal) {
        if (retVal instanceof Collection) {
            ((Collection<?>) retVal).forEach(this::decryptFields);
        } else {
            decryptFields(retVal);
        }
        return retVal;
    }

    // --------------------------
    // 필드 암호화 리플렉션 헬퍼
    // --------------------------
    private void encryptFields(Object target) {
        if (Objects.isNull(target)) return;
        ReflectionUtils.doWithFields(target.getClass(),
            field -> {
                field.setAccessible(true);
                Object val = field.get(target);
                if (val instanceof String) {
                    String enc = aesUtil.encrypt((String) val);
                    field.set(target, enc);
                }
            },
            field -> field.isAnnotationPresent(AESData.class)
        );
    }

    // --------------------------
    // 필드 복호화 리플렉션 헬퍼
    // --------------------------
    private void decryptFields(Object target) {
        if (Objects.isNull(target)) return;
        ReflectionUtils.doWithFields(target.getClass(),
            field -> {
                field.setAccessible(true);
                Object val = field.get(target);
                if (val instanceof String) {
                    String dec = aesUtil.decrypt((String) val);
                    field.set(target, dec);
                }
            },
            field -> field.isAnnotationPresent(AESData.class)
        );
    }
}
