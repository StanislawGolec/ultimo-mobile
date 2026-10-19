package pl.ultimo.mobile.core.database.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import pl.ultimo.mobile.core.database.entity.CardStatEntity;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CardStatDao_Impl implements CardStatDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CardStatEntity> __insertionAdapterOfCardStatEntity;

  private final SharedSQLiteStatement __preparedStmtOfRecordCardPlayed;

  private final SharedSQLiteStatement __preparedStmtOfRecordCardDrawn;

  public CardStatDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCardStatEntity = new EntityInsertionAdapter<CardStatEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `card_stats` (`cardToken`,`timesPlayed`,`timesDrawn`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CardStatEntity entity) {
        statement.bindString(1, entity.getCardToken());
        statement.bindLong(2, entity.getTimesPlayed());
        statement.bindLong(3, entity.getTimesDrawn());
      }
    };
    this.__preparedStmtOfRecordCardPlayed = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        INSERT INTO card_stats (cardToken, timesPlayed, timesDrawn) \n"
                + "        VALUES (?, 1, 0) \n"
                + "        ON CONFLICT(cardToken) DO UPDATE SET timesPlayed = timesPlayed + 1\n"
                + "        ";
        return _query;
      }
    };
    this.__preparedStmtOfRecordCardDrawn = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        INSERT INTO card_stats (cardToken, timesPlayed, timesDrawn) \n"
                + "        VALUES (?, 0, 1) \n"
                + "        ON CONFLICT(cardToken) DO UPDATE SET timesDrawn = timesDrawn + 1\n"
                + "        ";
        return _query;
      }
    };
  }

  @Override
  public Object insertOrUpdate(final CardStatEntity stat,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCardStatEntity.insert(stat);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object recordCardPlayed(final String cardToken,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRecordCardPlayed.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, cardToken);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeInsert();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfRecordCardPlayed.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object recordCardDrawn(final String cardToken,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRecordCardDrawn.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, cardToken);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeInsert();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfRecordCardDrawn.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CardStatEntity>> observeMostPlayedCards(final int limit) {
    final String _sql = "SELECT * FROM card_stats ORDER BY timesPlayed DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"card_stats"}, new Callable<List<CardStatEntity>>() {
      @Override
      @NonNull
      public List<CardStatEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCardToken = CursorUtil.getColumnIndexOrThrow(_cursor, "cardToken");
          final int _cursorIndexOfTimesPlayed = CursorUtil.getColumnIndexOrThrow(_cursor, "timesPlayed");
          final int _cursorIndexOfTimesDrawn = CursorUtil.getColumnIndexOrThrow(_cursor, "timesDrawn");
          final List<CardStatEntity> _result = new ArrayList<CardStatEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CardStatEntity _item;
            final String _tmpCardToken;
            _tmpCardToken = _cursor.getString(_cursorIndexOfCardToken);
            final int _tmpTimesPlayed;
            _tmpTimesPlayed = _cursor.getInt(_cursorIndexOfTimesPlayed);
            final int _tmpTimesDrawn;
            _tmpTimesDrawn = _cursor.getInt(_cursorIndexOfTimesDrawn);
            _item = new CardStatEntity(_tmpCardToken,_tmpTimesPlayed,_tmpTimesDrawn);
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
  public Object getCardStat(final String cardToken,
      final Continuation<? super CardStatEntity> $completion) {
    final String _sql = "SELECT * FROM card_stats WHERE cardToken = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, cardToken);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<CardStatEntity>() {
      @Override
      @Nullable
      public CardStatEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfCardToken = CursorUtil.getColumnIndexOrThrow(_cursor, "cardToken");
          final int _cursorIndexOfTimesPlayed = CursorUtil.getColumnIndexOrThrow(_cursor, "timesPlayed");
          final int _cursorIndexOfTimesDrawn = CursorUtil.getColumnIndexOrThrow(_cursor, "timesDrawn");
          final CardStatEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpCardToken;
            _tmpCardToken = _cursor.getString(_cursorIndexOfCardToken);
            final int _tmpTimesPlayed;
            _tmpTimesPlayed = _cursor.getInt(_cursorIndexOfTimesPlayed);
            final int _tmpTimesDrawn;
            _tmpTimesDrawn = _cursor.getInt(_cursorIndexOfTimesDrawn);
            _result = new CardStatEntity(_tmpCardToken,_tmpTimesPlayed,_tmpTimesDrawn);
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
