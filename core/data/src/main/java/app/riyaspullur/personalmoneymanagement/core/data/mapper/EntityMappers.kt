package app.riyaspullur.personalmoneymanagement.core.data.mapper

import app.riyaspullur.personalmoneymanagement.core.database.entity.AccountEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.AccountGroupEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.BudgetCategoryEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.BudgetEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.CategoryEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.SavingsGoalEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.TransactionEntity
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountGroup
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.BudgetCategoryLimit
import app.riyaspullur.personalmoneymanagement.core.domain.model.BudgetRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.Category
import app.riyaspullur.personalmoneymanagement.core.domain.model.SavingsGoal
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction

fun AccountEntity.toDomain() = AccountRecord(
    id = id,
    userId = userId,
    groupId = groupId,
    name = name,
    type = type,
    initialBalance = initialBalance,
    currency = currency,
    icon = icon,
    color = color,
    isArchived = isArchived,
    createdAt = createdAt,
    investedAmount = investedAmount,
    lastValuationDate = lastValuationDate,
    interestRate = interestRate,
    maturityDate = maturityDate,
    bankName = bankName,
    personName = personName,
    dueDate = dueDate,
    isReceivable = isReceivable
)

fun AccountRecord.toEntity() = AccountEntity(
    id = id,
    userId = userId,
    groupId = groupId,
    name = name,
    type = type,
    initialBalance = initialBalance,
    currency = currency,
    icon = icon,
    color = color,
    isArchived = isArchived,
    createdAt = createdAt,
    investedAmount = investedAmount,
    lastValuationDate = lastValuationDate,
    interestRate = interestRate,
    maturityDate = maturityDate,
    bankName = bankName,
    personName = personName,
    dueDate = dueDate,
    isReceivable = isReceivable
)

fun AccountGroupEntity.toDomain() = AccountGroup(
    id = id,
    userId = userId,
    name = name,
    sortOrder = sortOrder,
    isArchived = isArchived
)

fun AccountGroup.toEntity() = AccountGroupEntity(
    id = id,
    userId = userId,
    name = name,
    sortOrder = sortOrder,
    isArchived = isArchived
)

fun CategoryEntity.toDomain() = Category(
    id = id,
    userId = userId,
    name = name,
    icon = icon,
    color = color,
    type = type,
    parentCategoryId = parentCategoryId,
    isArchived = isArchived
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    userId = userId,
    name = name,
    icon = icon,
    color = color,
    type = type,
    parentCategoryId = parentCategoryId,
    isArchived = isArchived
)

fun SavingsGoalEntity.toDomain() = SavingsGoal(
    id = id,
    userId = userId,
    name = name,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    currency = currency,
    targetDate = targetDate,
    icon = icon,
    color = color,
    isCompleted = isCompleted,
    createdAt = createdAt
)

fun SavingsGoal.toEntity() = SavingsGoalEntity(
    id = id,
    userId = userId,
    name = name,
    targetAmount = targetAmount,
    currentAmount = currentAmount,
    currency = currency,
    targetDate = targetDate,
    icon = icon,
    color = color,
    isCompleted = isCompleted,
    createdAt = createdAt
)

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    userId = userId,
    accountId = accountId,
    toAccountId = toAccountId,
    amount = amount,
    currency = currency,
    categoryId = categoryId,
    type = type,
    merchant = merchant,
    description = description,
    notes = notes,
    transactionDate = transactionDate,
    createdAt = createdAt,
    updatedAt = updatedAt,
    paymentStatus = paymentStatus,
    receiptPath = receiptPath,
    isRecurring = isRecurring,
    isDeleted = isDeleted
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    userId = userId,
    accountId = accountId,
    toAccountId = toAccountId,
    amount = amount,
    currency = currency,
    categoryId = categoryId,
    type = type,
    merchant = merchant,
    description = description,
    notes = notes,
    transactionDate = transactionDate,
    createdAt = createdAt,
    updatedAt = updatedAt,
    paymentStatus = paymentStatus,
    receiptPath = receiptPath,
    isRecurring = isRecurring,
    isDeleted = isDeleted
)

fun BudgetEntity.toDomain() = BudgetRecord(
    id = id,
    userId = userId,
    name = name,
    totalLimit = totalLimit,
    currency = currency,
    startDate = startDate,
    endDate = endDate,
    alertThreshold = alertThreshold,
    isRolloverEnabled = isRolloverEnabled,
    createdAt = createdAt
)

fun BudgetRecord.toEntity() = BudgetEntity(
    id = id,
    userId = userId,
    name = name,
    totalLimit = totalLimit,
    currency = currency,
    startDate = startDate,
    endDate = endDate,
    alertThreshold = alertThreshold,
    isRolloverEnabled = isRolloverEnabled,
    createdAt = createdAt
)

fun BudgetCategoryEntity.toDomain() = BudgetCategoryLimit(
    id = id,
    budgetId = budgetId,
    categoryId = categoryId,
    categoryLimit = categoryLimit
)

fun BudgetCategoryLimit.toEntity() = BudgetCategoryEntity(
    id = id,
    budgetId = budgetId,
    categoryId = categoryId,
    categoryLimit = categoryLimit
)
