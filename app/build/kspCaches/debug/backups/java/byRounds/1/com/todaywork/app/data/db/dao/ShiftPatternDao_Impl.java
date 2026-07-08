package com.todaywork.app.data.db.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.todaywork.app.data.db.entity.ShiftPatternEntity;
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
public final class ShiftPatternDao_Impl implements ShiftPatternDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ShiftPatternEntity> __insertionAdapterOfShiftPatternEntity;

  private final EntityDeletionOrUpdateAdapter<ShiftPatternEntity> __deletionAdapterOfShiftPatternEntity;

  private final EntityDeletionOrUpdateAdapter<ShiftPatternEntity> __updateAdapterOfShiftPatternEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeactivateAll;

  private final SharedSQLiteStatement __preparedStmtOfActivatePattern;

  private final SharedSQLiteStatement __preparedStmtOfDeletePatternById;

  public ShiftPatternDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfShiftPatternEntity = new EntityInsertionAdapter<ShiftPatternEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `shift_patterns` (`id`,`name`,`cyclesJson`,`startDateEpoch`,`cycleOffsetDay`,`isActive`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ShiftPatternEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getCyclesJson());
        statement.bindLong(4, entity.getStartDateEpoch());
        statement.bindLong(5, entity.getCycleOffsetDay());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfShiftPatternEntity = new EntityDeletionOrUpdateAdapter<ShiftPatternEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `shift_patterns` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ShiftPatternEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfShiftPatternEntity = new EntityDeletionOrUpdateAdapter<ShiftPatternEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `shift_patterns` SET `id` = ?,`name` = ?,`cyclesJson` = ?,`startDateEpoch` = ?,`cycleOffsetDay` = ?,`isActive` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ShiftPatternEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getCyclesJson());
        statement.bindLong(4, entity.getStartDateEpoch());
        statement.bindLong(5, entity.getCycleOffsetDay());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getCreatedAt());
        statement.bindLong(8, entity.getId());
      }
    };
    this.__preparedStmtOfDeactivateAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE shift_patterns SET isActive = 0";
        return _query;
      }
    };
    this.__preparedStmtOfActivatePattern = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE shift_patterns SET isActive = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeletePatternById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM shift_patterns WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertPattern(final ShiftPatternEntity pattern,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfShiftPatternEntity.insertAndReturnId(pattern);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePattern(final ShiftPatternEntity pattern,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfShiftPatternEntity.handle(pattern);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePattern(final ShiftPatternEntity pattern,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfShiftPatternEntity.handle(pattern);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deactivateAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeactivateAll.acquire();
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
          __preparedStmtOfDeactivateAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object activatePattern(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfActivatePattern.acquire();
        int _argIndex = 1;
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
          __preparedStmtOfActivatePattern.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePatternById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeletePatternById.acquire();
        int _argIndex = 1;
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
          __preparedStmtOfDeletePatternById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ShiftPatternEntity>> getAllPatterns() {
    final String _sql = "SELECT * FROM shift_patterns ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"shift_patterns"}, new Callable<List<ShiftPatternEntity>>() {
      @Override
      @NonNull
      public List<ShiftPatternEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCyclesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "cyclesJson");
          final int _cursorIndexOfStartDateEpoch = CursorUtil.getColumnIndexOrThrow(_cursor, "startDateEpoch");
          final int _cursorIndexOfCycleOffsetDay = CursorUtil.getColumnIndexOrThrow(_cursor, "cycleOffsetDay");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<ShiftPatternEntity> _result = new ArrayList<ShiftPatternEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ShiftPatternEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCyclesJson;
            _tmpCyclesJson = _cursor.getString(_cursorIndexOfCyclesJson);
            final long _tmpStartDateEpoch;
            _tmpStartDateEpoch = _cursor.getLong(_cursorIndexOfStartDateEpoch);
            final int _tmpCycleOffsetDay;
            _tmpCycleOffsetDay = _cursor.getInt(_cursorIndexOfCycleOffsetDay);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new ShiftPatternEntity(_tmpId,_tmpName,_tmpCyclesJson,_tmpStartDateEpoch,_tmpCycleOffsetDay,_tmpIsActive,_tmpCreatedAt);
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
  public Flow<ShiftPatternEntity> getActivePattern() {
    final String _sql = "SELECT * FROM shift_patterns WHERE isActive = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"shift_patterns"}, new Callable<ShiftPatternEntity>() {
      @Override
      @Nullable
      public ShiftPatternEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCyclesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "cyclesJson");
          final int _cursorIndexOfStartDateEpoch = CursorUtil.getColumnIndexOrThrow(_cursor, "startDateEpoch");
          final int _cursorIndexOfCycleOffsetDay = CursorUtil.getColumnIndexOrThrow(_cursor, "cycleOffsetDay");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final ShiftPatternEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCyclesJson;
            _tmpCyclesJson = _cursor.getString(_cursorIndexOfCyclesJson);
            final long _tmpStartDateEpoch;
            _tmpStartDateEpoch = _cursor.getLong(_cursorIndexOfStartDateEpoch);
            final int _tmpCycleOffsetDay;
            _tmpCycleOffsetDay = _cursor.getInt(_cursorIndexOfCycleOffsetDay);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new ShiftPatternEntity(_tmpId,_tmpName,_tmpCyclesJson,_tmpStartDateEpoch,_tmpCycleOffsetDay,_tmpIsActive,_tmpCreatedAt);
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
  public Object getPatternById(final long id,
      final Continuation<? super ShiftPatternEntity> $completion) {
    final String _sql = "SELECT * FROM shift_patterns WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ShiftPatternEntity>() {
      @Override
      @Nullable
      public ShiftPatternEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCyclesJson = CursorUtil.getColumnIndexOrThrow(_cursor, "cyclesJson");
          final int _cursorIndexOfStartDateEpoch = CursorUtil.getColumnIndexOrThrow(_cursor, "startDateEpoch");
          final int _cursorIndexOfCycleOffsetDay = CursorUtil.getColumnIndexOrThrow(_cursor, "cycleOffsetDay");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final ShiftPatternEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCyclesJson;
            _tmpCyclesJson = _cursor.getString(_cursorIndexOfCyclesJson);
            final long _tmpStartDateEpoch;
            _tmpStartDateEpoch = _cursor.getLong(_cursorIndexOfStartDateEpoch);
            final int _tmpCycleOffsetDay;
            _tmpCycleOffsetDay = _cursor.getInt(_cursorIndexOfCycleOffsetDay);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new ShiftPatternEntity(_tmpId,_tmpName,_tmpCyclesJson,_tmpStartDateEpoch,_tmpCycleOffsetDay,_tmpIsActive,_tmpCreatedAt);
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
