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
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.todaywork.app.data.db.entity.WorkRecordEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
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
public final class WorkRecordDao_Impl implements WorkRecordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<WorkRecordEntity> __insertionAdapterOfWorkRecordEntity;

  private final EntityDeletionOrUpdateAdapter<WorkRecordEntity> __deletionAdapterOfWorkRecordEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteByDate;

  private final SharedSQLiteStatement __preparedStmtOfDeletePatternGeneratedRecords;

  public WorkRecordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfWorkRecordEntity = new EntityInsertionAdapter<WorkRecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `work_records` (`dateEpoch`,`shiftTypeName`,`startTimeMinutes`,`endTimeMinutes`,`memo`,`isManual`,`patternId`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WorkRecordEntity entity) {
        statement.bindLong(1, entity.getDateEpoch());
        statement.bindString(2, entity.getShiftTypeName());
        statement.bindLong(3, entity.getStartTimeMinutes());
        statement.bindLong(4, entity.getEndTimeMinutes());
        statement.bindString(5, entity.getMemo());
        final int _tmp = entity.isManual() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getPatternId());
        statement.bindLong(8, entity.getUpdatedAt());
      }
    };
    this.__deletionAdapterOfWorkRecordEntity = new EntityDeletionOrUpdateAdapter<WorkRecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `work_records` WHERE `dateEpoch` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final WorkRecordEntity entity) {
        statement.bindLong(1, entity.getDateEpoch());
      }
    };
    this.__preparedStmtOfDeleteByDate = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM work_records WHERE dateEpoch = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeletePatternGeneratedRecords = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM work_records WHERE patternId = ? AND isManual = 0";
        return _query;
      }
    };
  }

  @Override
  public Object insertOrUpdate(final WorkRecordEntity record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWorkRecordEntity.insert(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<WorkRecordEntity> records,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfWorkRecordEntity.insert(records);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRecord(final WorkRecordEntity record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfWorkRecordEntity.handle(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteByDate(final long dateEpoch, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByDate.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, dateEpoch);
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
          __preparedStmtOfDeleteByDate.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePatternGeneratedRecords(final long patternId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeletePatternGeneratedRecords.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, patternId);
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
          __preparedStmtOfDeletePatternGeneratedRecords.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<WorkRecordEntity>> getRecordsBetween(final long startEpoch,
      final long endEpoch) {
    final String _sql = "SELECT * FROM work_records WHERE dateEpoch BETWEEN ? AND ? ORDER BY dateEpoch ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startEpoch);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endEpoch);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"work_records"}, new Callable<List<WorkRecordEntity>>() {
      @Override
      @NonNull
      public List<WorkRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDateEpoch = CursorUtil.getColumnIndexOrThrow(_cursor, "dateEpoch");
          final int _cursorIndexOfShiftTypeName = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftTypeName");
          final int _cursorIndexOfStartTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeMinutes");
          final int _cursorIndexOfEndTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeMinutes");
          final int _cursorIndexOfMemo = CursorUtil.getColumnIndexOrThrow(_cursor, "memo");
          final int _cursorIndexOfIsManual = CursorUtil.getColumnIndexOrThrow(_cursor, "isManual");
          final int _cursorIndexOfPatternId = CursorUtil.getColumnIndexOrThrow(_cursor, "patternId");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<WorkRecordEntity> _result = new ArrayList<WorkRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WorkRecordEntity _item;
            final long _tmpDateEpoch;
            _tmpDateEpoch = _cursor.getLong(_cursorIndexOfDateEpoch);
            final String _tmpShiftTypeName;
            _tmpShiftTypeName = _cursor.getString(_cursorIndexOfShiftTypeName);
            final int _tmpStartTimeMinutes;
            _tmpStartTimeMinutes = _cursor.getInt(_cursorIndexOfStartTimeMinutes);
            final int _tmpEndTimeMinutes;
            _tmpEndTimeMinutes = _cursor.getInt(_cursorIndexOfEndTimeMinutes);
            final String _tmpMemo;
            _tmpMemo = _cursor.getString(_cursorIndexOfMemo);
            final boolean _tmpIsManual;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManual);
            _tmpIsManual = _tmp != 0;
            final long _tmpPatternId;
            _tmpPatternId = _cursor.getLong(_cursorIndexOfPatternId);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new WorkRecordEntity(_tmpDateEpoch,_tmpShiftTypeName,_tmpStartTimeMinutes,_tmpEndTimeMinutes,_tmpMemo,_tmpIsManual,_tmpPatternId,_tmpUpdatedAt);
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
  public Object getRecordsBetweenSync(final long startEpoch, final long endEpoch,
      final Continuation<? super List<WorkRecordEntity>> $completion) {
    final String _sql = "SELECT * FROM work_records WHERE dateEpoch BETWEEN ? AND ? ORDER BY dateEpoch ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startEpoch);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endEpoch);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<WorkRecordEntity>>() {
      @Override
      @NonNull
      public List<WorkRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDateEpoch = CursorUtil.getColumnIndexOrThrow(_cursor, "dateEpoch");
          final int _cursorIndexOfShiftTypeName = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftTypeName");
          final int _cursorIndexOfStartTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeMinutes");
          final int _cursorIndexOfEndTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeMinutes");
          final int _cursorIndexOfMemo = CursorUtil.getColumnIndexOrThrow(_cursor, "memo");
          final int _cursorIndexOfIsManual = CursorUtil.getColumnIndexOrThrow(_cursor, "isManual");
          final int _cursorIndexOfPatternId = CursorUtil.getColumnIndexOrThrow(_cursor, "patternId");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<WorkRecordEntity> _result = new ArrayList<WorkRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final WorkRecordEntity _item;
            final long _tmpDateEpoch;
            _tmpDateEpoch = _cursor.getLong(_cursorIndexOfDateEpoch);
            final String _tmpShiftTypeName;
            _tmpShiftTypeName = _cursor.getString(_cursorIndexOfShiftTypeName);
            final int _tmpStartTimeMinutes;
            _tmpStartTimeMinutes = _cursor.getInt(_cursorIndexOfStartTimeMinutes);
            final int _tmpEndTimeMinutes;
            _tmpEndTimeMinutes = _cursor.getInt(_cursorIndexOfEndTimeMinutes);
            final String _tmpMemo;
            _tmpMemo = _cursor.getString(_cursorIndexOfMemo);
            final boolean _tmpIsManual;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManual);
            _tmpIsManual = _tmp != 0;
            final long _tmpPatternId;
            _tmpPatternId = _cursor.getLong(_cursorIndexOfPatternId);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new WorkRecordEntity(_tmpDateEpoch,_tmpShiftTypeName,_tmpStartTimeMinutes,_tmpEndTimeMinutes,_tmpMemo,_tmpIsManual,_tmpPatternId,_tmpUpdatedAt);
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

  @Override
  public Object getRecordByDate(final long dateEpoch,
      final Continuation<? super WorkRecordEntity> $completion) {
    final String _sql = "SELECT * FROM work_records WHERE dateEpoch = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, dateEpoch);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<WorkRecordEntity>() {
      @Override
      @Nullable
      public WorkRecordEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDateEpoch = CursorUtil.getColumnIndexOrThrow(_cursor, "dateEpoch");
          final int _cursorIndexOfShiftTypeName = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftTypeName");
          final int _cursorIndexOfStartTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeMinutes");
          final int _cursorIndexOfEndTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeMinutes");
          final int _cursorIndexOfMemo = CursorUtil.getColumnIndexOrThrow(_cursor, "memo");
          final int _cursorIndexOfIsManual = CursorUtil.getColumnIndexOrThrow(_cursor, "isManual");
          final int _cursorIndexOfPatternId = CursorUtil.getColumnIndexOrThrow(_cursor, "patternId");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final WorkRecordEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpDateEpoch;
            _tmpDateEpoch = _cursor.getLong(_cursorIndexOfDateEpoch);
            final String _tmpShiftTypeName;
            _tmpShiftTypeName = _cursor.getString(_cursorIndexOfShiftTypeName);
            final int _tmpStartTimeMinutes;
            _tmpStartTimeMinutes = _cursor.getInt(_cursorIndexOfStartTimeMinutes);
            final int _tmpEndTimeMinutes;
            _tmpEndTimeMinutes = _cursor.getInt(_cursorIndexOfEndTimeMinutes);
            final String _tmpMemo;
            _tmpMemo = _cursor.getString(_cursorIndexOfMemo);
            final boolean _tmpIsManual;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManual);
            _tmpIsManual = _tmp != 0;
            final long _tmpPatternId;
            _tmpPatternId = _cursor.getLong(_cursorIndexOfPatternId);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new WorkRecordEntity(_tmpDateEpoch,_tmpShiftTypeName,_tmpStartTimeMinutes,_tmpEndTimeMinutes,_tmpMemo,_tmpIsManual,_tmpPatternId,_tmpUpdatedAt);
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

  @Override
  public Flow<WorkRecordEntity> getRecordByDateFlow(final long dateEpoch) {
    final String _sql = "SELECT * FROM work_records WHERE dateEpoch = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, dateEpoch);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"work_records"}, new Callable<WorkRecordEntity>() {
      @Override
      @Nullable
      public WorkRecordEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDateEpoch = CursorUtil.getColumnIndexOrThrow(_cursor, "dateEpoch");
          final int _cursorIndexOfShiftTypeName = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftTypeName");
          final int _cursorIndexOfStartTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeMinutes");
          final int _cursorIndexOfEndTimeMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "endTimeMinutes");
          final int _cursorIndexOfMemo = CursorUtil.getColumnIndexOrThrow(_cursor, "memo");
          final int _cursorIndexOfIsManual = CursorUtil.getColumnIndexOrThrow(_cursor, "isManual");
          final int _cursorIndexOfPatternId = CursorUtil.getColumnIndexOrThrow(_cursor, "patternId");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final WorkRecordEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpDateEpoch;
            _tmpDateEpoch = _cursor.getLong(_cursorIndexOfDateEpoch);
            final String _tmpShiftTypeName;
            _tmpShiftTypeName = _cursor.getString(_cursorIndexOfShiftTypeName);
            final int _tmpStartTimeMinutes;
            _tmpStartTimeMinutes = _cursor.getInt(_cursorIndexOfStartTimeMinutes);
            final int _tmpEndTimeMinutes;
            _tmpEndTimeMinutes = _cursor.getInt(_cursorIndexOfEndTimeMinutes);
            final String _tmpMemo;
            _tmpMemo = _cursor.getString(_cursorIndexOfMemo);
            final boolean _tmpIsManual;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsManual);
            _tmpIsManual = _tmp != 0;
            final long _tmpPatternId;
            _tmpPatternId = _cursor.getLong(_cursorIndexOfPatternId);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new WorkRecordEntity(_tmpDateEpoch,_tmpShiftTypeName,_tmpStartTimeMinutes,_tmpEndTimeMinutes,_tmpMemo,_tmpIsManual,_tmpPatternId,_tmpUpdatedAt);
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
  public Object countByTypesAndRange(final List<String> types, final long startEpoch,
      final long endEpoch, final Continuation<? super Integer> $completion) {
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT COUNT(*) FROM work_records WHERE shiftTypeName IN (");
    final int _inputSize = types.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(") AND dateEpoch BETWEEN ");
    _stringBuilder.append("?");
    _stringBuilder.append(" AND ");
    _stringBuilder.append("?");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 2 + _inputSize;
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (String _item : types) {
      _statement.bindString(_argIndex, _item);
      _argIndex++;
    }
    _argIndex = 1 + _inputSize;
    _statement.bindLong(_argIndex, startEpoch);
    _argIndex = 2 + _inputSize;
    _statement.bindLong(_argIndex, endEpoch);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
