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
public final class PassTurnUseCase_Factory implements Factory<PassTurnUseCase> {
  private final Provider<WebSocketEngine> webSocketEngineProvider;

  public PassTurnUseCase_Factory(Provider<WebSocketEngine> webSocketEngineProvider) {
    this.webSocketEngineProvider = webSocketEngineProvider;
  }

  @Override
  public PassTurnUseCase get() {
    return newInstance(webSocketEngineProvider.get());
  }

  public static PassTurnUseCase_Factory create(Provider<WebSocketEngine> webSocketEngineProvider) {
    return new PassTurnUseCase_Factory(webSocketEngineProvider);
  }

  public static PassTurnUseCase newInstance(WebSocketEngine webSocketEngine) {
    return new PassTurnUseCase(webSocketEngine);
  }
}
