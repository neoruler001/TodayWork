package com.todaywork.app.data.repository;

import com.google.gson.Gson;
import com.todaywork.app.data.db.dao.ShiftPatternDao;
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
public final class ShiftRepository_Factory implements Factory<ShiftRepository> {
  private final Provider<ShiftPatternDao> shiftPatternDaoProvider;

  private final Provider<WorkRecordDao> workRecordDaoProvider;

  private final Provider<Gson> gsonProvider;

  public ShiftRepository_Factory(Provider<ShiftPatternDao> shiftPatternDaoProvider,
      Provider<WorkRecordDao> workRecordDaoProvider, Provider<Gson> gsonProvider) {
    this.shiftPatternDaoProvider = shiftPatternDaoProvider;
    this.workRecordDaoProvider = workRecordDaoProvider;
    this.gsonProvider = gsonProvider;
  }

  @Override
  public ShiftRepository get() {
    return newInstance(shiftPatternDaoProvider.get(), workRecordDaoProvider.get(), gsonProvider.get());
  }

  public static ShiftRepository_Factory create(
      javax.inject.Provider<ShiftPatternDao> shiftPatternDaoProvider,
      javax.inject.Provider<WorkRecordDao> workRecordDaoProvider,
      javax.inject.Provider<Gson> gsonProvider) {
    return new ShiftRepository_Factory(Providers.asDaggerProvider(shiftPatternDaoProvider), Providers.asDaggerProvider(workRecordDaoProvider), Providers.asDaggerProvider(gsonProvider));
  }

  public static ShiftRepository_Factory create(Provider<ShiftPatternDao> shiftPatternDaoProvider,
      Provider<WorkRecordDao> workRecordDaoProvider, Provider<Gson> gsonProvider) {
    return new ShiftRepository_Factory(shiftPatternDaoProvider, workRecordDaoProvider, gsonProvider);
  }

  public static ShiftRepository newInstance(ShiftPatternDao shiftPatternDao,
      WorkRecordDao workRecordDao, Gson gson) {
    return new ShiftRepository(shiftPatternDao, workRecordDao, gson);
  }
}
