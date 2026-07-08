package com.todaywork.app.data.repository;

import com.todaywork.app.data.db.dao.SalarySettingDao;
import com.todaywork.app.data.db.dao.WorkRecordDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class SalaryRepository_Factory implements Factory<SalaryRepository> {
  private final Provider<SalarySettingDao> salarySettingDaoProvider;

  private final Provider<WorkRecordDao> workRecordDaoProvider;

  public SalaryRepository_Factory(Provider<SalarySettingDao> salarySettingDaoProvider,
      Provider<WorkRecordDao> workRecordDaoProvider) {
    this.salarySettingDaoProvider = salarySettingDaoProvider;
    this.workRecordDaoProvider = workRecordDaoProvider;
  }

  @Override
  public SalaryRepository get() {
    return newInstance(salarySettingDaoProvider.get(), workRecordDaoProvider.get());
  }

  public static SalaryRepository_Factory create(
      javax.inject.Provider<SalarySettingDao> salarySettingDaoProvider,
      javax.inject.Provider<WorkRecordDao> workRecordDaoProvider) {
    return new SalaryRepository_Factory(Providers.asDaggerProvider(salarySettingDaoProvider), Providers.asDaggerProvider(workRecordDaoProvider));
  }

  public static SalaryRepository_Factory create(Provider<SalarySettingDao> salarySettingDaoProvider,
      Provider<WorkRecordDao> workRecordDaoProvider) {
    return new SalaryRepository_Factory(salarySettingDaoProvider, workRecordDaoProvider);
  }

  public static SalaryRepository newInstance(SalarySettingDao salarySettingDao,
      WorkRecordDao workRecordDao) {
    return new SalaryRepository(salarySettingDao, workRecordDao);
  }
}
