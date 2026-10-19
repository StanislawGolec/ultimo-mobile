package pl.ultimo.mobile.core.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineDispatcher;

@ScopeMetadata
@QualifierMetadata("pl.ultimo.mobile.core.di.DefaultDispatcher")
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
public final class CoroutinesModule_ProvideDefaultDispatcherFactory implements Factory<CoroutineDispatcher> {
  @Override
  public CoroutineDispatcher get() {
    return provideDefaultDispatcher();
  }

  public static CoroutinesModule_ProvideDefaultDispatcherFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CoroutineDispatcher provideDefaultDispatcher() {
    return Preconditions.checkNotNullFromProvides(CoroutinesModule.INSTANCE.provideDefaultDispatcher());
  }

  private static final class InstanceHolder {
    private static final CoroutinesModule_ProvideDefaultDispatcherFactory INSTANCE = new CoroutinesModule_ProvideDefaultDispatcherFactory();
  }
}
