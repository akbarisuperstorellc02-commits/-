package com.bh.personalacademy.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bh.personalacademy.data.AcademyRepository
import com.bh.personalacademy.data.local.GameAttemptEntity
import com.bh.personalacademy.data.local.LessonProgressEntity
import com.bh.personalacademy.data.local.QuizAttemptEntity
import com.bh.personalacademy.data.local.ReminderEntity
import com.bh.personalacademy.data.local.RewardEventEntity
import com.bh.personalacademy.data.local.StudySessionEntity
import com.bh.personalacademy.data.local.VocabularyEntity
import com.bh.personalacademy.data.local.WalletEntity
import com.bh.personalacademy.data.preferences.AcademyPreferences
import com.bh.personalacademy.data.local.AcademyDao
import com.example.academy.domain.model.UserProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AcademyViewModel(
    private val repository: AcademyRepository
) : ViewModel() {
    val profile: StateFlow<UserProfile> =
        repository.profile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserProfile())
    val wallet: StateFlow<WalletEntity?> =
        repository.wallet.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val lessonProgress: StateFlow<List<LessonProgressEntity>> =
        repository.lessonProgress.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val quizAttempts: StateFlow<List<QuizAttemptEntity>> =
        repository.quizAttempts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val gameAttempts: StateFlow<List<GameAttemptEntity>> =
        repository.gameAttempts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val vocabulary: StateFlow<List<VocabularyEntity>> =
        repository.vocabulary.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val reminders: StateFlow<List<ReminderEntity>> =
        repository.reminders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val studySessions: StateFlow<List<StudySessionEntity>> =
        repository.studySessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val totalStudyMillis: StateFlow<Long> =
        repository.totalStudyMillis.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    fun initialize() {
        viewModelScope.launch { repository.initializeProfileWallet() }
    }

    fun completeLesson(lessonId: String, score: Int = 100, profileId: String = "primary") {
        viewModelScope.launch { repository.completeLesson(lessonId, score, profileId) }
    }

    fun saveStudySession(lessonId: String, startedAt: Long, durationMillis: Long) {
        viewModelScope.launch { repository.saveStudySession(lessonId, startedAt, durationMillis) }
    }

    fun practiseWord(word: VocabularyEntity) {
        viewModelScope.launch { repository.practiseWord(word) }
    }

    fun rewardOnce(event: RewardEventEntity, onResult: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch { onResult?.invoke(repository.rewardOnce(event)) }
    }

    fun recordQuiz(attempt: QuizAttemptEntity) {
        viewModelScope.launch { repository.recordQuiz(attempt) }
    }

    fun recordGame(attempt: GameAttemptEntity) {
        viewModelScope.launch { repository.recordGame(attempt) }
    }

    fun saveReminder(reminder: ReminderEntity) {
        viewModelScope.launch { repository.saveReminder(reminder) }
    }

    fun deleteReminder(reminderId: String) {
        viewModelScope.launch { repository.deleteReminder(reminderId) }
    }
}

class AcademyViewModelFactory(
    private val repository: AcademyRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(AcademyViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
        return AcademyViewModel(repository) as T
    }
}
