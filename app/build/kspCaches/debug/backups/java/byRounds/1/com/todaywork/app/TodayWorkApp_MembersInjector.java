package com.todaywork.app;

import androidx.hilt.work.HiltWorkerFactory;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class TodayWorkApp_MembersInjector implements MembersInjector<TodayWorkApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public TodayWorkApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<TodayWorkApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new TodayWorkApp_MembersInjector(workerFactoryProvider);
  }

  public static MembersInjector<TodayWorkApp> create(
      javax.inject.Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new TodayWorkApp_MembersInjector(Providers.asDaggerProvider(workerFactoryProvider));
  }

  @Override
  public void injectMembers(TodayWorkApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.todaywork.app.TodayWorkApp.workerFactory")
  public static void injectWorkerFactory(TodayWorkApp instance, HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
