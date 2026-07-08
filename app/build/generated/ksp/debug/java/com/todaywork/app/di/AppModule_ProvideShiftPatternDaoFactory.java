package com.todaywork.app.di;

import com.todaywork.app.data.db.AppDatabase;
import com.todaywork.app.data.db.dao.ShiftPatternDao;
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
public final class AppModule_ProvideShiftPatternDaoFactory implements Factory<ShiftPatternDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideShiftPatternDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ShiftPatternDao get() {
    return provideShiftPatternDao(dbProvider.get());
  }

  public static AppModule_ProvideShiftPatternDaoFactory create(
      javax.inject.Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideShiftPatternDaoFactory(Providers.asDaggerProvider(dbProvider));
  }

  public static AppModule_ProvideShiftPatternDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideShiftPatternDaoFactory(dbProvider);
  }

  public static ShiftPatternDao provideShiftPatternDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideShiftPatternDao(db));
  }
}
