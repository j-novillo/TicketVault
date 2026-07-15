package com.tickettrack.ui.capture;

import com.tickettrack.camera.DocumentScannerGateway;
import com.tickettrack.domain.usecase.ProcessScannedImageUseCase;
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
public final class CaptureViewModel_Factory implements Factory<CaptureViewModel> {
  private final Provider<DocumentScannerGateway> documentScannerGatewayProvider;

  private final Provider<ProcessScannedImageUseCase> processScannedImageUseCaseProvider;

  public CaptureViewModel_Factory(Provider<DocumentScannerGateway> documentScannerGatewayProvider,
      Provider<ProcessScannedImageUseCase> processScannedImageUseCaseProvider) {
    this.documentScannerGatewayProvider = documentScannerGatewayProvider;
    this.processScannedImageUseCaseProvider = processScannedImageUseCaseProvider;
  }

  @Override
  public CaptureViewModel get() {
    return newInstance(documentScannerGatewayProvider.get(), processScannedImageUseCaseProvider.get());
  }

  public static CaptureViewModel_Factory create(
      Provider<DocumentScannerGateway> documentScannerGatewayProvider,
      Provider<ProcessScannedImageUseCase> processScannedImageUseCaseProvider) {
    return new CaptureViewModel_Factory(documentScannerGatewayProvider, processScannedImageUseCaseProvider);
  }

  public static CaptureViewModel newInstance(DocumentScannerGateway documentScannerGateway,
      ProcessScannedImageUseCase processScannedImageUseCase) {
    return new CaptureViewModel(documentScannerGateway, processScannedImageUseCase);
  }
}
