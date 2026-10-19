package pl.ultimo.mobile.core.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import kotlinx.coroutines.CoroutineDispatcher;
import kotlinx.coroutines.CoroutineScope;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata({
    "pl.ultimo.mobile.core.di.ApplicationScope",
    "pl.ultimo.mobile.core.di.DefaultDispatcher"
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
public final class CoroutinesModule_ProvideApplicationScopeFactory implements Factory<CoroutineScope> {
  private final Provider<CoroutineDispatcher> defaultDispatcherProvider;

  public CoroutinesModule_ProvideApplicationScopeFactory(
      Provider<CoroutineDispatcher> defaultDispatcherProvider) {
    this.defaultDispatcherProvider = defaultDispatcherProvider;
  }

  @Override
  public CoroutineScope get() {
    return provideApplicationScope(defaultDispatcherProvider.get());
  }

  public static CoroutinesModule_ProvideApplicationScopeFactory create(
      Provider<CoroutineDispatcher> defaultDispatcherProvider) {
    return new CoroutinesModule_ProvideApplicationScopeFactory(defaultDispatcherProvider);
  }

  public static CoroutineScope provideApplicationScope(CoroutineDispatcher defaultDispatcher) {
    return Preconditions.checkNotNullFromProvides(CoroutinesModule.INSTANCE.provideApplicationScope(defaultDispatcher));
  }
}
