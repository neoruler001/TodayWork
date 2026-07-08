package com.todaywork.app.data.db;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.todaywork.app.data.db.dao.AlarmSettingDao;
import com.todaywork.app.data.db.dao.AlarmSettingDao_Impl;
import com.todaywork.app.data.db.dao.SalarySettingDao;
import com.todaywork.app.data.db.dao.SalarySettingDao_Impl;
import com.todaywork.app.data.db.dao.ShiftPatternDao;
import com.todaywork.app.data.db.dao.ShiftPatternDao_Impl;
import com.todaywork.app.data.db.dao.WorkRecordDao;
import com.todaywork.app.data.db.dao.WorkRecordDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ShiftPatternDao _shiftPatternDao;

  private volatile WorkRecordDao _workRecordDao;

  private volatile SalarySettingDao _salarySettingDao;

  private volatile AlarmSettingDao _alarmSettingDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `shift_patterns` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `cyclesJson` TEXT NOT NULL, `startDateEpoch` INTEGER NOT NULL, `cycleOffsetDay` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `work_records` (`dateEpoch` INTEGER NOT NULL, `shiftTypeName` TEXT NOT NULL, `startTimeMinutes` INTEGER NOT NULL, `endTimeMinutes` INTEGER NOT NULL, `memo` TEXT NOT NULL, `isManual` INTEGER NOT NULL, `patternId` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`dateEpoch`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `salary_settings` (`id` INTEGER NOT NULL, `hourlyWage` INTEGER NOT NULL, `overtimeRate` REAL NOT NULL, `nightShiftRate` REAL NOT NULL, `weekendRate` REAL NOT NULL, `holidayRate` REAL NOT NULL, `mealAllowancePerDay` INTEGER NOT NULL, `regularHoursPerDay` INTEGER NOT NULL, `regularHoursPerWeek` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `alarm_settings` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `label` TEXT NOT NULL, `minutesBefore` INTEGER NOT NULL, `isEnabled` INTEGER NOT NULL, `shiftTypeFilter` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'c3a40ca4ed4c88759c0dbfb29ac5459c')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `shift_patterns`");
        db.execSQL("DROP TABLE IF EXISTS `work_records`");
        db.execSQL("DROP TABLE IF EXISTS `salary_settings`");
        db.execSQL("DROP TABLE IF EXISTS `alarm_settings`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsShiftPatterns = new HashMap<String, TableInfo.Column>(7);
        _columnsShiftPatterns.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShiftPatterns.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShiftPatterns.put("cyclesJson", new TableInfo.Column("cyclesJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShiftPatterns.put("startDateEpoch", new TableInfo.Column("startDateEpoch", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShiftPatterns.put("cycleOffsetDay", new TableInfo.Column("cycleOffsetDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShiftPatterns.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShiftPatterns.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysShiftPatterns = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesShiftPatterns = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoShiftPatterns = new TableInfo("shift_patterns", _columnsShiftPatterns, _foreignKeysShiftPatterns, _indicesShiftPatterns);
        final TableInfo _existingShiftPatterns = TableInfo.read(db, "shift_patterns");
        if (!_infoShiftPatterns.equals(_existingShiftPatterns)) {
          return new RoomOpenHelper.ValidationResult(false, "shift_patterns(com.todaywork.app.data.db.entity.ShiftPatternEntity).\n"
                  + " Expected:\n" + _infoShiftPatterns + "\n"
                  + " Found:\n" + _existingShiftPatterns);
        }
        final HashMap<String, TableInfo.Column> _columnsWorkRecords = new HashMap<String, TableInfo.Column>(8);
        _columnsWorkRecords.put("dateEpoch", new TableInfo.Column("dateEpoch", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkRecords.put("shiftTypeName", new TableInfo.Column("shiftTypeName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkRecords.put("startTimeMinutes", new TableInfo.Column("startTimeMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkRecords.put("endTimeMinutes", new TableInfo.Column("endTimeMinutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkRecords.put("memo", new TableInfo.Column("memo", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkRecords.put("isManual", new TableInfo.Column("isManual", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkRecords.put("patternId", new TableInfo.Column("patternId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWorkRecords.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWorkRecords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWorkRecords = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWorkRecords = new TableInfo("work_records", _columnsWorkRecords, _foreignKeysWorkRecords, _indicesWorkRecords);
        final TableInfo _existingWorkRecords = TableInfo.read(db, "work_records");
        if (!_infoWorkRecords.equals(_existingWorkRecords)) {
          return new RoomOpenHelper.ValidationResult(false, "work_records(com.todaywork.app.data.db.entity.WorkRecordEntity).\n"
                  + " Expected:\n" + _infoWorkRecords + "\n"
                  + " Found:\n" + _existingWorkRecords);
        }
        final HashMap<String, TableInfo.Column> _columnsSalarySettings = new HashMap<String, TableInfo.Column>(10);
        _columnsSalarySettings.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSalarySettings.put("hourlyWage", new TableInfo.Column("hourlyWage", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSalarySettings.put("overtimeRate", new TableInfo.Column("overtimeRate", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSalarySettings.put("nightShiftRate", new TableInfo.Column("nightShiftRate", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSalarySettings.put("weekendRate", new TableInfo.Column("weekendRate", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSalarySettings.put("holidayRate", new TableInfo.Column("holidayRate", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSalarySettings.put("mealAllowancePerDay", new TableInfo.Column("mealAllowancePerDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSalarySettings.put("regularHoursPerDay", new TableInfo.Column("regularHoursPerDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSalarySettings.put("regularHoursPerWeek", new TableInfo.Column("regularHoursPerWeek", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSalarySettings.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSalarySettings = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSalarySettings = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSalarySettings = new TableInfo("salary_settings", _columnsSalarySettings, _foreignKeysSalarySettings, _indicesSalarySettings);
        final TableInfo _existingSalarySettings = TableInfo.read(db, "salary_settings");
        if (!_infoSalarySettings.equals(_existingSalarySettings)) {
          return new RoomOpenHelper.ValidationResult(false, "salary_settings(com.todaywork.app.data.db.entity.SalarySettingEntity).\n"
                  + " Expected:\n" + _infoSalarySettings + "\n"
                  + " Found:\n" + _existingSalarySettings);
        }
        final HashMap<String, TableInfo.Column> _columnsAlarmSettings = new HashMap<String, TableInfo.Column>(6);
        _columnsAlarmSettings.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlarmSettings.put("label", new TableInfo.Column("label", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlarmSettings.put("minutesBefore", new TableInfo.Column("minutesBefore", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlarmSettings.put("isEnabled", new TableInfo.Column("isEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlarmSettings.put("shiftTypeFilter", new TableInfo.Column("shiftTypeFilter", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlarmSettings.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAlarmSettings = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAlarmSettings = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAlarmSettings = new TableInfo("alarm_settings", _columnsAlarmSettings, _foreignKeysAlarmSettings, _indicesAlarmSettings);
        final TableInfo _existingAlarmSettings = TableInfo.read(db, "alarm_settings");
        if (!_infoAlarmSettings.equals(_existingAlarmSettings)) {
          return new RoomOpenHelper.ValidationResult(false, "alarm_settings(com.todaywork.app.data.db.entity.AlarmSettingEntity).\n"
                  + " Expected:\n" + _infoAlarmSettings + "\n"
                  + " Found:\n" + _existingAlarmSettings);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "c3a40ca4ed4c88759c0dbfb29ac5459c", "930a42e0c1b57b9a8fd4f5886d6b1774");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "shift_patterns","work_records","salary_settings","alarm_settings");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `shift_patterns`");
      _db.execSQL("DELETE FROM `work_records`");
      _db.execSQL("DELETE FROM `salary_settings`");
      _db.execSQL("DELETE FROM `alarm_settings`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ShiftPatternDao.class, ShiftPatternDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(WorkRecordDao.class, WorkRecordDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SalarySettingDao.class, SalarySettingDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AlarmSettingDao.class, AlarmSettingDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ShiftPatternDao shiftPatternDao() {
    if (_shiftPatternDao != null) {
      return _shiftPatternDao;
    } else {
      synchronized(this) {
        if(_shiftPatternDao == null) {
          _shiftPatternDao = new ShiftPatternDao_Impl(this);
        }
        return _shiftPatternDao;
      }
    }
  }

  @Override
  public WorkRecordDao workRecordDao() {
    if (_workRecordDao != null) {
      return _workRecordDao;
    } else {
      synchronized(this) {
        if(_workRecordDao == null) {
          _workRecordDao = new WorkRecordDao_Impl(this);
        }
        return _workRecordDao;
      }
    }
  }

  @Override
  public SalarySettingDao salarySettingDao() {
    if (_salarySettingDao != null) {
      return _salarySettingDao;
    } else {
      synchronized(this) {
        if(_salarySettingDao == null) {
          _salarySettingDao = new SalarySettingDao_Impl(this);
        }
        return _salarySettingDao;
      }
    }
  }

  @Override
  public AlarmSettingDao alarmSettingDao() {
    if (_alarmSettingDao != null) {
      return _alarmSettingDao;
    } else {
      synchronized(this) {
        if(_alarmSettingDao == null) {
          _alarmSettingDao = new AlarmSettingDao_Impl(this);
        }
        return _alarmSettingDao;
      }
    }
  }
}
