package pl.ultimo.mobile.domain.usecase;

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
public final class RecordMatchOutcomeUseCase_Factory implements Factory<RecordMatchOutcomeUseCase> {
  private final Provider<PlayerDao> playerDaoProvider;

  private final Provider<MatchRecordDao> matchRecordDaoProvider;

  public RecordMatchOutcomeUseCase_Factory(Provider<PlayerDao> playerDaoProvider,
      Provider<MatchRecordDao> matchRecordDaoProvider) {
    this.playerDaoProvider = playerDaoProvider;
    this.matchRecordDaoProvider = matchRecordDaoProvider;
  }

  @Override
  public RecordMatchOutcomeUseCase get() {
    return newInstance(playerDaoProvider.get(), matchRecordDaoProvider.get());
  }

  public static RecordMatchOutcomeUseCase_Factory create(Provider<PlayerDao> playerDaoProvider,
      Provider<MatchRecordDao> matchRecordDaoProvider) {
    return new RecordMatchOutcomeUseCase_Factory(playerDaoProvider, matchRecordDaoProvider);
  }

  public static RecordMatchOutcomeUseCase newInstance(PlayerDao playerDao,
      MatchRecordDao matchRecordDao) {
    return new RecordMatchOutcomeUseCase(playerDao, matchRecordDao);
  }
}
