package com.example.coldpreventionguardianapp.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.coldpreventionguardianapp.R
import androidx.lifecycle.viewModelScope
import com.example.coldpreventionguardianapp.data.model.Comment
import com.example.coldpreventionguardianapp.data.model.HealthAssessment
import com.example.coldpreventionguardianapp.data.model.Medication
import com.example.coldpreventionguardianapp.data.model.TemperatureRecord
import com.example.coldpreventionguardianapp.data.repository.CommunityRepository
import com.example.coldpreventionguardianapp.data.repository.SessionManager
import com.example.coldpreventionguardianapp.data.repository.TemperatureRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val repository = TemperatureRepository()

    private val _temperatureRecords = MutableStateFlow<List<TemperatureRecord>>(emptyList())
    val temperatureRecords: StateFlow<List<TemperatureRecord>> = _temperatureRecords.asStateFlow()

    private val _healthAssessment = MutableStateFlow<HealthAssessment?>(null)
    val healthAssessment: StateFlow<HealthAssessment?> = _healthAssessment.asStateFlow()

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()

    private val communityRepository = CommunityRepository()
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    init {
        val uid = SessionManager.currentUser.value?.uid
        if (uid != null) {
            observeRecords(uid)
        }

        // Re-observe when user changes
        viewModelScope.launch {
            SessionManager.currentUser.collect { user ->
                if (user != null) {
                    observeRecords(user.uid)
                }
            }
        }

        // Compute assessment & medication whenever records change
        viewModelScope.launch {
            _temperatureRecords.collect { records ->
                val temp = records.firstOrNull()?.temperature
                _healthAssessment.value = temp?.let { computeHealthAssessment(it) }
                _medications.value = computeMedications(temp)
            }
        }

        // Observe community comments in real-time
        viewModelScope.launch {
            communityRepository.observeComments().collect { commentList ->
                _comments.value = commentList
            }
        }
    }

    private fun observeRecords(uid: String) {
        viewModelScope.launch {
            repository.observeTemperatureRecords(uid).collect { records ->
                _temperatureRecords.value = records
            }
        }
    }

    fun submitTemperature(record: TemperatureRecord) {
        val uid = SessionManager.currentUser.value?.uid ?: return
        viewModelScope.launch {
            repository.submitTemperature(uid, record)
        }
    }

    // ---- Community ----

    fun postComment(content: String) {
        val author = SessionManager.currentUser.value?.username ?: return
        viewModelScope.launch {
            communityRepository.postComment(author, content)
        }
    }

    fun toggleLike(commentId: String) {
        val currentUsername = SessionManager.currentUser.value?.username ?: return
        viewModelScope.launch {
            communityRepository.toggleLike(commentId, currentUsername)
        }
    }

    // --------------- Business Logic ---------------

    private fun computeHealthAssessment(temp: Double): HealthAssessment {
        return when {
            temp < 37.3 -> HealthAssessment(
                titleRes = R.string.health_title_normal,
                analysisRes = R.string.health_analysis_normal,
                causesRes = R.string.health_causes_normal,
                complicationsRes = R.string.health_complications_normal,
                adviceRes = R.string.health_advice_normal,
                themeColor = Color(0xFF4CAF50) // Green
            )
            temp < 38.0 -> HealthAssessment(
                titleRes = R.string.health_title_mild,
                analysisRes = R.string.health_analysis_mild,
                causesRes = R.string.health_causes_mild,
                complicationsRes = R.string.health_complications_mild,
                adviceRes = R.string.health_advice_mild,
                themeColor = Color(0xFFFF9800) // Orange
            )
            temp < 39.0 -> HealthAssessment(
                titleRes = R.string.health_title_fever,
                analysisRes = R.string.health_analysis_fever,
                causesRes = R.string.health_causes_fever,
                complicationsRes = R.string.health_complications_fever,
                adviceRes = R.string.health_advice_fever,
                themeColor = Color(0xFFFF5722) // Deep Orange / Red
            )
            else -> HealthAssessment(
                titleRes = R.string.health_title_emergency,
                analysisRes = R.string.health_analysis_emergency,
                causesRes = R.string.health_causes_emergency,
                complicationsRes = R.string.health_complications_emergency,
                adviceRes = R.string.health_advice_emergency,
                themeColor = Color(0xFFD32F2F) // Deep Red
            )
        }
    }

    private fun computeMedications(temp: Double?): List<Medication> {
        val medications = mutableListOf<Medication>()

        // Default preventive medications (always present)
        medications.add(
            Medication(
                nameRes = R.string.med_name_vitamin_d3,
                regionRes = R.string.med_region_global,
                descRes = R.string.med_desc_vitamin_d3,
                typeRes = R.string.med_type_preventive
            )
        )
        medications.add(
            Medication(
                nameRes = R.string.med_name_echinacea,
                regionRes = R.string.med_region_eu_us,
                descRes = R.string.med_desc_echinacea,
                typeRes = R.string.med_type_preventive
            )
        )

        // >= 37.3°C: Add treatment medications
        if (temp != null && temp >= 37.3) {
            medications.add(
                Medication(
                    nameRes = R.string.med_name_paracetamol,
                    regionRes = R.string.med_region_global,
                    descRes = R.string.med_desc_paracetamol,
                    typeRes = R.string.med_type_treatment,
                    warningRes = R.string.med_warning_paracetamol
                )
            )
            medications.add(
                Medication(
                    nameRes = R.string.med_name_guaifenesin,
                    regionRes = R.string.med_region_global,
                    descRes = R.string.med_desc_guaifenesin,
                    typeRes = R.string.med_type_treatment
                )
            )
        }

        // >= 38.0°C: Add stronger treatment medications
        if (temp != null && temp >= 38.0) {
            medications.add(
                Medication(
                    nameRes = R.string.med_name_ibuprofen,
                    regionRes = R.string.med_region_global,
                    descRes = R.string.med_desc_ibuprofen,
                    typeRes = R.string.med_type_treatment,
                    warningRes = R.string.med_warning_ibuprofen
                )
            )
            medications.add(
                Medication(
                    nameRes = R.string.med_name_lemsip,
                    regionRes = R.string.med_region_eu_au,
                    descRes = R.string.med_desc_lemsip,
                    typeRes = R.string.med_type_treatment,
                    warningRes = R.string.med_warning_lemsip
                )
            )
        }

        return medications
    }
}