package com.marmatsan.dev.core_domain.usecase

interface NonSuspendingUseCase<in T, out R> : UseCase<T, R> {
    operator fun invoke(input: T): R
}