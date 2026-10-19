package pl.ultimo.mobile.domain.usecase;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import pl.ultimo.mobile.core.datastore.UserPreferencesRepository;
import pl.ultimo.mobile.core.network.WebSocketEngine;

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
public final class ConnectToGameUseCase_Factory implements Factory<ConnectToGameUseCase> {
  private final Provider<WebSocketEngine> webSocketEngineProvider;

  private final Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

  public ConnectToGameUseCase_Factory(Provider<WebSocketEngine> webSocketEngineProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    this.webSocketEngineProvider = webSocketEngineProvider;
    this.userPreferencesRepositoryProvider = userPreferencesRepositoryProvider;
  }

  @Override
  public ConnectToGameUseCase get() {
    return newInstance(webSocketEngineProvider.get(), userPreferencesRepositoryProvider.get());
  }

  public static ConnectToGameUseCase_Factory create(
      Provider<WebSocketEngine> webSocketEngineProvider,
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    return new ConnectToGameUseCase_Factory(webSocketEngineProvider, userPreferencesRepositoryProvider);
  }

  public static ConnectToGameUseCase newInstance(WebSocketEngine webSocketEngine,
      UserPreferencesRepository userPreferencesRepository) {
    return new ConnectToGameUseCase(webSocketEngine, userPreferencesRepository);
  }
}
