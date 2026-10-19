package pl.ultimo.mobile.core.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import kotlinx.coroutines.CoroutineDispatcher;
import kotlinx.coroutines.CoroutineScope;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata({
    "pl.ultimo.mobile.core.di.ApplicationScope",
    "pl.ultimo.mobile.core.di.IoDispatcher"
})
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
public final class OkHttpWebSocketEngine_Factory implements Factory<OkHttpWebSocketEngine> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<NetworkConnectivityObserver> connectivityObserverProvider;

  private final Provider<CoroutineScope> externalScopeProvider;

  private final Provider<CoroutineDispatcher> ioDispatcherProvider;

  public OkHttpWebSocketEngine_Factory(Provider<OkHttpClient> okHttpClientProvider,
      Provider<NetworkConnectivityObserver> connectivityObserverProvider,
      Provider<CoroutineScope> externalScopeProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
    this.connectivityObserverProvider = connectivityObserverProvider;
    this.externalScopeProvider = externalScopeProvider;
    this.ioDispatcherProvider = ioDispatcherProvider;
  }

  @Override
  public OkHttpWebSocketEngine get() {
    return newInstance(okHttpClientProvider.get(), connectivityObserverProvider.get(), externalScopeProvider.get(), ioDispatcherProvider.get());
  }

  public static OkHttpWebSocketEngine_Factory create(Provider<OkHttpClient> okHttpClientProvider,
      Provider<NetworkConnectivityObserver> connectivityObserverProvider,
      Provider<CoroutineScope> externalScopeProvider,
      Provider<CoroutineDispatcher> ioDispatcherProvider) {
    return new OkHttpWebSocketEngine_Factory(okHttpClientProvider, connectivityObserverProvider, externalScopeProvider, ioDispatcherProvider);
  }

  public static OkHttpWebSocketEngine newInstance(OkHttpClient okHttpClient,
      NetworkConnectivityObserver connectivityObserver, CoroutineScope externalScope,
      CoroutineDispatcher ioDispatcher) {
    return new OkHttpWebSocketEngine(okHttpClient, connectivityObserver, externalScope, ioDispatcher);
  }
}
