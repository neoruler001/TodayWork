package com.todaywork.app.ui.screens.schedule;

import com.google.gson.Gson;
import com.todaywork.app.data.repository.ShiftRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class ScheduleViewModel_Factory implements Factory<ScheduleViewModel> {
  private final Provider<ShiftRepository> shiftRepoProvider;

  private final Provider<Gson> gsonProvider;

  public ScheduleViewModel_Factory(Provider<ShiftRepository> shiftRepoProvider,
      Provider<Gson> gsonProvider) {
    this.shiftRepoProvider = shiftRepoProvider;
    this.gsonProvider = gsonProvider;
  }

  @Override
  public ScheduleViewModel get() {
    return newInstance(shiftRepoProvider.get(), gsonProvider.get());
  }

  public static ScheduleViewModel_Factory create(
      javax.inject.Provider<ShiftRepository> shiftRepoProvider,
      javax.inject.Provider<Gson> gsonProvider) {
    return new ScheduleViewModel_Factory(Providers.asDaggerProvider(shiftRepoProvider), Providers.asDaggerProvider(gsonProvider));
  }

  public static ScheduleViewModel_Factory create(Provider<ShiftRepository> shiftRepoProvider,
      Provider<Gson> gsonProvider) {
    return new ScheduleViewModel_Factory(shiftRepoProvider, gsonProvider);
  }

  public static ScheduleViewModel newInstance(ShiftRepository shiftRepo, Gson gson) {
    return new ScheduleViewModel(shiftRepo, gson);
  }
}
