package pl.ultimo.mobile.core.database;

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
import pl.ultimo.mobile.core.database.dao.CardStatDao;
import pl.ultimo.mobile.core.database.dao.CardStatDao_Impl;
import pl.ultimo.mobile.core.database.dao.MatchRecordDao;
import pl.ultimo.mobile.core.database.dao.MatchRecordDao_Impl;
import pl.ultimo.mobile.core.database.dao.PlayerDao;
import pl.ultimo.mobile.core.database.dao.PlayerDao_Impl;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UltimoDatabase_Impl extends UltimoDatabase {
  private volatile PlayerDao _playerDao;

  private volatile MatchRecordDao _matchRecordDao;

  private volatile CardStatDao _cardStatDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `player_profiles` (`id` TEXT NOT NULL, `handle` TEXT NOT NULL, `avatarIndex` INTEGER NOT NULL, `totalMatches` INTEGER NOT NULL, `totalWins` INTEGER NOT NULL, `currentStreak` INTEGER NOT NULL, `bestStreak` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `match_records` (`id` TEXT NOT NULL, `timestampEpochMs` INTEGER NOT NULL, `roomId` TEXT NOT NULL, `opponentCount` INTEGER NOT NULL, `result` TEXT NOT NULL, `cardsPlayed` INTEGER NOT NULL, `matchDurationSeconds` INTEGER NOT NULL, `finalScore` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `card_stats` (`cardToken` TEXT NOT NULL, `timesPlayed` INTEGER NOT NULL, `timesDrawn` INTEGER NOT NULL, PRIMARY KEY(`cardToken`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'ada448b564ea06cb1675f0f29dc1baa0')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `player_profiles`");
        db.execSQL("DROP TABLE IF EXISTS `match_records`");
        db.execSQL("DROP TABLE IF EXISTS `card_stats`");
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
        final HashMap<String, TableInfo.Column> _columnsPlayerProfiles = new HashMap<String, TableInfo.Column>(7);
        _columnsPlayerProfiles.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerProfiles.put("handle", new TableInfo.Column("handle", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerProfiles.put("avatarIndex", new TableInfo.Column("avatarIndex", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerProfiles.put("totalMatches", new TableInfo.Column("totalMatches", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerProfiles.put("totalWins", new TableInfo.Column("totalWins", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerProfiles.put("currentStreak", new TableInfo.Column("currentStreak", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerProfiles.put("bestStreak", new TableInfo.Column("bestStreak", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPlayerProfiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPlayerProfiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPlayerProfiles = new TableInfo("player_profiles", _columnsPlayerProfiles, _foreignKeysPlayerProfiles, _indicesPlayerProfiles);
        final TableInfo _existingPlayerProfiles = TableInfo.read(db, "player_profiles");
        if (!_infoPlayerProfiles.equals(_existingPlayerProfiles)) {
          return new RoomOpenHelper.ValidationResult(false, "player_profiles(pl.ultimo.mobile.core.database.entity.PlayerProfileEntity).\n"
                  + " Expected:\n" + _infoPlayerProfiles + "\n"
                  + " Found:\n" + _existingPlayerProfiles);
        }
        final HashMap<String, TableInfo.Column> _columnsMatchRecords = new HashMap<String, TableInfo.Column>(8);
        _columnsMatchRecords.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatchRecords.put("timestampEpochMs", new TableInfo.Column("timestampEpochMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatchRecords.put("roomId", new TableInfo.Column("roomId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatchRecords.put("opponentCount", new TableInfo.Column("opponentCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatchRecords.put("result", new TableInfo.Column("result", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatchRecords.put("cardsPlayed", new TableInfo.Column("cardsPlayed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatchRecords.put("matchDurationSeconds", new TableInfo.Column("matchDurationSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatchRecords.put("finalScore", new TableInfo.Column("finalScore", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMatchRecords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMatchRecords = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMatchRecords = new TableInfo("match_records", _columnsMatchRecords, _foreignKeysMatchRecords, _indicesMatchRecords);
        final TableInfo _existingMatchRecords = TableInfo.read(db, "match_records");
        if (!_infoMatchRecords.equals(_existingMatchRecords)) {
          return new RoomOpenHelper.ValidationResult(false, "match_records(pl.ultimo.mobile.core.database.entity.MatchRecordEntity).\n"
                  + " Expected:\n" + _infoMatchRecords + "\n"
                  + " Found:\n" + _existingMatchRecords);
        }
        final HashMap<String, TableInfo.Column> _columnsCardStats = new HashMap<String, TableInfo.Column>(3);
        _columnsCardStats.put("cardToken", new TableInfo.Column("cardToken", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCardStats.put("timesPlayed", new TableInfo.Column("timesPlayed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCardStats.put("timesDrawn", new TableInfo.Column("timesDrawn", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCardStats = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCardStats = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCardStats = new TableInfo("card_stats", _columnsCardStats, _foreignKeysCardStats, _indicesCardStats);
        final TableInfo _existingCardStats = TableInfo.read(db, "card_stats");
        if (!_infoCardStats.equals(_existingCardStats)) {
          return new RoomOpenHelper.ValidationResult(false, "card_stats(pl.ultimo.mobile.core.database.entity.CardStatEntity).\n"
                  + " Expected:\n" + _infoCardStats + "\n"
                  + " Found:\n" + _existingCardStats);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "ada448b564ea06cb1675f0f29dc1baa0", "570890adde1270477359db624c1d6ab8");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "player_profiles","match_records","card_stats");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `player_profiles`");
      _db.execSQL("DELETE FROM `match_records`");
      _db.execSQL("DELETE FROM `card_stats`");
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
    _typeConvertersMap.put(PlayerDao.class, PlayerDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MatchRecordDao.class, MatchRecordDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CardStatDao.class, CardStatDao_Impl.getRequiredConverters());
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
  public PlayerDao playerDao() {
    if (_playerDao != null) {
      return _playerDao;
    } else {
      synchronized(this) {
        if(_playerDao == null) {
          _playerDao = new PlayerDao_Impl(this);
        }
        return _playerDao;
      }
    }
  }

  @Override
  public MatchRecordDao matchRecordDao() {
    if (_matchRecordDao != null) {
      return _matchRecordDao;
    } else {
      synchronized(this) {
        if(_matchRecordDao == null) {
          _matchRecordDao = new MatchRecordDao_Impl(this);
        }
        return _matchRecordDao;
      }
    }
  }

  @Override
  public CardStatDao cardStatDao() {
    if (_cardStatDao != null) {
      return _cardStatDao;
    } else {
      synchronized(this) {
        if(_cardStatDao == null) {
          _cardStatDao = new CardStatDao_Impl(this);
        }
        return _cardStatDao;
      }
    }
  }
}
