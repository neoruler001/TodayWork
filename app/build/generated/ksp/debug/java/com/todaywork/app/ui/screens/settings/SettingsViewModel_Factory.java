package com.todaywork.app.ui.screens.settings;

import android.content.Context;
import com.todaywork.app.data.datastore.AppPreferences;
import com.todaywork.app.data.db.dao.AlarmSettingDao;
import com.todaywork.app.data.repository.ShiftRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<AppPreferences> prefsProvider;

  private final Provider<AlarmSettingDao> alarmSettingDaoProvider;

  private final Provider<ShiftRepository> shiftRepoProvider;

  public SettingsViewModel_Factory(Provider<Context> contextProvider,
      Provider<AppPreferences> prefsProvider, Provider<AlarmSettingDao> alarmSettingDaoProvider,
      Provider<ShiftRepository> shiftRepoProvider) {
    this.contextProvider = contextProvider;
    this.prefsProvider = prefsProvider;
    this.alarmSettingDaoProvider = alarmSettingDaoProvider;
    this.shiftRepoProvider = shiftRepoProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(contextProvider.get(), prefsProvider.get(), alarmSettingDaoProvider.get(), shiftRepoProvider.get());
  }

  public static SettingsViewModel_Factory create(javax.inject.Provider<Context> contextProvider,
      javax.inject.Provider<AppPreferences> prefsProvider,
      javax.inject.Provider<AlarmSettingDao> alarmSettingDaoProvider,
      javax.inject.Provider<ShiftRepository> shiftRepoProvider) {
    return new SettingsViewModel_Factory(Providers.asDaggerProvider(contextProvider), Providers.asDaggerProvider(prefsProvider), Providers.asDaggerProvider(alarmSettingDaoProvider), Providers.asDaggerProvider(shiftRepoProvider));
  }

  public static SettingsViewModel_Factory create(Provider<Context> contextProvider,
      Provider<AppPreferences> prefsProvider, Provider<AlarmSettingDao> alarmSettingDaoProvider,
      Provider<ShiftRepository> shiftRepoProvider) {
    return new SettingsViewModel_Factory(contextProvider, prefsProvider, alarmSettingDaoProvider, shiftRepoProvider);
  }

  public static SettingsViewModel newInstance(Context context, AppPreferences prefs,
      AlarmSettingDao alarmSettingDao, ShiftRepository shiftRepo) {
    return new SettingsViewModel(context, prefs, alarmSettingDao, shiftRepo);
  }
}
