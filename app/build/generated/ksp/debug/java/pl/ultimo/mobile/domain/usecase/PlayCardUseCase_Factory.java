package pl.ultimo.mobile.domain.usecase;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
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
public final class PlayCardUseCase_Factory implements Factory<PlayCardUseCase> {
  private final Provider<WebSocketEngine> webSocketEngineProvider;

  public PlayCardUseCase_Factory(Provider<WebSocketEngine> webSocketEngineProvider) {
    this.webSocketEngineProvider = webSocketEngineProvider;
  }

  @Override
  public PlayCardUseCase get() {
    return newInstance(webSocketEngineProvider.get());
  }

  public static PlayCardUseCase_Factory create(Provider<WebSocketEngine> webSocketEngineProvider) {
    return new PlayCardUseCase_Factory(webSocketEngineProvider);
  }

  public static PlayCardUseCase newInstance(WebSocketEngine webSocketEngine) {
    return new PlayCardUseCase(webSocketEngine);
  }
}
