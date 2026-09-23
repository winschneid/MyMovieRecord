package com.winschneid.mymovierecord.domain.usecase

import com.winschneid.mymovierecord.domain.repository.MovieRecordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTitlesUseCase @Inject constructor(
    private val repository: MovieRecordRepository,
) {
    operator fun invoke(): Flow<List<String>> = repository.observeDistinctTitles()
}
