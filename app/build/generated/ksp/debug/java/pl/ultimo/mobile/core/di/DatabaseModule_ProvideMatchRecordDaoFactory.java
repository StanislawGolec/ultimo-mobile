package pl.ultimo.mobile.core.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import pl.ultimo.mobile.core.database.UltimoDatabase;
import pl.ultimo.mobile.core.database.dao.MatchRecordDao;

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
public final class DatabaseModule_ProvideMatchRecordDaoFactory implements Factory<MatchRecordDao> {
  private final Provider<UltimoDatabase> databaseProvider;

  public DatabaseModule_ProvideMatchRecordDaoFactory(Provider<UltimoDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public MatchRecordDao get() {
    return provideMatchRecordDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideMatchRecordDaoFactory create(
      Provider<UltimoDatabase> databaseProvider) {
    return new DatabaseModule_ProvideMatchRecordDaoFactory(databaseProvider);
  }

  public static MatchRecordDao provideMatchRecordDao(UltimoDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideMatchRecordDao(database));
  }
}
