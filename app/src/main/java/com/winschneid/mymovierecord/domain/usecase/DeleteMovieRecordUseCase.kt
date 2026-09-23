package com.winschneid.mymovierecord.domain.usecase

import com.winschneid.mymovierecord.domain.repository.MovieRecordRepository
import javax.inject.Inject

class DeleteMovieRecordUseCase @Inject constructor(
    private val repository: MovieRecordRepository
) {
    suspend operator fun invoke(id: Long) = repository.deleteRecord(id)
}
