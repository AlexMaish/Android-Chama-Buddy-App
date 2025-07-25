package com.example.chamabuddy.domain.repository

import com.example.chamabuddy.domain.model.BenefitEntity
import kotlinx.coroutines.flow.Flow

interface BenefitRepository {
    suspend fun addBenefit(benefit: BenefitEntity)
    fun getBenefits(groupId: String): Flow<List<BenefitEntity>>
    fun getTotal(groupId: String): Flow<Double>
}