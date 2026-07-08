package com.todaywork.app.di;

import com.todaywork.app.data.db.AppDatabase;
import com.todaywork.app.data.db.dao.AlarmSettingDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class AppModule_ProvideAlarmSettingDaoFactory implements Factory<AlarmSettingDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideAlarmSettingDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AlarmSettingDao get() {
    return provideAlarmSettingDao(dbProvider.get());
  }

  public static AppModule_ProvideAlarmSettingDaoFactory create(
      javax.inject.Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideAlarmSettingDaoFactory(Providers.asDaggerProvider(dbProvider));
  }

  public static AppModule_ProvideAlarmSettingDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideAlarmSettingDaoFactory(dbProvider);
  }

  public static AlarmSettingDao provideAlarmSettingDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideAlarmSettingDao(db));
  }
}
