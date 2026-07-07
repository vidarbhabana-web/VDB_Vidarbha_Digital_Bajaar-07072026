package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    private const val MODEL_NAME = "gemini-3.5-flash" // Recommended for basic text/chat tasks

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Calls Gemini to generate text based on a prompt and optional system instructions.
     */
    suspend fun generateContent(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is empty or placeholder!")
            return@withContext "Error: API Key is missing. Please configure it in the AI Studio Secrets panel."
        }

        try {
            // Construct request payload using native JSONObject
            val requestJson = JSONObject()
            
            // Contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            // System Instruction (if provided)
            if (!systemInstruction.isNullOrBlank()) {
                val sysInstrObj = JSONObject()
                val sysPartsArray = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArray.put(sysPartObj)
                sysInstrObj.put("parts", sysPartsArray)
                requestJson.put("systemInstruction", sysInstrObj)
            }

            // Generation config
            val configObj = JSONObject()
            configObj.put("temperature", 0.7)
            requestJson.put("generationConfig", configObj)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)

            val url = "$BASE_URL$MODEL_NAME:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(body)
                .header("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                Log.e(TAG, "API error code: ${response.code}, body: $responseBody")
                return@withContext "Error: Failed to fetch response from AI advisor. Status code: ${response.code}"
            }

            // Parse response
            val root = JSONObject(responseBody)
            val candidates = root.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                if (content != null) {
                    val parts = content.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "No text part found")
                    }
                }
            }
            "No output received from the AI advisor."
        } catch (e: Exception) {
            Log.e(TAG, "Exception during API call", e)
            "Error: Unable to connect to AI Advisor. Details: ${e.localizedMessage}"
        }
    }
}
