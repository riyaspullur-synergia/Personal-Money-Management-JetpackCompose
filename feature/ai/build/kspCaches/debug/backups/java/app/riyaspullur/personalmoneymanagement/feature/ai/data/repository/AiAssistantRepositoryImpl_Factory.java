package app.riyaspullur.personalmoneymanagement.feature.ai.data.repository;

import app.riyaspullur.personalmoneymanagement.feature.ai.data.executor.AiToolExecutor;
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
public final class AiAssistantRepositoryImpl_Factory implements Factory<AiAssistantRepositoryImpl> {
  private final Provider<AiToolExecutor> toolExecutorProvider;

  private AiAssistantRepositoryImpl_Factory(Provider<AiToolExecutor> toolExecutorProvider) {
    this.toolExecutorProvider = toolExecutorProvider;
  }

  @Override
  public AiAssistantRepositoryImpl get() {
    return newInstance(toolExecutorProvider.get());
  }

  public static AiAssistantRepositoryImpl_Factory create(
      Provider<AiToolExecutor> toolExecutorProvider) {
    return new AiAssistantRepositoryImpl_Factory(toolExecutorProvider);
  }

  public static AiAssistantRepositoryImpl newInstance(AiToolExecutor toolExecutor) {
    return new AiAssistantRepositoryImpl(toolExecutor);
  }
}
