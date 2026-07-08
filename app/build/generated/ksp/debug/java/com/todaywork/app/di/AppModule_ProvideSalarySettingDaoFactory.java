package com.todaywork.app.di;

import com.todaywork.app.data.db.AppDatabase;
import com.todaywork.app.data.db.dao.SalarySettingDao;
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
public final class AppModule_ProvideSalarySettingDaoFactory implements Factory<SalarySettingDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideSalarySettingDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SalarySettingDao get() {
    return provideSalarySettingDao(dbProvider.get());
  }

  public static AppModule_ProvideSalarySettingDaoFactory create(
      javax.inject.Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideSalarySettingDaoFactory(Providers.asDaggerProvider(dbProvider));
  }

  public static AppModule_ProvideSalarySettingDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideSalarySettingDaoFactory(dbProvider);
  }

  public static SalarySettingDao provideSalarySettingDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSalarySettingDao(db));
  }
}
