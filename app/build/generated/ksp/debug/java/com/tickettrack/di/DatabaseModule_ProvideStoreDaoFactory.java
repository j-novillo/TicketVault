package com.tickettrack.di;

import com.tickettrack.data.local.TicketTrackDatabase;
import com.tickettrack.data.local.dao.StoreDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class DatabaseModule_ProvideStoreDaoFactory implements Factory<StoreDao> {
  private final Provider<TicketTrackDatabase> databaseProvider;

  public DatabaseModule_ProvideStoreDaoFactory(Provider<TicketTrackDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public StoreDao get() {
    return provideStoreDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideStoreDaoFactory create(
      Provider<TicketTrackDatabase> databaseProvider) {
    return new DatabaseModule_ProvideStoreDaoFactory(databaseProvider);
  }

  public static StoreDao provideStoreDao(TicketTrackDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideStoreDao(database));
  }
}
