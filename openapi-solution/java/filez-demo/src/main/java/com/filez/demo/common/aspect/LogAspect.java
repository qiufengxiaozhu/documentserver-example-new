package com.filez.demo.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LogAspect {

	@Pointcut("@annotation(com.filez.demo.common.aspect.Log)")
	public void logPointCut() {
	}

	@Before("logPointCut()")
	public void doBefore(JoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Log logAnnotation = signature.getMethod().getAnnotation(Log.class);

		String methodName = logAnnotation != null ? logAnnotation.value() : "未命名方法";

		log.info("===== 开始执行 [{}]: {}.{} =====",
				methodName,
				signature.getDeclaringTypeName(),
				signature.getName());
		log.info("入参内容==> {}", Arrays.toString(joinPoint.getArgs()));
	}

	@Around("logPointCut()")
	public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Log logAnnotation = signature.getMethod().getAnnotation(Log.class);

		String methodName = logAnnotation != null ? logAnnotation.value() : "未命名方法";
		// 执行原方法
		Object result = joinPoint.proceed();

		log.info("===== 执行完成 [{}] 出参内容==> {}", methodName, result);
		return result;
	}

	@AfterThrowing(pointcut = "logPointCut()", throwing = "ex")
	public void doAfterThrowing(Throwable ex) {
		log.error("方法执行异常==> ", ex);
	}
}
