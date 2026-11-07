package com.bh.personalacademy.data.local
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(entities=[LessonProgressEntity::class,QuizAttemptEntity::class,StudySessionEntity::class,WalletEntity::class,RewardEventEntity::class,VocabularyEntity::class,DailyGoalEntity::class,ReminderEntity::class],version=1,exportSchema=false)
abstract class AcademyDatabase:RoomDatabase(){
 abstract fun academyDao():AcademyDao
 companion object{
  @Volatile private var instance:AcademyDatabase?=null
  fun getInstance(context:Context):AcademyDatabase=instance?: synchronized(this){
   instance?:Room.databaseBuilder(context.applicationContext,AcademyDatabase::class.java,"bh_personal_academy.db").build().also{instance=it}
  }
 }
}