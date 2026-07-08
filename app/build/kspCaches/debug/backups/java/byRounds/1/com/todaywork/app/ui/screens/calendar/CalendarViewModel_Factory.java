package com.todaywork.app.ui.screens.calendar;

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
public final class CalendarViewModel_Factory implements Factory<CalendarViewModel> {
  private final Provider<ShiftRepository> shiftRepoProvider;

  public CalendarViewModel_Factory(Provider<ShiftRepository> shiftRepoProvider) {
    this.shiftRepoProvider = shiftRepoProvider;
  }

  @Override
  public CalendarViewModel get() {
    return newInstance(shiftRepoProvider.get());
  }

  public static CalendarViewModel_Factory create(
      javax.inject.Provider<ShiftRepository> shiftRepoProvider) {
    return new CalendarViewModel_Factory(Providers.asDaggerProvider(shiftRepoProvider));
  }

  public static CalendarViewModel_Factory create(Provider<ShiftRepository> shiftRepoProvider) {
    return new CalendarViewModel_Factory(shiftRepoProvider);
  }

  public static CalendarViewModel newInstance(ShiftRepository shiftRepo) {
    return new CalendarViewModel(shiftRepo);
  }
}
