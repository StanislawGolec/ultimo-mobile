package pl.ultimo.mobile.core.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import pl.ultimo.mobile.core.database.UltimoDatabase;
import pl.ultimo.mobile.core.database.dao.PlayerDao;

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
public final class DatabaseModule_ProvidePlayerDaoFactory implements Factory<PlayerDao> {
  private final Provider<UltimoDatabase> databaseProvider;

  public DatabaseModule_ProvidePlayerDaoFactory(Provider<UltimoDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public PlayerDao get() {
    return providePlayerDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvidePlayerDaoFactory create(
      Provider<UltimoDatabase> databaseProvider) {
    return new DatabaseModule_ProvidePlayerDaoFactory(databaseProvider);
  }

  public static PlayerDao providePlayerDao(UltimoDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePlayerDao(database));
  }
}
