package com.tickettrack.camera;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class GmsDocumentScannerGateway_Factory implements Factory<GmsDocumentScannerGateway> {
  private final Provider<TicketImageStorage> ticketImageStorageProvider;

  public GmsDocumentScannerGateway_Factory(
      Provider<TicketImageStorage> ticketImageStorageProvider) {
    this.ticketImageStorageProvider = ticketImageStorageProvider;
  }

  @Override
  public GmsDocumentScannerGateway get() {
    return newInstance(ticketImageStorageProvider.get());
  }

  public static GmsDocumentScannerGateway_Factory create(
      Provider<TicketImageStorage> ticketImageStorageProvider) {
    return new GmsDocumentScannerGateway_Factory(ticketImageStorageProvider);
  }

  public static GmsDocumentScannerGateway newInstance(TicketImageStorage ticketImageStorage) {
    return new GmsDocumentScannerGateway(ticketImageStorage);
  }
}
