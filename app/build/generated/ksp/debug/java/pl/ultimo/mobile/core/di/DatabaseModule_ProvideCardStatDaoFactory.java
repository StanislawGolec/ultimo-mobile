package pl.ultimo.mobile.core.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import pl.ultimo.mobile.core.database.UltimoDatabase;
import pl.ultimo.mobile.core.database.dao.CardStatDao;

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
public final class DatabaseModule_ProvideCardStatDaoFactory implements Factory<CardStatDao> {
  private final Provider<UltimoDatabase> databaseProvider;

  public DatabaseModule_ProvideCardStatDaoFactory(Provider<UltimoDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public CardStatDao get() {
    return provideCardStatDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideCardStatDaoFactory create(
      Provider<UltimoDatabase> databaseProvider) {
    return new DatabaseModule_ProvideCardStatDaoFactory(databaseProvider);
  }

  public static CardStatDao provideCardStatDao(UltimoDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideCardStatDao(database));
  }
}
