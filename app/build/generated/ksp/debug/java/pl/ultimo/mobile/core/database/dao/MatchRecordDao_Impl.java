package pl.ultimo.mobile.core.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.Integer;
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
import pl.ultimo.mobile.core.database.entity.MatchRecordEntity;
import pl.ultimo.mobile.core.database.entity.MatchResult;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MatchRecordDao_Impl implements MatchRecordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MatchRecordEntity> __insertionAdapterOfMatchRecordEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearHistory;

  public MatchRecordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMatchRecordEntity = new EntityInsertionAdapter<MatchRecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `match_records` (`id`,`timestampEpochMs`,`roomId`,`opponentCount`,`result`,`cardsPlayed`,`matchDurationSeconds`,`finalScore`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MatchRecordEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindLong(2, entity.getTimestampEpochMs());
        statement.bindString(3, entity.getRoomId());
        statement.bindLong(4, entity.getOpponentCount());
        statement.bindString(5, __MatchResult_enumToString(entity.getResult()));
        statement.bindLong(6, entity.getCardsPlayed());
        statement.bindLong(7, entity.getMatchDurationSeconds());
        statement.bindLong(8, entity.getFinalScore());
      }
    };
    this.__preparedStmtOfClearHistory = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM match_records";
        return _query;
      }
    };
  }

  @Override
  public Object insertMatch(final MatchRecordEntity match,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMatchRecordEntity.insert(match);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearHistory(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearHistory.acquire();
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
          __preparedStmtOfClearHistory.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MatchRecordEntity>> observeRecentMatches() {
    final String _sql = "SELECT * FROM match_records ORDER BY timestampEpochMs DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"match_records"}, new Callable<List<MatchRecordEntity>>() {
      @Override
      @NonNull
      public List<MatchRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestampEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampEpochMs");
          final int _cursorIndexOfRoomId = CursorUtil.getColumnIndexOrThrow(_cursor, "roomId");
          final int _cursorIndexOfOpponentCount = CursorUtil.getColumnIndexOrThrow(_cursor, "opponentCount");
          final int _cursorIndexOfResult = CursorUtil.getColumnIndexOrThrow(_cursor, "result");
          final int _cursorIndexOfCardsPlayed = CursorUtil.getColumnIndexOrThrow(_cursor, "cardsPlayed");
          final int _cursorIndexOfMatchDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "matchDurationSeconds");
          final int _cursorIndexOfFinalScore = CursorUtil.getColumnIndexOrThrow(_cursor, "finalScore");
          final List<MatchRecordEntity> _result = new ArrayList<MatchRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MatchRecordEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpTimestampEpochMs;
            _tmpTimestampEpochMs = _cursor.getLong(_cursorIndexOfTimestampEpochMs);
            final String _tmpRoomId;
            _tmpRoomId = _cursor.getString(_cursorIndexOfRoomId);
            final int _tmpOpponentCount;
            _tmpOpponentCount = _cursor.getInt(_cursorIndexOfOpponentCount);
            final MatchResult _tmpResult;
            _tmpResult = __MatchResult_stringToEnum(_cursor.getString(_cursorIndexOfResult));
            final int _tmpCardsPlayed;
            _tmpCardsPlayed = _cursor.getInt(_cursorIndexOfCardsPlayed);
            final long _tmpMatchDurationSeconds;
            _tmpMatchDurationSeconds = _cursor.getLong(_cursorIndexOfMatchDurationSeconds);
            final int _tmpFinalScore;
            _tmpFinalScore = _cursor.getInt(_cursorIndexOfFinalScore);
            _item = new MatchRecordEntity(_tmpId,_tmpTimestampEpochMs,_tmpRoomId,_tmpOpponentCount,_tmpResult,_tmpCardsPlayed,_tmpMatchDurationSeconds,_tmpFinalScore);
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
  public Flow<List<MatchRecordEntity>> observeRecentMatches(final int limit) {
    final String _sql = "SELECT * FROM match_records ORDER BY timestampEpochMs DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"match_records"}, new Callable<List<MatchRecordEntity>>() {
      @Override
      @NonNull
      public List<MatchRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestampEpochMs = CursorUtil.getColumnIndexOrThrow(_cursor, "timestampEpochMs");
          final int _cursorIndexOfRoomId = CursorUtil.getColumnIndexOrThrow(_cursor, "roomId");
          final int _cursorIndexOfOpponentCount = CursorUtil.getColumnIndexOrThrow(_cursor, "opponentCount");
          final int _cursorIndexOfResult = CursorUtil.getColumnIndexOrThrow(_cursor, "result");
          final int _cursorIndexOfCardsPlayed = CursorUtil.getColumnIndexOrThrow(_cursor, "cardsPlayed");
          final int _cursorIndexOfMatchDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "matchDurationSeconds");
          final int _cursorIndexOfFinalScore = CursorUtil.getColumnIndexOrThrow(_cursor, "finalScore");
          final List<MatchRecordEntity> _result = new ArrayList<MatchRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MatchRecordEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpTimestampEpochMs;
            _tmpTimestampEpochMs = _cursor.getLong(_cursorIndexOfTimestampEpochMs);
            final String _tmpRoomId;
            _tmpRoomId = _cursor.getString(_cursorIndexOfRoomId);
            final int _tmpOpponentCount;
            _tmpOpponentCount = _cursor.getInt(_cursorIndexOfOpponentCount);
            final MatchResult _tmpResult;
            _tmpResult = __MatchResult_stringToEnum(_cursor.getString(_cursorIndexOfResult));
            final int _tmpCardsPlayed;
            _tmpCardsPlayed = _cursor.getInt(_cursorIndexOfCardsPlayed);
            final long _tmpMatchDurationSeconds;
            _tmpMatchDurationSeconds = _cursor.getLong(_cursorIndexOfMatchDurationSeconds);
            final int _tmpFinalScore;
            _tmpFinalScore = _cursor.getInt(_cursorIndexOfFinalScore);
            _item = new MatchRecordEntity(_tmpId,_tmpTimestampEpochMs,_tmpRoomId,_tmpOpponentCount,_tmpResult,_tmpCardsPlayed,_tmpMatchDurationSeconds,_tmpFinalScore);
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
  public Object getMatchCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM match_records";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
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

  private String __MatchResult_enumToString(@NonNull final MatchResult _value) {
    switch (_value) {
      case WIN: return "WIN";
      case LOSS: return "LOSS";
      case ABANDONED: return "ABANDONED";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private MatchResult __MatchResult_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "WIN": return MatchResult.WIN;
      case "LOSS": return MatchResult.LOSS;
      case "ABANDONED": return MatchResult.ABANDONED;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
