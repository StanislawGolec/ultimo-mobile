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
public final class DeclareUltimoUseCase_Factory implements Factory<DeclareUltimoUseCase> {
  private final Provider<WebSocketEngine> webSocketEngineProvider;

  public DeclareUltimoUseCase_Factory(Provider<WebSocketEngine> webSocketEngineProvider) {
    this.webSocketEngineProvider = webSocketEngineProvider;
  }

  @Override
  public DeclareUltimoUseCase get() {
    return newInstance(webSocketEngineProvider.get());
  }

  public static DeclareUltimoUseCase_Factory create(
      Provider<WebSocketEngine> webSocketEngineProvider) {
    return new DeclareUltimoUseCase_Factory(webSocketEngineProvider);
  }

  public static DeclareUltimoUseCase newInstance(WebSocketEngine webSocketEngine) {
    return new DeclareUltimoUseCase(webSocketEngine);
  }
}
