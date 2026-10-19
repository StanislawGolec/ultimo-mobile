package pl.ultimo.mobile.core.audio;

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
public final class SoundEffectsManager_Factory implements Factory<SoundEffectsManager> {
  private final Provider<Context> contextProvider;

  private final Provider<UserPreferencesRepository> preferencesRepositoryProvider;

  public SoundEffectsManager_Factory(Provider<Context> contextProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.preferencesRepositoryProvider = preferencesRepositoryProvider;
  }

  @Override
  public SoundEffectsManager get() {
    return newInstance(contextProvider.get(), preferencesRepositoryProvider.get());
  }

  public static SoundEffectsManager_Factory create(Provider<Context> contextProvider,
      Provider<UserPreferencesRepository> preferencesRepositoryProvider) {
    return new SoundEffectsManager_Factory(contextProvider, preferencesRepositoryProvider);
  }

  public static SoundEffectsManager newInstance(Context context,
      UserPreferencesRepository preferencesRepository) {
    return new SoundEffectsManager(context, preferencesRepository);
  }
}
