package com.todaywork.app.di;

import com.todaywork.app.data.db.AppDatabase;
import com.todaywork.app.data.db.dao.WorkRecordDao;
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
public final class AppModule_ProvideWorkRecordDaoFactory implements Factory<WorkRecordDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideWorkRecordDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public WorkRecordDao get() {
    return provideWorkRecordDao(dbProvider.get());
  }

  public static AppModule_ProvideWorkRecordDaoFactory create(
      javax.inject.Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideWorkRecordDaoFactory(Providers.asDaggerProvider(dbProvider));
  }

  public static AppModule_ProvideWorkRecordDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideWorkRecordDaoFactory(dbProvider);
  }

  public static WorkRecordDao provideWorkRecordDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideWorkRecordDao(db));
  }
}
