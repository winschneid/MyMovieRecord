package com.winschneid.mymovierecord.domain.usecase

import com.winschneid.mymovierecord.domain.repository.MovieRecordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTheaterNamesUseCase @Inject constructor(
    private val repository: MovieRecordRepository,
) {
    operator fun invoke(): Flow<List<String>> = repository.observeDistinctTheaterNames()
}
