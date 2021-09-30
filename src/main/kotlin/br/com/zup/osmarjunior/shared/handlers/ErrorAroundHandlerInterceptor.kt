package br.com.zup.osmarjunior.shared.handlers

import br.com.zup.osmarjunior.exceptions.ChavePixExistenteException
import br.com.zup.osmarjunior.exceptions.ChavePixNaoEncontradaException
import br.com.zup.osmarjunior.exceptions.OperacaoNaoPermitidaException
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import jakarta.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorAroundHandler::class)
class ErrorAroundHandlerInterceptor : MethodInterceptor<Any, Any> {

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {

        try {
            return context.proceed()
        } catch (ex: Exception) {

            val responseObserver = context.parameterValues[1] as StreamObserver<*>

            val status = when (ex) {
                is ConstraintViolationException -> Status.INVALID_ARGUMENT
                is IllegalArgumentException -> Status.INVALID_ARGUMENT
                is ChavePixExistenteException -> Status.ALREADY_EXISTS
                is IllegalStateException -> Status.FAILED_PRECONDITION
                is ChavePixNaoEncontradaException -> Status.NOT_FOUND
                is OperacaoNaoPermitidaException -> Status.PERMISSION_DENIED
                else -> Status.UNKNOWN
            }

            responseObserver.onError(
                status
                    .withCause(ex)
                    .withDescription(ex.message)
                    .asRuntimeException()
            )
        }

        return null
    }

}