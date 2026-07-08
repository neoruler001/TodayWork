package com.todaywork.app.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.todaywork.app.data.db.AppDatabase
import com.todaywork.app.data.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "todaywork.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideShiftPatternDao(db: AppDatabase): ShiftPatternDao = db.shiftPatternDao()

    @Provides
    fun provideWorkRecordDao(db: AppDatabase): WorkRecordDao = db.workRecordDao()

    @Provides
    fun provideSalarySettingDao(db: AppDatabase): SalarySettingDao = db.salarySettingDao()

    @Provides
    fun provideAlarmSettingDao(db: AppDatabase): AlarmSettingDao = db.alarmSettingDao()

    @Provides
    fun provideMemoDao(db: AppDatabase): MemoDao = db.memoDao()

    @Singleton
    @Provides
    fun provideGson(): Gson = Gson()
}
