package pl.ultimo.mobile.feature.game;

import androidx.lifecycle.SavedStateHandle;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import pl.ultimo.mobile.core.database.dao.CardStatDao;
import pl.ultimo.mobile.core.datastore.UserPreferencesRepository;
import pl.ultimo.mobile.core.network.WebSocketEngine;
import pl.ultimo.mobile.domain.usecase.DeclareUltimoUseCase;
import pl.ultimo.mobile.domain.usecase.DrawCardUseCase;
import pl.ultimo.mobile.domain.usecase.PassTurnUseCase;
import pl.ultimo.mobile.domain.usecase.PlayCardUseCase;
import pl.ultimo.mobile.domain.usecase.RecordMatchOutcomeUseCase;

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
public final class GameViewModel_Factory implements Factory<GameViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<WebSocketEngine> webSocketEngineProvider;

  private final Provider<PlayCardUseCase> playCardUseCaseProvider;

  private final Provider<DrawCardUseCase> drawCardUseCaseProvider;

  private final Provider<PassTurnUseCase> passTurnUseCaseProvider;

  private final Provider<DeclareUltimoUseCase> declareUltimoUseCaseProvider;

  private final Provider<RecordMatchOutcomeUseCase> recordMatchOutcomeUseCaseProvider;

  private final Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

  private final Provider<CardStatDao> cardStatDaoProvider;

  public GameViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<WebSocketEngine> webSocketEngineProvider,
      Provider<PlayCardUseCase> playCardUseCaseProvider,
      Provider<DrawCardUseCase> drawCardUseCaseProvider,
      Provider<PassTurnUseCase> passTurnUseCaseProvider,
      Provider<DeclareUltimoUseCase> declareUltimoUseCaseProvider,
      Provider<RecordMatchOutcomeUseCase> recordMatchOutcomeUseCaseProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider,
      Provider<CardStatDao> cardStatDaoProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.webSocketEngineProvider = webSocketEngineProvider;
    this.playCardUseCaseProvider = playCardUseCaseProvider;
    this.drawCardUseCaseProvider = drawCardUseCaseProvider;
    this.passTurnUseCaseProvider = passTurnUseCaseProvider;
    this.declareUltimoUseCaseProvider = declareUltimoUseCaseProvider;
    this.recordMatchOutcomeUseCaseProvider = recordMatchOutcomeUseCaseProvider;
    this.userPreferencesRepositoryProvider = userPreferencesRepositoryProvider;
    this.cardStatDaoProvider = cardStatDaoProvider;
  }

  @Override
  public GameViewModel get() {
    return newInstance(savedStateHandleProvider.get(), webSocketEngineProvider.get(), playCardUseCaseProvider.get(), drawCardUseCaseProvider.get(), passTurnUseCaseProvider.get(), declareUltimoUseCaseProvider.get(), recordMatchOutcomeUseCaseProvider.get(), userPreferencesRepositoryProvider.get(), cardStatDaoProvider.get());
  }

  public static GameViewModel_Factory create(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<WebSocketEngine> webSocketEngineProvider,
      Provider<PlayCardUseCase> playCardUseCaseProvider,
      Provider<DrawCardUseCase> drawCardUseCaseProvider,
      Provider<PassTurnUseCase> passTurnUseCaseProvider,
      Provider<DeclareUltimoUseCase> declareUltimoUseCaseProvider,
      Provider<RecordMatchOutcomeUseCase> recordMatchOutcomeUseCaseProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider,
      Provider<CardStatDao> cardStatDaoProvider) {
    return new GameViewModel_Factory(savedStateHandleProvider, webSocketEngineProvider, playCardUseCaseProvider, drawCardUseCaseProvider, passTurnUseCaseProvider, declareUltimoUseCaseProvider, recordMatchOutcomeUseCaseProvider, userPreferencesRepositoryProvider, cardStatDaoProvider);
  }

  public static GameViewModel newInstance(SavedStateHandle savedStateHandle,
      WebSocketEngine webSocketEngine, PlayCardUseCase playCardUseCase,
      DrawCardUseCase drawCardUseCase, PassTurnUseCase passTurnUseCase,
      DeclareUltimoUseCase declareUltimoUseCase,
      RecordMatchOutcomeUseCase recordMatchOutcomeUseCase,
      UserPreferencesRepository userPreferencesRepository, CardStatDao cardStatDao) {
    return new GameViewModel(savedStateHandle, webSocketEngine, playCardUseCase, drawCardUseCase, passTurnUseCase, declareUltimoUseCase, recordMatchOutcomeUseCase, userPreferencesRepository, cardStatDao);
  }
}
