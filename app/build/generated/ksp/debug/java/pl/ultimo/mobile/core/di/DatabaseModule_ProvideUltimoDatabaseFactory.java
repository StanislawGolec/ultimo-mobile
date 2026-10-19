package pl.ultimo.mobile.core.di;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import pl.ultimo.mobile.core.database.UltimoDatabase;

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
public final class DatabaseModule_ProvideUltimoDatabaseFactory implements Factory<UltimoDatabase> {
  private final Provider<Context> contextProvider;

  public DatabaseModule_ProvideUltimoDatabaseFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public UltimoDatabase get() {
    return provideUltimoDatabase(contextProvider.get());
  }

  public static DatabaseModule_ProvideUltimoDatabaseFactory create(
      Provider<Context> contextProvider) {
    return new DatabaseModule_ProvideUltimoDatabaseFactory(contextProvider);
  }

  public static UltimoDatabase provideUltimoDatabase(Context context) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideUltimoDatabase(context));
  }
}
