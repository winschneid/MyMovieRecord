package com.winschneid.mymovierecord.domain.usecase

import com.winschneid.mymovierecord.domain.model.MovieRecord
import com.winschneid.mymovierecord.domain.repository.MovieRecordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMovieRecordsUseCase @Inject constructor(
    private val repository: MovieRecordRepository
) {
    operator fun invoke(): Flow<List<MovieRecord>> = repository.observeAllRecords()
}
