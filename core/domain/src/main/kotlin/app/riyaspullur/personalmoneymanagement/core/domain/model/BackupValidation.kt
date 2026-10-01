package app.riyaspullur.personalmoneymanagement.core.domain.model

/** Validate the complete graph before replacement; never silently skip a record. */
fun BackupArchive.validate() {
    require(format == "PersonalMoneyManagementBackup" && version == 1)
    require(themeMode in listOf("SYSTEM", "LIGHT", "DARK"))
    fun ids(values: List<Long>): Set<Long> {
        require(values.all { it > 0 } && values.distinct().size == values.size)
        return values.toSet()
    }
    with(data) {
        val accountsById = ids(accounts.map { it.id })
        val groupsById = ids(accountGroups.map { it.id })
        val categoriesById = ids(categories.map { it.id })
        val budgetsById = ids(budgets.map { it.id })
        ids(transactions.map { it.id })
        ids(savingsGoals.map { it.id })
        ids(budgetCategoryLimits.map { it.id })
        val owners = accounts.map { it.userId } + accountGroups.map { it.userId } +
            categories.map { it.userId } + budgets.map { it.userId } +
            transactions.map { it.userId } + savingsGoals.map { it.userId }
        require(owners.all { it > 0 } && owners.distinct().size <= 1)
        accounts.forEach { require(it.groupId == null || it.groupId in groupsById) }
        val parents = categories.associate { it.id to it.parentCategoryId }
        categories.forEach {
            require(it.parentCategoryId == null || it.parentCategoryId in categoriesById)
            val visited = mutableSetOf<Long>()
            var current: Long? = it.id
            while (current != null) { require(visited.add(current)); current = parents[current] }
        }
        transactions.forEach {
            require(it.accountId in accountsById)
            require(it.toAccountId == null || it.toAccountId in accountsById)
            require(it.categoryId == null || it.categoryId in categoriesById)
        }
        budgetCategoryLimits.forEach {
            require(it.budgetId in budgetsById && it.categoryId in categoriesById)
        }
        require(receipts.keys == transactions.filter { it.receiptPath != null }.map { it.id.toString() }.toSet())
    }
}
