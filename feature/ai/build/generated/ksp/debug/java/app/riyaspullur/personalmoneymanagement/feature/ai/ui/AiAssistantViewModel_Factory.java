package app.riyaspullur.personalmoneymanagement.feature.ai.ui;

import app.riyaspullur.personalmoneymanagement.feature.ai.domain.usecase.AiAssistantUseCase;
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
public final class AiAssistantViewModel_Factory implements Factory<AiAssistantViewModel> {
  private final Provider<AiAssistantUseCase> aiAssistantUseCaseProvider;

  private AiAssistantViewModel_Factory(Provider<AiAssistantUseCase> aiAssistantUseCaseProvider) {
    this.aiAssistantUseCaseProvider = aiAssistantUseCaseProvider;
  }

  @Override
  public AiAssistantViewModel get() {
    return newInstance(aiAssistantUseCaseProvider.get());
  }

  public static AiAssistantViewModel_Factory create(
      Provider<AiAssistantUseCase> aiAssistantUseCaseProvider) {
    return new AiAssistantViewModel_Factory(aiAssistantUseCaseProvider);
  }

  public static AiAssistantViewModel newInstance(AiAssistantUseCase aiAssistantUseCase) {
    return new AiAssistantViewModel(aiAssistantUseCase);
  }
}
