package com.disneystreaming.neutrino.annotation.scope;

import com.google.inject.ScopeAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The scope annotation for StreamingBatch scope,
 * which keeps the instance singleton per streaming batch per JVM
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RUNTIME)
@ScopeAnnotation
public @interface StreamingBatch {
}
