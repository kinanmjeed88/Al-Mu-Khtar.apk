package com.mukhtari.app.domain.repository

interface SecurityRepository {
    suspend fun setPin(pin: String)
    suspend fun verifyPin(pin: String): Boolean
    suspend fun isPinSet(): Boolean
    
    suspend fun setSecurityQuestion(question: String, answer: String)
    suspend fun verifySecurityAnswer(answer: String): Boolean
    
    suspend fun enableBiometric(enabled: Boolean)
    suspend fun isBiometricEnabled(): Boolean
    
    suspend fun setAutoLockDuration(minutes: Int)
    suspend fun getAutoLockDuration(): Int
}
