package pl.ultimo.mobile.core.haptic;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import pl.ultimo.mobile.core.datastore.UserPreferencesRepository;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class HapticFeedbackManager_Factory implements Factory<HapticFeedbackManager> {
  private final Provider<Context> contextProvider;

  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  public HapticFeedbackManager_Factory(Provider<Context> contextProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
  }

  @Override
  public HapticFeedbackManager get() {
    return newInstance(contextProvider.get(), preferencesRepositoryProvider.get());
  }

  public static HapticFeedbackManager_Factory create(Provider<Context> contextProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    return new HapticFeedbackManager_Factory(contextProvider, preferencesRepositoryProvider);
  }

  public static HapticFeedbackManager newInstance(Context context,
      UserPreferencesRepository preferencesRepository) {
    return new HapticFeedbackManager(context, preferencesRepository);
  }
}
