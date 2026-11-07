package com.bh.personalacademy.data.local
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
@Entity(tableName="lesson_progress",primaryKeys=["profileId","lessonId"])
data class LessonProgressEntity(val profileId:String="primary",val lessonId:String,val completedAt:Long?=null,val bestScore:Int=0,val attempts:Int=0)
@Entity(tableName="quiz_attempts",indices=[Index("profileId"),Index("completedAt")])
data class QuizAttemptEntity(@PrimaryKey val attemptId:String,val profileId:String="primary",val quizId:String,val score:Int,val totalQuestions:Int,val completedAt:Long,val incorrectQuestionIds:String="")
@Entity(tableName="study_sessions",indices=[Index("profileId"),Index("startedAt")])
data class StudySessionEntity(@PrimaryKey val sessionId:String,val profileId:String="primary",val lessonId:String,val startedAt:Long,val durationMillis:Long,val completedAt:Long?=null)
@Entity(tableName="wallets")
data class WalletEntity(@PrimaryKey val profileId:String="primary",val xp:Long=0,val coins:Long=0,val level:Int=1)
@Entity(tableName="reward_events",indices=[Index(value=["profileId","source","sourceId"],unique=true)])
data class RewardEventEntity(@PrimaryKey val eventId:String,val profileId:String="primary",val source:String,val sourceId:String,val xpDelta:Int,val coinDelta:Int,val createdAt:Long)
@Entity(tableName="vocabulary",indices=[Index("profileId"),Index("english")])
data class VocabularyEntity(@PrimaryKey val wordId:String,val profileId:String="primary",val english:String,val dariMeaning:String,val pronunciation:String,val exampleEnglish:String,val exampleDari:String,val categoryId:String,val favourite:Boolean=false,val mastery:Int=0)
@Entity(tableName="daily_goals",primaryKeys=["profileId","dateIso"])
data class DailyGoalEntity(val profileId:String="primary",val dateIso:String,val targetMinutes:Int,val targetLessons:Int,val targetWords:Int,val targetQuizzes:Int)
@Entity(tableName="reminders",indices=[Index("profileId")])
data class ReminderEntity(@PrimaryKey val reminderId:String,val profileId:String="primary",val title:String,val hour:Int,val minute:Int,val repeatDaysMask:Int,val enabled:Boolean)