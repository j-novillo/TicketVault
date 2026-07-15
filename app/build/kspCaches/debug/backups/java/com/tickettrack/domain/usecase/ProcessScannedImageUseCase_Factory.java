package com.tickettrack.domain.usecase;

import com.tickettrack.camera.DocumentScannerGateway;
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
public final class ProcessScannedImageUseCase_Factory implements Factory<ProcessScannedImageUseCase> {
  private final Provider<DocumentScannerGateway> documentScannerGatewayProvider;

  public ProcessScannedImageUseCase_Factory(
      Provider<DocumentScannerGateway> documentScannerGatewayProvider) {
    this.documentScannerGatewayProvider = documentScannerGatewayProvider;
  }

  @Override
  public ProcessScannedImageUseCase get() {
    return newInstance(documentScannerGatewayProvider.get());
  }

  public static ProcessScannedImageUseCase_Factory create(
      Provider<DocumentScannerGateway> documentScannerGatewayProvider) {
    return new ProcessScannedImageUseCase_Factory(documentScannerGatewayProvider);
  }

  public static ProcessScannedImageUseCase newInstance(
      DocumentScannerGateway documentScannerGateway) {
    return new ProcessScannedImageUseCase(documentScannerGateway);
  }
}
