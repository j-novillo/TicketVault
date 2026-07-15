package com.tickettrack.di;

import com.tickettrack.data.local.TicketTrackDatabase;
import com.tickettrack.data.local.dao.TicketDao;
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
public final class DatabaseModule_ProvideTicketDaoFactory implements Factory<TicketDao> {
  private final Provider<TicketTrackDatabase> databaseProvider;

  public DatabaseModule_ProvideTicketDaoFactory(Provider<TicketTrackDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public TicketDao get() {
    return provideTicketDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideTicketDaoFactory create(
      Provider<TicketTrackDatabase> databaseProvider) {
    return new DatabaseModule_ProvideTicketDaoFactory(databaseProvider);
  }

  public static TicketDao provideTicketDao(TicketTrackDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideTicketDao(database));
  }
}
