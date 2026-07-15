package com.tickettrack.domain.usecase;

import com.tickettrack.data.repository.TicketRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class GetTicketsUseCase_Factory implements Factory<GetTicketsUseCase> {
  private final Provider<TicketRepository> ticketRepositoryProvider;

  public GetTicketsUseCase_Factory(Provider<TicketRepository> ticketRepositoryProvider) {
    this.ticketRepositoryProvider = ticketRepositoryProvider;
  }

  @Override
  public GetTicketsUseCase get() {
    return newInstance(ticketRepositoryProvider.get());
  }

  public static GetTicketsUseCase_Factory create(
      Provider<TicketRepository> ticketRepositoryProvider) {
    return new GetTicketsUseCase_Factory(ticketRepositoryProvider);
  }

  public static GetTicketsUseCase newInstance(TicketRepository ticketRepository) {
    return new GetTicketsUseCase(ticketRepository);
  }
}
