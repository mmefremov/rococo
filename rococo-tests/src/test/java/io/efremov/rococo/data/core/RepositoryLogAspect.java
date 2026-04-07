package io.efremov.rococo.data.core;

import io.efremov.rococo.util.JsonUtils;
import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Slf4j
@Aspect
public class RepositoryLogAspect {

  @Around("execution(* io.efremov.rococo.data.repository..*(..))")
  public Object logResult(ProceedingJoinPoint joinPoint) throws Throwable {
    Object result = joinPoint.proceed();
    String methodName = joinPoint.getSignature().toShortString();
    String json = JsonUtils.writeValueAsString(result);
    log.info("Query result from {}:\n{}", methodName, json);
    Allure.addAttachment("result", "application/json", json, "json");

    return result;
  }
}
