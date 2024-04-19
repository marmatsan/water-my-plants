package com.marmatsan.dev.core_domain.usecase

interface SuspendingUseCase<in T, out R> : UseCase<T, R> {
    suspend operator fun invoke(input: T): R
}