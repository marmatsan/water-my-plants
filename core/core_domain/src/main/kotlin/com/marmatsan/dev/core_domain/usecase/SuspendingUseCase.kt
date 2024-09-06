package com.marmatsan.dev.core_domain.usecase

abstract class SuspendingUseCase<in I, out O> : UseCase {
    abstract suspend operator fun invoke(input: I): O
}