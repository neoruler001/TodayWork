package com.todaywork.app.ui.screens.salary;

import com.todaywork.app.data.repository.SalaryRepository;
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
public final class SalaryViewModel_Factory implements Factory<SalaryViewModel> {
  private final Provider<SalaryRepository> salaryRepoProvider;

  public SalaryViewModel_Factory(Provider<SalaryRepository> salaryRepoProvider) {
    this.salaryRepoProvider = salaryRepoProvider;
  }

  @Override
  public SalaryViewModel get() {
    return newInstance(salaryRepoProvider.get());
  }

  public static SalaryViewModel_Factory create(
      javax.inject.Provider<SalaryRepository> salaryRepoProvider) {
    return new SalaryViewModel_Factory(Providers.asDaggerProvider(salaryRepoProvider));
  }

  public static SalaryViewModel_Factory create(Provider<SalaryRepository> salaryRepoProvider) {
    return new SalaryViewModel_Factory(salaryRepoProvider);
  }

  public static SalaryViewModel newInstance(SalaryRepository salaryRepo) {
    return new SalaryViewModel(salaryRepo);
  }
}
