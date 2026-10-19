package pl.ultimo.mobile.feature.lobby;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import pl.ultimo.mobile.core.datastore.UserPreferencesRepository;
import pl.ultimo.mobile.core.network.WebSocketEngine;
import pl.ultimo.mobile.domain.usecase.ConnectToGameUseCase;

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
public final class LobbyViewModel_Factory implements Factory<LobbyViewModel> {
  private final Provider<ConnectToGameUseCase> connectToGameUseCaseProvider;

  private final Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

  private final Provider<WebSocketEngine> webSocketEngineProvider;

  public LobbyViewModel_Factory(Provider<ConnectToGameUseCase> connectToGameUseCaseProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider,
      Provider<WebSocketEngine> webSocketEngineProvider) {
    this.connectToGameUseCaseProvider = connectToGameUseCaseProvider;
    this.userPreferencesRepositoryProvider = userPreferencesRepositoryProvider;
    this.webSocketEngineProvider = webSocketEngineProvider;
  }

  @Override
  public LobbyViewModel get() {
    return newInstance(connectToGameUseCaseProvider.get(), userPreferencesRepositoryProvider.get(), webSocketEngineProvider.get());
  }

  public static LobbyViewModel_Factory create(
      Provider<ConnectToGameUseCase> connectToGameUseCaseProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider,
      Provider<WebSocketEngine> webSocketEngineProvider) {
    return new LobbyViewModel_Factory(connectToGameUseCaseProvider, userPreferencesRepositoryProvider, webSocketEngineProvider);
  }

  public static LobbyViewModel newInstance(ConnectToGameUseCase connectToGameUseCase,
      UserPreferencesRepository userPreferencesRepository, WebSocketEngine webSocketEngine) {
    return new LobbyViewModel(connectToGameUseCase, userPreferencesRepository, webSocketEngine);
  }
}
