package app.riyaspullur.personalmoneymanagement.feature.ai.domain.usecase;

import app.riyaspullur.personalmoneymanagement.feature.ai.domain.repository.AiAssistantRepository;
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
public final class AiAssistantUseCase_Factory implements Factory<AiAssistantUseCase> {
  private final Provider<AiAssistantRepository> repositoryProvider;

  private AiAssistantUseCase_Factory(Provider<AiAssistantRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public AiAssistantUseCase get() {
    return newInstance(repositoryProvider.get());
  }

  public static AiAssistantUseCase_Factory create(
      Provider<AiAssistantRepository> repositoryProvider) {
    return new AiAssistantUseCase_Factory(repositoryProvider);
  }

  public static AiAssistantUseCase newInstance(AiAssistantRepository repository) {
    return new AiAssistantUseCase(repository);
  }
}
