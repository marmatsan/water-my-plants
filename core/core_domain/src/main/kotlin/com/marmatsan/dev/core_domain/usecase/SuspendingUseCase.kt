package com.marmatsan.dev.core_domain.usecase

import com.marmatsan.dev.core_domain.result.Result
import com.marmatsan.dev.core_domain.result.RootError

abstract class SuspendingUseCase<I, T, R : RootError> : UseCase {
    abstract suspend operator fun invoke(input: I): Result<T, R>
}