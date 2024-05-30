package com.marmatsan.dev.core_domain.usecase

abstract class NonSuspendingUseCase<in I, out O> : UseCase {
    abstract operator fun invoke(input: I): O
}