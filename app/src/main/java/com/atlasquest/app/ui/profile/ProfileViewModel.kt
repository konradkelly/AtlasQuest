package com.atlasquest.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atlasquest.app.data.model.Profile
import com.atlasquest.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Shared by HomeScreen and ResultsScreen to surface the current XP/streak. */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    profileRepository: ProfileRepository
) : ViewModel() {
    val profile: StateFlow<Profile> = profileRepository.profile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = Profile(),
    )
}
