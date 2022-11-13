package ru.ytken.a464_project_watermarks.domain.usecase

import ru.ytken.a464_project_watermarks.domain.models.Image
import ru.ytken.a464_project_watermarks.domain.models.Status
import ru.ytken.a464_project_watermarks.domain.repository.MainRepository

class GetImageUseCase(
    private val repository: MainRepository
) {
    operator fun invoke(): Image {
        return repository.getImage()
    }

    companion object {
        private val STATUS = Status.INITIAL
    }
}