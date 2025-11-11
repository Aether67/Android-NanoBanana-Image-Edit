package com.yunho.nanobanana.domain.usecase

import com.yunho.nanobanana.domain.model.AIGenerationResult
import com.yunho.nanobanana.domain.model.ImageGenerationRequest
import com.yunho.nanobanana.domain.repository.AIRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for generating AI content
 * Encapsulates business logic for image/text generation
 */
class GenerateAIContentUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    /**
     * Execute content generation
     * 
     * @param request The generation request with all parameters
     * @return Flow of generation results for reactive updates
     */
    operator fun invoke(request: ImageGenerationRequest): Flow<AIGenerationResult> {
        return aiRepository.generateContent(request)
    }
}
