package com.todaywork.app.data.db.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.todaywork.app.data.db.entity.AlarmSettingEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AlarmSettingDao_Impl implements AlarmSettingDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AlarmSettingEntity> __insertionAdapterOfAlarmSettingEntity;

  private final EntityDeletionOrUpdateAdapter<AlarmSettingEntity> __deletionAdapterOfAlarmSettingEntity;

  private final EntityDeletionOrUpdateAdapter<AlarmSettingEntity> __updateAdapterOfAlarmSettingEntity;

  private final SharedSQLiteStatement __preparedStmtOfSetEnabled;

  public AlarmSettingDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAlarmSettingEntity = new EntityInsertionAdapter<AlarmSettingEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `alarm_settings` (`id`,`label`,`minutesBefore`,`isEnabled`,`shiftTypeFilter`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AlarmSettingEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getLabel());
        statement.bindLong(3, entity.getMinutesBefore());
        final int _tmp = entity.isEnabled() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindString(5, entity.getShiftTypeFilter());
        statement.bindLong(6, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfAlarmSettingEntity = new EntityDeletionOrUpdateAdapter<AlarmSettingEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `alarm_settings` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AlarmSettingEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfAlarmSettingEntity = new EntityDeletionOrUpdateAdapter<AlarmSettingEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `alarm_settings` SET `id` = ?,`label` = ?,`minutesBefore` = ?,`isEnabled` = ?,`shiftTypeFilter` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AlarmSettingEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getLabel());
        statement.bindLong(3, entity.getMinutesBefore());
        final int _tmp = entity.isEnabled() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindString(5, entity.getShiftTypeFilter());
        statement.bindLong(6, entity.getCreatedAt());
        statement.bindLong(7, entity.getId());
      }
    };
    this.__preparedStmtOfSetEnabled = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE alarm_settings SET isEnabled = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final AlarmSettingEntity alarm,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfAlarmSettingEntity.insertAndReturnId(alarm);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final AlarmSettingEntity alarm,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfAlarmSettingEntity.handle(alarm);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final AlarmSettingEntity alarm,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAlarmSettingEntity.handle(alarm);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object setEnabled(final long id, final boolean enabled,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetEnabled.acquire();
        int _argIndex = 1;
        final int _tmp = enabled ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfSetEnabled.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AlarmSettingEntity>> getAllAlarms() {
    final String _sql = "SELECT * FROM alarm_settings ORDER BY minutesBefore DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"alarm_settings"}, new Callable<List<AlarmSettingEntity>>() {
      @Override
      @NonNull
      public List<AlarmSettingEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfMinutesBefore = CursorUtil.getColumnIndexOrThrow(_cursor, "minutesBefore");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfShiftTypeFilter = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftTypeFilter");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AlarmSettingEntity> _result = new ArrayList<AlarmSettingEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlarmSettingEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final int _tmpMinutesBefore;
            _tmpMinutesBefore = _cursor.getInt(_cursorIndexOfMinutesBefore);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            final String _tmpShiftTypeFilter;
            _tmpShiftTypeFilter = _cursor.getString(_cursorIndexOfShiftTypeFilter);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new AlarmSettingEntity(_tmpId,_tmpLabel,_tmpMinutesBefore,_tmpIsEnabled,_tmpShiftTypeFilter,_tmpCreatedAt);
            _result.add(_item);
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
  public Object getEnabledAlarms(final Continuation<? super List<AlarmSettingEntity>> $completion) {
    final String _sql = "SELECT * FROM alarm_settings WHERE isEnabled = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AlarmSettingEntity>>() {
      @Override
      @NonNull
      public List<AlarmSettingEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfMinutesBefore = CursorUtil.getColumnIndexOrThrow(_cursor, "minutesBefore");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfShiftTypeFilter = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftTypeFilter");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AlarmSettingEntity> _result = new ArrayList<AlarmSettingEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlarmSettingEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final int _tmpMinutesBefore;
            _tmpMinutesBefore = _cursor.getInt(_cursorIndexOfMinutesBefore);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            final String _tmpShiftTypeFilter;
            _tmpShiftTypeFilter = _cursor.getString(_cursorIndexOfShiftTypeFilter);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new AlarmSettingEntity(_tmpId,_tmpLabel,_tmpMinutesBefore,_tmpIsEnabled,_tmpShiftTypeFilter,_tmpCreatedAt);
            _result.add(_item);
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
