package com.marmatsan.dev.core_domain.usecase

import com.marmatsan.dev.core_domain.result.Result
import com.marmatsan.dev.core_domain.result.RootError

abstract class NonSuspendingUseCase<I, T, R : RootError> : UseCase {
    abstract operator fun invoke(input: I): Result<T, R>
}