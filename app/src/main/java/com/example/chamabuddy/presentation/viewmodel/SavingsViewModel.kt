package com.example.chamabuddy.presentation.viewmodel

import android.R.interpolator.cycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chamabuddy.domain.model.Cycle
import com.example.chamabuddy.domain.model.Member
import com.example.chamabuddy.domain.model.MonthlySaving
import com.example.chamabuddy.domain.model.MonthlySavingEntry
import com.example.chamabuddy.domain.repository.CycleRepository
import com.example.chamabuddy.domain.repository.MemberRepository
import com.example.chamabuddy.domain.repository.SavingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SavingsViewModel @Inject constructor(
    private val savingsRepository: SavingsRepository,
    private val memberRepository: MemberRepository,
    private val cycleRepository: CycleRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<SavingsState>(SavingsState.Idle)
    val state: StateFlow<SavingsState> = _state.asStateFlow()
    private val _memberSavings = MutableStateFlow<List<MonthlySavingEntry>>(emptyList())
    val memberSavings: StateFlow<List<MonthlySavingEntry>> = _memberSavings.asStateFlow()
    private val _cycleSavings = MutableStateFlow<List<MonthlySaving>>(emptyList())
    val cycleSavings: StateFlow<List<MonthlySaving>> = _cycleSavings.asStateFlow()
    private val _members = MutableStateFlow<Map<String, Member>>(emptyMap())
    val members: StateFlow<Map<String, Member>> = _members.asStateFlow()
    private val _memberTotals = MutableStateFlow<Map<String, Int>>(emptyMap())
    val memberTotals: StateFlow<Map<String, Int>> = _memberTotals.asStateFlow()
    private val _activeCycle = MutableStateFlow<Cycle?>(null)
    val activeCycle: StateFlow<Cycle?> = _activeCycle.asStateFlow()


    private val _totalGroupSavings = MutableStateFlow(0)
    val totalGroupSavings: StateFlow<Int> = _totalGroupSavings.asStateFlow()




    // Rename the properties to avoid setter conflicts
    private var _groupId: String by mutableStateOf("")
    private var _cycleId: String by mutableStateOf("")


    private val _allMemberCycles = MutableStateFlow<List<CycleWithSavings>>(emptyList())
    val allMemberCycles: StateFlow<List<CycleWithSavings>> = _allMemberCycles.asStateFlow()


    fun initializeGroupId(id: String) {
        _groupId = id
        loadActiveCycle()
        loadActiveMembers()
    }

    fun initializeCycleId(id: String) {
        _cycleId = id
        loadActiveMembers()
        loadAllMemberSavingsTotalsByCycle()
    }

    init {
        loadActiveMembers()
        loadAllMemberSavingsTotalsByCycle()
    }

    private fun loadActiveCycle() {
        viewModelScope.launch {
            // Get active cycle using groupId
            _activeCycle.value = cycleRepository.getActiveCycleForGroup(_groupId)
            activeCycle.value?.cycleId?.let { initializeCycleId(it) }
        }
    }

    private fun loadActiveMembers() {
        viewModelScope.launch {
            try {
                // Only load members for current group
                memberRepository.getMembersByGroupFlow(_groupId).collect { members ->
                    _members.value = members.associateBy { it.memberId }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    private fun loadAllMemberSavingsTotalsByCycle() {
        viewModelScope.launch {
            try {
                val activeMembers = memberRepository.getMembersByGroup(_groupId)
                val totals = mutableMapOf<String, Int>()

                activeMembers.forEach { member ->
                    // Get savings specific to group and cycle
                    val total = savingsRepository.getMemberSavingsTotalByGroupAndCycle(
                        groupId = _groupId,
                        cycleId = _cycleId,
                        memberId = member.memberId
                    )
                    totals[member.memberId] = total
                }
                _memberTotals.value = totals
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    fun handleEvent(event: SavingsEvent) {
        when (event) {
            is SavingsEvent.EnsureMonthExists -> ensureMonthExists(event)
            is SavingsEvent.RecordSavings -> recordSavings(event)
            is SavingsEvent.GetMemberSavings -> getMemberSavings(event)
            is SavingsEvent.GetCycleSavings -> getCycleSavings(event)
            is SavingsEvent.GetSavingsProgress -> getSavingsProgress(event)
            is SavingsEvent.GetMemberSavingsTotalByCycle -> getMemberSavingsTotalByCycle(event)
            is SavingsEvent.GetMemberSavingsTotal -> getMemberSavingsTotal(event)
            is SavingsEvent.GetAllMemberCycles -> getAllMemberCycles(event.memberId)

            SavingsEvent.ResetSavingsState -> resetState()
        }
    }





    private fun getAllMemberCycles(memberId: String) {
        viewModelScope.launch {
            _state.value = SavingsState.Loading
            try {
                // 1. Get all cycles where member has savings
                var cyclesWithSavings = savingsRepository.getCycleWithSavingsForMember(memberId)

                // 2. Get active cycle if exists
                val activeCycle = _activeCycle.value

                // 3. If member is in active cycle but not in the list, add it
                if (activeCycle != null) {
                    val hasActiveCycle = cyclesWithSavings.any { it.cycle.cycleId == activeCycle.cycleId }
                    if (!hasActiveCycle) {
                        val activeCycleWithSavings = CycleWithSavings(
                            cycle = activeCycle,
                            savingsEntries = emptyList()
                        )
                        cyclesWithSavings += activeCycleWithSavings
                    }
                }

                // 4. Sort by start date (newest first)
                val sorted = cyclesWithSavings.sortedByDescending { it.cycle.startDate }
                _allMemberCycles.value = sorted

                _state.value = SavingsState.Idle
            } catch (e: Exception) {
                _state.value = SavingsState.Error(e.message ?: "Failed to load cycles")
            }
        }
    }








    private fun recordSavings(event: SavingsEvent.RecordSavings) {
        viewModelScope.launch {
            _state.value = SavingsState.Loading
            try {
                // Validate against active cycle
                val activeCycle = cycleRepository.getActiveCycleForGroup(event.groupId)
                    ?: throw IllegalStateException("No active cycle in group")

                if (activeCycle.cycleId != event.cycleId) {
                    throw IllegalStateException("Must record in current cycle")
                }

                val result = savingsRepository.recordMonthlySavings(
                    cycleId = event.cycleId,
                    monthYear = event.monthYear,
                    memberId = event.memberId,
                    amount = event.amount,
                    recordedBy = event.recordedBy,
                    groupId = event.groupId
                )
                val cycle = cycleRepository.getCycleById(event.cycleId)

                if (cycle == null) {
                    _state.value = SavingsState.Error("No active cycle found")
                    return@launch
                }
                if (result.isSuccess) {
                    // Refresh ALL cycles after saving
                    getAllMemberCycles(event.memberId)
                    _state.value = SavingsState.SavingsRecorded(true)
                } else {

                    _state.value = SavingsState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to record savings"
                    )
                }
            } catch (e: Exception) {
                _state.value = SavingsState.Error(e.message ?: "Failed to record savings")
            }
        }
    }

    private fun getMemberSavings(event: SavingsEvent.GetMemberSavings) {
        viewModelScope.launch {
            _state.value = SavingsState.Loading
            try {
                savingsRepository.getMemberSavings(event.cycleId, event.memberId)
                    .collect { entries ->
                        _memberSavings.value = entries
                        _state.value = SavingsState.SavingsEntriesLoaded(entries)
                    }
            } catch (e: Exception) {
                _state.value = SavingsState.Error(e.message ?: "Failed to load savings")
            }
        }
    }

    private fun getMemberSavingsTotal(event: SavingsEvent.GetMemberSavingsTotal) {
        viewModelScope.launch {
            _state.value = SavingsState.Loading
            try {
                val total = savingsRepository.getMemberSavingsTotal(event.memberId)
                _state.value = SavingsState.MemberSavingsTotal(total)
            } catch (e: Exception) {
                _state.value = SavingsState.Error(e.message ?: "Failed to load savings total")
            }
        }
    }

    private fun getCycleSavings(event: SavingsEvent.GetCycleSavings) {
        viewModelScope.launch {
            _state.value = SavingsState.Loading
            try {
                savingsRepository.getCycleSavings(event.cycleId).collect { savings ->
                    _cycleSavings.value = savings
                    _state.value = SavingsState.SavingsLoaded(savings)
                }
            } catch (e: Exception) {
                _state.value = SavingsState.Error(e.message ?: "Failed to load cycle savings")
            }
        }
    }

    private fun getSavingsProgress(event: SavingsEvent.GetSavingsProgress) {
        viewModelScope.launch {
            _state.value = SavingsState.Loading
            try {
                // Get domain model
                val domainProgress = savingsRepository.getMonthlySavingsProgress(
                    event.cycleId,
                    event.monthYear
                )

                // Map to presentation model
                val presentationProgress = SavingsState.SavingsProgress(
                    targetAmount = domainProgress.targetAmount,
                    currentAmount = domainProgress.currentAmount,
                    membersCompleted = domainProgress.membersCompleted,
                    totalMembers = domainProgress.totalMembers
                )

                _state.value = presentationProgress
            } catch (e: Exception) {
                _state.value = SavingsState.Error(e.message ?: "Failed to load savings progress")
            }
        }
    }

    private fun getMemberSavingsTotalByCycle(event: SavingsEvent.GetMemberSavingsTotalByCycle) {
        viewModelScope.launch {
            _state.value = SavingsState.Loading
            try {
                val total = savingsRepository.getMemberSavingsTotalByCycle(
                    event.cycleId,
                    event.memberId
                )
                _state.value = SavingsState.MemberSavingsTotal(total)
            } catch (e: Exception) {
                _state.value = SavingsState.Error(e.message ?: "Failed to load savings total")
            }
        }
    }

    private fun ensureMonthExists(event: SavingsEvent.EnsureMonthExists) {
        viewModelScope.launch {
            try {
                savingsRepository.ensureMonthExists(
                    event.cycleId,
                    event.monthYear,
                    event.targetAmount,
                    event.groupId
                )
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun resetState() {
        _state.value = SavingsState.Idle
    }
}

sealed class SavingsState {
    object Idle : SavingsState()
    object Loading : SavingsState()
    data class SavingsLoaded(val savings: List<MonthlySaving>) : SavingsState()
    data class SavingsEntriesLoaded(val entries: List<MonthlySavingEntry>) : SavingsState()
    data class SavingsProgress(
        val targetAmount: Int,
        val currentAmount: Int,
        val membersCompleted: Int,
        val totalMembers: Int
    ) : SavingsState()
    data class SavingsRecorded(val success: Boolean) : SavingsState()
    data class MemberSavingsTotal(val total: Int) : SavingsState()
    data class Error(val message: String) : SavingsState()
}

sealed class SavingsEvent {
    data class RecordSavings(
        val monthYear: String,
        val memberId: String,
        val amount: Int,
        val recordedBy: String,
        val cycleId: String,
        val groupId: String
    ) : SavingsEvent()
    data class GetAllMemberCycles(val memberId: String) : SavingsEvent()
    data class GetMemberSavingsTotal(val memberId: String) : SavingsEvent()
    data class GetMemberSavings(val cycleId: String, val memberId: String) : SavingsEvent()
    data class GetCycleSavings(val cycleId: String) : SavingsEvent()
    data class GetSavingsProgress(val cycleId: String, val monthYear: String) : SavingsEvent()
    data class GetMemberSavingsTotalByCycle(val cycleId: String, val memberId: String) : SavingsEvent()
    data class EnsureMonthExists(val cycleId: String, val monthYear: String, val targetAmount: Int, val groupId: String) : SavingsEvent()
    object ResetSavingsState : SavingsEvent()
}

data class CycleWithSavings(
    val cycle: Cycle,
    val savingsEntries: List<MonthlySavingEntry>
)