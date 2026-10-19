package pl.ultimo.mobile.feature.history;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import pl.ultimo.mobile.core.database.dao.MatchRecordDao;
import pl.ultimo.mobile.core.database.dao.PlayerDao;

@ScopeMetadata
@QualifierMetadata
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
    "deprecation"
})
public final class HistoryViewModel_Factory implements Factory<HistoryViewModel> {
  private final Provider<PlayerDao> playerDaoProvider;

  private final Provider<MatchRecordDao> matchRecordDaoProvider;

  public HistoryViewModel_Factory(Provider<PlayerDao> playerDaoProvider,
      Provider<MatchRecordDao> matchRecordDaoProvider) {
    this.playerDaoProvider = playerDaoProvider;
    this.matchRecordDaoProvider = matchRecordDaoProvider;
  }

  @Override
  public HistoryViewModel get() {
    return newInstance(playerDaoProvider.get(), matchRecordDaoProvider.get());
  }

  public static HistoryViewModel_Factory create(Provider<PlayerDao> playerDaoProvider,
      Provider<MatchRecordDao> matchRecordDaoProvider) {
    return new HistoryViewModel_Factory(playerDaoProvider, matchRecordDaoProvider);
  }

  public static HistoryViewModel newInstance(PlayerDao playerDao, MatchRecordDao matchRecordDao) {
    return new HistoryViewModel(playerDao, matchRecordDao);
  }
}
