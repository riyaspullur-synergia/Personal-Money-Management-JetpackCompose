package app.riyaspullur.personalmoneymanagement.core.export

import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionImporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ExportModule {
    @Binds
    abstract fun bindTransactionImporter(impl: CsvImporter): TransactionImporter
}
