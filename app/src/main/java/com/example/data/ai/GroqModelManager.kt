package com.example.data.ai

import android.util.Log

object GroqModelManager {
    private const val TAG = "GroqModelManager"

    // Primary preference is llama-3.3-70b-versatile as requested.
    // If not accessible on the account/tier (HTTP 404 / model_not_found),
    // fallback automatically to available models like llama-3.1-8b-instant.
    private val CANDIDATE_MODELS = listOf(
        "llama-3.3-70b-versatile",
        "llama-3.1-8b-instant",
        "llama3-70b-8192",
        "openai/gpt-oss-120b"
    )

    @Volatile
    private var verifiedWorkingModel: String? = null

    fun getCandidateModels(): List<String> {
        val current = verifiedWorkingModel
        return if (current != null) {
            listOf(current) + CANDIDATE_MODELS.filter { it != current }
        } else {
            CANDIDATE_MODELS
        }
    }

    fun markModelWorking(model: String) {
        verifiedWorkingModel = model
        Log.i(TAG, "Groq model verified working: $model")
    }

    fun getActiveDisplayName(): String {
        return when (verifiedWorkingModel) {
            "llama-3.1-8b-instant" -> "Llama 3.1 8B (Groq)"
            "llama3-70b-8192" -> "Llama 3 70B (Groq)"
            "openai/gpt-oss-120b" -> "GPT-OSS 120B (Groq)"
            else -> "Llama 3.3 70B (Groq)"
        }
    }
}
