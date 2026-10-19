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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;
import pl.ultimo.mobile.core.database.entity.PlayerProfileEntity;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PlayerDao_Impl implements PlayerDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PlayerProfileEntity> __insertionAdapterOfPlayerProfileEntity;

  private final SharedSQLiteStatement __preparedStmtOfRecordVictory;

  private final SharedSQLiteStatement __preparedStmtOfRecordDefeat;

  public PlayerDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPlayerProfileEntity = new EntityInsertionAdapter<PlayerProfileEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `player_profiles` (`id`,`handle`,`avatarIndex`,`totalMatches`,`totalWins`,`currentStreak`,`bestStreak`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlayerProfileEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getHandle());
        statement.bindLong(3, entity.getAvatarIndex());
        statement.bindLong(4, entity.getTotalMatches());
        statement.bindLong(5, entity.getTotalWins());
        statement.bindLong(6, entity.getCurrentStreak());
        statement.bindLong(7, entity.getBestStreak());
      }
    };
    this.__preparedStmtOfRecordVictory = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE player_profiles \n"
                + "        SET totalMatches = totalMatches + 1, \n"
                + "            totalWins = totalWins + 1, \n"
                + "            currentStreak = currentStreak + 1, \n"
                + "            bestStreak = CASE WHEN currentStreak + 1 > bestStreak THEN currentStreak + 1 ELSE bestStreak END \n"
                + "        WHERE id = ?\n"
                + "        ";
        return _query;
      }
    };
    this.__preparedStmtOfRecordDefeat = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE player_profiles \n"
                + "        SET totalMatches = totalMatches + 1, \n"
                + "            currentStreak = 0 \n"
                + "        WHERE id = ?\n"
                + "        ";
        return _query;
      }
    };
  }

  @Override
  public Object insertOrUpdate(final PlayerProfileEntity profile,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPlayerProfileEntity.insert(profile);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object recordVictory(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRecordVictory.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfRecordVictory.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object recordDefeat(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRecordDefeat.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfRecordDefeat.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<PlayerProfileEntity> observePlayerProfile(final String id) {
    final String _sql = "SELECT * FROM player_profiles WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"player_profiles"}, new Callable<PlayerProfileEntity>() {
      @Override
      @Nullable
      public PlayerProfileEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHandle = CursorUtil.getColumnIndexOrThrow(_cursor, "handle");
          final int _cursorIndexOfAvatarIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "avatarIndex");
          final int _cursorIndexOfTotalMatches = CursorUtil.getColumnIndexOrThrow(_cursor, "totalMatches");
          final int _cursorIndexOfTotalWins = CursorUtil.getColumnIndexOrThrow(_cursor, "totalWins");
          final int _cursorIndexOfCurrentStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStreak");
          final int _cursorIndexOfBestStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "bestStreak");
          final PlayerProfileEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpHandle;
            _tmpHandle = _cursor.getString(_cursorIndexOfHandle);
            final int _tmpAvatarIndex;
            _tmpAvatarIndex = _cursor.getInt(_cursorIndexOfAvatarIndex);
            final int _tmpTotalMatches;
            _tmpTotalMatches = _cursor.getInt(_cursorIndexOfTotalMatches);
            final int _tmpTotalWins;
            _tmpTotalWins = _cursor.getInt(_cursorIndexOfTotalWins);
            final int _tmpCurrentStreak;
            _tmpCurrentStreak = _cursor.getInt(_cursorIndexOfCurrentStreak);
            final int _tmpBestStreak;
            _tmpBestStreak = _cursor.getInt(_cursorIndexOfBestStreak);
            _result = new PlayerProfileEntity(_tmpId,_tmpHandle,_tmpAvatarIndex,_tmpTotalMatches,_tmpTotalWins,_tmpCurrentStreak,_tmpBestStreak);
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
  public Object getPlayerProfile(final String id,
      final Continuation<? super PlayerProfileEntity> $completion) {
    final String _sql = "SELECT * FROM player_profiles WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PlayerProfileEntity>() {
      @Override
      @Nullable
      public PlayerProfileEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfHandle = CursorUtil.getColumnIndexOrThrow(_cursor, "handle");
          final int _cursorIndexOfAvatarIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "avatarIndex");
          final int _cursorIndexOfTotalMatches = CursorUtil.getColumnIndexOrThrow(_cursor, "totalMatches");
          final int _cursorIndexOfTotalWins = CursorUtil.getColumnIndexOrThrow(_cursor, "totalWins");
          final int _cursorIndexOfCurrentStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStreak");
          final int _cursorIndexOfBestStreak = CursorUtil.getColumnIndexOrThrow(_cursor, "bestStreak");
          final PlayerProfileEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpHandle;
            _tmpHandle = _cursor.getString(_cursorIndexOfHandle);
            final int _tmpAvatarIndex;
            _tmpAvatarIndex = _cursor.getInt(_cursorIndexOfAvatarIndex);
            final int _tmpTotalMatches;
            _tmpTotalMatches = _cursor.getInt(_cursorIndexOfTotalMatches);
            final int _tmpTotalWins;
            _tmpTotalWins = _cursor.getInt(_cursorIndexOfTotalWins);
            final int _tmpCurrentStreak;
            _tmpCurrentStreak = _cursor.getInt(_cursorIndexOfCurrentStreak);
            final int _tmpBestStreak;
            _tmpBestStreak = _cursor.getInt(_cursorIndexOfBestStreak);
            _result = new PlayerProfileEntity(_tmpId,_tmpHandle,_tmpAvatarIndex,_tmpTotalMatches,_tmpTotalWins,_tmpCurrentStreak,_tmpBestStreak);
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
