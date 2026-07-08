package com.todaywork.app.data.db.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.todaywork.app.data.db.entity.SalarySettingEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SalarySettingDao_Impl implements SalarySettingDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SalarySettingEntity> __insertionAdapterOfSalarySettingEntity;

  public SalarySettingDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSalarySettingEntity = new EntityInsertionAdapter<SalarySettingEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `salary_settings` (`id`,`hourlyWage`,`overtimeRate`,`nightShiftRate`,`weekendRate`,`holidayRate`,`mealAllowancePerDay`,`regularHoursPerDay`,`regularHoursPerWeek`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SalarySettingEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getHourlyWage());
        statement.bindDouble(3, entity.getOvertimeRate());
        statement.bindDouble(4, entity.getNightShiftRate());
        statement.bindDouble(5, entity.getWeekendRate());
        statement.bindDouble(6, entity.getHolidayRate());
        statement.bindLong(7, entity.getMealAllowancePerDay());
        statement.bindLong(8, entity.getRegularHoursPerDay());
        statement.bindLong(9, entity.getRegularHoursPerWeek());
        statement.bindLong(10, entity.getUpdatedAt());
      }
    };
  }

  @Override
  public Object upsert(final SalarySettingEntity setting,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSalarySettingEntity.insert(setting);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<SalarySettingEntity> getSalarySettings() {
    final String _sql = "SELECT * FROM salary_settings WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"salary_settings"}, new Callable<SalarySettingEntity>() {
      @Override
      @Nullable
      public SalarySettingEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHourlyWage = CursorUtil.getColumnIndexOrThrow(_cursor, "hourlyWage");
          final int _cursorIndexOfOvertimeRate = CursorUtil.getColumnIndexOrThrow(_cursor, "overtimeRate");
          final int _cursorIndexOfNightShiftRate = CursorUtil.getColumnIndexOrThrow(_cursor, "nightShiftRate");
          final int _cursorIndexOfWeekendRate = CursorUtil.getColumnIndexOrThrow(_cursor, "weekendRate");
          final int _cursorIndexOfHolidayRate = CursorUtil.getColumnIndexOrThrow(_cursor, "holidayRate");
          final int _cursorIndexOfMealAllowancePerDay = CursorUtil.getColumnIndexOrThrow(_cursor, "mealAllowancePerDay");
          final int _cursorIndexOfRegularHoursPerDay = CursorUtil.getColumnIndexOrThrow(_cursor, "regularHoursPerDay");
          final int _cursorIndexOfRegularHoursPerWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "regularHoursPerWeek");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final SalarySettingEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpHourlyWage;
            _tmpHourlyWage = _cursor.getInt(_cursorIndexOfHourlyWage);
            final float _tmpOvertimeRate;
            _tmpOvertimeRate = _cursor.getFloat(_cursorIndexOfOvertimeRate);
            final float _tmpNightShiftRate;
            _tmpNightShiftRate = _cursor.getFloat(_cursorIndexOfNightShiftRate);
            final float _tmpWeekendRate;
            _tmpWeekendRate = _cursor.getFloat(_cursorIndexOfWeekendRate);
            final float _tmpHolidayRate;
            _tmpHolidayRate = _cursor.getFloat(_cursorIndexOfHolidayRate);
            final int _tmpMealAllowancePerDay;
            _tmpMealAllowancePerDay = _cursor.getInt(_cursorIndexOfMealAllowancePerDay);
            final int _tmpRegularHoursPerDay;
            _tmpRegularHoursPerDay = _cursor.getInt(_cursorIndexOfRegularHoursPerDay);
            final int _tmpRegularHoursPerWeek;
            _tmpRegularHoursPerWeek = _cursor.getInt(_cursorIndexOfRegularHoursPerWeek);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new SalarySettingEntity(_tmpId,_tmpHourlyWage,_tmpOvertimeRate,_tmpNightShiftRate,_tmpWeekendRate,_tmpHolidayRate,_tmpMealAllowancePerDay,_tmpRegularHoursPerDay,_tmpRegularHoursPerWeek,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getSalarySettingsSync(final Continuation<? super SalarySettingEntity> $completion) {
    final String _sql = "SELECT * FROM salary_settings WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<SalarySettingEntity>() {
      @Override
      @Nullable
      public SalarySettingEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHourlyWage = CursorUtil.getColumnIndexOrThrow(_cursor, "hourlyWage");
          final int _cursorIndexOfOvertimeRate = CursorUtil.getColumnIndexOrThrow(_cursor, "overtimeRate");
          final int _cursorIndexOfNightShiftRate = CursorUtil.getColumnIndexOrThrow(_cursor, "nightShiftRate");
          final int _cursorIndexOfWeekendRate = CursorUtil.getColumnIndexOrThrow(_cursor, "weekendRate");
          final int _cursorIndexOfHolidayRate = CursorUtil.getColumnIndexOrThrow(_cursor, "holidayRate");
          final int _cursorIndexOfMealAllowancePerDay = CursorUtil.getColumnIndexOrThrow(_cursor, "mealAllowancePerDay");
          final int _cursorIndexOfRegularHoursPerDay = CursorUtil.getColumnIndexOrThrow(_cursor, "regularHoursPerDay");
          final int _cursorIndexOfRegularHoursPerWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "regularHoursPerWeek");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final SalarySettingEntity _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpHourlyWage;
            _tmpHourlyWage = _cursor.getInt(_cursorIndexOfHourlyWage);
            final float _tmpOvertimeRate;
            _tmpOvertimeRate = _cursor.getFloat(_cursorIndexOfOvertimeRate);
            final float _tmpNightShiftRate;
            _tmpNightShiftRate = _cursor.getFloat(_cursorIndexOfNightShiftRate);
            final float _tmpWeekendRate;
            _tmpWeekendRate = _cursor.getFloat(_cursorIndexOfWeekendRate);
            final float _tmpHolidayRate;
            _tmpHolidayRate = _cursor.getFloat(_cursorIndexOfHolidayRate);
            final int _tmpMealAllowancePerDay;
            _tmpMealAllowancePerDay = _cursor.getInt(_cursorIndexOfMealAllowancePerDay);
            final int _tmpRegularHoursPerDay;
            _tmpRegularHoursPerDay = _cursor.getInt(_cursorIndexOfRegularHoursPerDay);
            final int _tmpRegularHoursPerWeek;
            _tmpRegularHoursPerWeek = _cursor.getInt(_cursorIndexOfRegularHoursPerWeek);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new SalarySettingEntity(_tmpId,_tmpHourlyWage,_tmpOvertimeRate,_tmpNightShiftRate,_tmpWeekendRate,_tmpHolidayRate,_tmpMealAllowancePerDay,_tmpRegularHoursPerDay,_tmpRegularHoursPerWeek,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
