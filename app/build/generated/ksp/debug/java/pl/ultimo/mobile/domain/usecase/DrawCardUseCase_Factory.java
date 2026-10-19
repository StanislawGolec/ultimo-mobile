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
public final class DrawCardUseCase_Factory implements Factory<DrawCardUseCase> {
  private final Provider<WebSocketEngine> webSocketEngineProvider;

  public DrawCardUseCase_Factory(Provider<WebSocketEngine> webSocketEngineProvider) {
    this.webSocketEngineProvider = webSocketEngineProvider;
  }

  @Override
  public DrawCardUseCase get() {
    return newInstance(webSocketEngineProvider.get());
  }

  public static DrawCardUseCase_Factory create(Provider<WebSocketEngine> webSocketEngineProvider) {
    return new DrawCardUseCase_Factory(webSocketEngineProvider);
  }

  public static DrawCardUseCase newInstance(WebSocketEngine webSocketEngine) {
    return new DrawCardUseCase(webSocketEngine);
  }
}
