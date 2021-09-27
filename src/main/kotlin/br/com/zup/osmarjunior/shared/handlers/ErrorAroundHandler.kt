package br.com.zup.osmarjunior.shared.handlers

import io.micronaut.aop.Around
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*

@MustBeDocumented
@Retention(RUNTIME)
@Target(CLASS, FIELD, TYPE)
@Around
annotation class ErrorAroundHandler