package pl.ultimo.mobile;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import pl.ultimo.mobile.core.audio.SoundEffectsManager;
import pl.ultimo.mobile.core.haptic.HapticFeedbackManager;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<SoundEffectsManager> soundManagerProvider;

  private final Provider<HapticFeedbackManager> hapticManagerProvider;

  public MainActivity_MembersInjector(Provider<SoundEffectsManager> soundManagerProvider,
      Provider<HapticFeedbackManager> hapticManagerProvider) {
    this.soundManagerProvider = soundManagerProvider;
    this.hapticManagerProvider = hapticManagerProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<SoundEffectsManager> soundManagerProvider,
      Provider<HapticFeedbackManager> hapticManagerProvider) {
    return new MainActivity_MembersInjector(soundManagerProvider, hapticManagerProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectSoundManager(instance, soundManagerProvider.get());
    injectHapticManager(instance, hapticManagerProvider.get());
  }

  @InjectedFieldSignature("pl.ultimo.mobile.MainActivity.soundManager")
  public static void injectSoundManager(MainActivity instance, SoundEffectsManager soundManager) {
    instance.soundManager = soundManager;
  }

  @InjectedFieldSignature("pl.ultimo.mobile.MainActivity.hapticManager")
  public static void injectHapticManager(MainActivity instance,
      HapticFeedbackManager hapticManager) {
    instance.hapticManager = hapticManager;
  }
}
