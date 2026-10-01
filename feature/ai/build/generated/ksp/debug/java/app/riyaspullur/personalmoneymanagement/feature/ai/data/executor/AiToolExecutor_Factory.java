package app.riyaspullur.personalmoneymanagement.feature.ai.data.executor;

import app.riyaspullur.personalmoneymanagement.core.domain.repository.AccountRepository;
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AuthRepository;
import app.riyaspullur.personalmoneymanagement.core.domain.repository.BudgetRepository;
import app.riyaspullur.personalmoneymanagement.core.domain.repository.CategoryRepository;
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository;
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.AddTransactionUseCase;
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
public final class AiToolExecutor_Factory implements Factory<AiToolExecutor> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<AccountRepository> accountRepositoryProvider;

  private final Provider<TransactionRepository> transactionRepositoryProvider;

  private final Provider<CategoryRepository> categoryRepositoryProvider;

  private final Provider<BudgetRepository> budgetRepositoryProvider;

  private final Provider<AddTransactionUseCase> addTransactionUseCaseProvider;

  private AiToolExecutor_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<AccountRepository> accountRepositoryProvider,
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<BudgetRepository> budgetRepositoryProvider,
      Provider<AddTransactionUseCase> addTransactionUseCaseProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.accountRepositoryProvider = accountRepositoryProvider;
    this.transactionRepositoryProvider = transactionRepositoryProvider;
    this.categoryRepositoryProvider = categoryRepositoryProvider;
    this.budgetRepositoryProvider = budgetRepositoryProvider;
    this.addTransactionUseCaseProvider = addTransactionUseCaseProvider;
  }

  @Override
  public AiToolExecutor get() {
    return newInstance(authRepositoryProvider.get(), accountRepositoryProvider.get(), transactionRepositoryProvider.get(), categoryRepositoryProvider.get(), budgetRepositoryProvider.get(), addTransactionUseCaseProvider.get());
  }

  public static AiToolExecutor_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<AccountRepository> accountRepositoryProvider,
      Provider<TransactionRepository> transactionRepositoryProvider,
      Provider<CategoryRepository> categoryRepositoryProvider,
      Provider<BudgetRepository> budgetRepositoryProvider,
      Provider<AddTransactionUseCase> addTransactionUseCaseProvider) {
    return new AiToolExecutor_Factory(authRepositoryProvider, accountRepositoryProvider, transactionRepositoryProvider, categoryRepositoryProvider, budgetRepositoryProvider, addTransactionUseCaseProvider);
  }

  public static AiToolExecutor newInstance(AuthRepository authRepository,
      AccountRepository accountRepository, TransactionRepository transactionRepository,
      CategoryRepository categoryRepository, BudgetRepository budgetRepository,
      AddTransactionUseCase addTransactionUseCase) {
    return new AiToolExecutor(authRepository, accountRepository, transactionRepository, categoryRepository, budgetRepository, addTransactionUseCase);
  }
}
