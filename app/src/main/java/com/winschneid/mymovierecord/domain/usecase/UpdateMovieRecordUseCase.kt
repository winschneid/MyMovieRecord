package com.winschneid.mymovierecord.domain.usecase

import com.winschneid.mymovierecord.domain.model.MovieRecord
import com.winschneid.mymovierecord.domain.repository.MovieRecordRepository
import javax.inject.Inject

class UpdateMovieRecordUseCase @Inject constructor(
    private val repository: MovieRecordRepository,
) {
    suspend operator fun invoke(record: MovieRecord) = repository.updateRecord(record)
}
