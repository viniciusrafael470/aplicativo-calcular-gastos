package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ExpenseEntity
import com.example.ui.components.BudgetDialog
import com.example.ui.components.ExpenseEditorSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MeuBolsoTheme
import com.example.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch

enum class AppTab(
  val titleRes: Int,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
) {
  GASTOS(
    titleRes = R.string.tab_expenses,
    selectedIcon = Icons.Rounded.AccountBalanceWallet,
    unselectedIcon = Icons.Outlined.AccountBalanceWallet,
    testTag = "nav_tab_expenses",
  ),
  ANALISES(
    titleRes = R.string.tab_analytics,
    selectedIcon = Icons.Rounded.BarChart,
    unselectedIcon = Icons.Outlined.BarChart,
    testTag = "nav_tab_analytics",
  ),
  CALCULADORA(
    titleRes = R.string.tab_calculator,
    selectedIcon = Icons.Rounded.Calculate,
    unselectedIcon = Icons.Outlined.Calculate,
    testTag = "nav_tab_calculator",
  ),
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MeuBolsoTheme {
        MeuBolsoApp()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeuBolsoApp(
  expenseViewModel: ExpenseViewModel = viewModel(),
) {
  val uiState by expenseViewModel.uiState.collectAsStateWithLifecycle()
  val calcState by expenseViewModel.calculatorState.collectAsStateWithLifecycle()

  var currentTab by rememberSaveable { mutableStateOf(AppTab.GASTOS) }
  var showEditorSheet by remember { mutableStateOf(false) }
  var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
  var showBudgetDialog by remember { mutableStateOf(false) }

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val savedMsg = stringResource(R.string.expense_saved)
  val deletedMsg = stringResource(R.string.expense_deleted)
  val undoLabel = stringResource(R.string.undo_action)
  val budgetSavedMsg = stringResource(R.string.budget_saved)

  BackHandler(enabled = currentTab != AppTab.GASTOS) {
    currentTab = AppTab.GASTOS
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useNavigationRail = maxWidth >= 600.dp

    Row(modifier = Modifier.fillMaxSize()) {
      if (useNavigationRail) {
        NavigationRail(
          containerColor = MaterialTheme.colorScheme.surface,
          modifier = Modifier.fillMaxHeight(),
        ) {
          AppTab.entries.forEach { tab ->
            val selected = currentTab == tab
            NavigationRailItem(
              selected = selected,
              onClick = { currentTab = tab },
              icon = {
                Icon(
                  imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                  contentDescription = stringResource(tab.titleRes),
                )
              },
              label = { Text(stringResource(tab.titleRes)) },
              modifier = Modifier.testTag(tab.testTag),
            )
          }
        }
      }

      Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
          CenterAlignedTopAppBar(
            title = {
              Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
              )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
              containerColor = MaterialTheme.colorScheme.background,
            ),
          )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
          if (currentTab != AppTab.CALCULADORA) {
            ExtendedFloatingActionButton(
              onClick = {
                editingExpense = null
                showEditorSheet = true
              },
              icon = {
                Icon(
                  imageVector = Icons.Rounded.Add,
                  contentDescription = stringResource(R.string.cd_add_expense),
                )
              },
              text = { Text(stringResource(R.string.add_expense)) },
              shape = RoundedCornerShape(18.dp),
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.testTag("fab_add_expense"),
            )
          }
        },
        bottomBar = {
          if (!useNavigationRail) {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surface,
              tonalElevation = 3.dp,
            ) {
              AppTab.entries.forEach { tab ->
                val selected = currentTab == tab
                NavigationBarItem(
                  selected = selected,
                  onClick = { currentTab = tab },
                  icon = {
                    Icon(
                      imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                      contentDescription = stringResource(tab.titleRes),
                    )
                  },
                  label = { Text(stringResource(tab.titleRes)) },
                  modifier = Modifier.testTag(tab.testTag),
                )
              }
            }
          }
        },
        modifier = Modifier.weight(1f),
      ) { innerPadding ->
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .widthIn(max = 720.dp)
              .weight(1f),
          ) {
            when (currentTab) {
              AppTab.GASTOS -> {
                HomeScreen(
                  uiState = uiState,
                  onPreviousMonth = expenseViewModel::previousMonth,
                  onNextMonth = expenseViewModel::nextMonth,
                  onOpenBudgetDialog = { showBudgetDialog = true },
                  onSearchQueryChange = expenseViewModel::updateSearchQuery,
                  onCategoryFilterClick = expenseViewModel::toggleCategoryFilter,
                  onAddExpenseClick = {
                    editingExpense = null
                    showEditorSheet = true
                  },
                  onEditExpenseClick = { expense ->
                    editingExpense = expense
                    showEditorSheet = true
                  },
                  onDeleteExpenseClick = { expense ->
                    expenseViewModel.deleteExpense(expense)
                    scope.launch {
                      snackbarHostState.currentSnackbarData?.dismiss()
                      val result = snackbarHostState.showSnackbar(
                        message = deletedMsg,
                        actionLabel = undoLabel,
                        duration = SnackbarDuration.Short,
                      )
                      if (result == SnackbarResult.ActionPerformed) {
                        expenseViewModel.restoreExpense(expense)
                      }
                    }
                  },
                )
              }

              AppTab.ANALISES -> {
                AnalyticsScreen(
                  uiState = uiState,
                  onPreviousMonth = expenseViewModel::previousMonth,
                  onNextMonth = expenseViewModel::nextMonth,
                  onSelectMonth = expenseViewModel::selectMonth,
                )
              }

              AppTab.CALCULADORA -> {
                CalculatorScreen(
                  calcState = calcState,
                  dashboardState = uiState,
                  onAppendToken = expenseViewModel::appendCalcToken,
                  onEvaluateExpression = expenseViewModel::evaluateCalcExpression,
                  onAddItem = expenseViewModel::addCalcItem,
                  onRemoveItem = expenseViewModel::removeCalcItem,
                  onClearAll = expenseViewModel::clearAllCalculator,
                  onAdjustmentChange = expenseViewModel::updateAdjustmentPercent,
                  onSplitPeopleChange = expenseViewModel::updateSplitPeople,
                  onSaveCalculatedExpense = { title, category, paymentMethod, savePerPerson ->
                    val saved = expenseViewModel.saveCalculatedExpense(
                      title = title,
                      category = category,
                      paymentMethod = paymentMethod,
                      savePerPersonShare = savePerPerson,
                    )
                    if (saved) {
                      currentTab = AppTab.GASTOS
                      scope.launch {
                        snackbarHostState.showSnackbar(savedMsg)
                      }
                    }
                  },
                  onOpenBudgetDialog = { showBudgetDialog = true },
                )
              }
            }
          }
        }
      }
    }
  }

  if (showEditorSheet) {
    ExpenseEditorSheet(
      editingExpense = editingExpense,
      onDismiss = {
        showEditorSheet = false
        editingExpense = null
      },
      onSave = { existingId, title, amountCents, category, paymentMethod, dateMillis, notes, isRecurring ->
        expenseViewModel.saveExpense(
          existingId = existingId,
          title = title,
          amountCents = amountCents,
          category = category,
          paymentMethod = paymentMethod,
          dateMillis = dateMillis,
          notes = notes,
          isRecurring = isRecurring,
        )
        showEditorSheet = false
        editingExpense = null
        scope.launch {
          snackbarHostState.showSnackbar(savedMsg)
        }
      },
      onDelete = { expense ->
        showEditorSheet = false
        editingExpense = null
        expenseViewModel.deleteExpense(expense)
        scope.launch {
          snackbarHostState.currentSnackbarData?.dismiss()
          val result = snackbarHostState.showSnackbar(
            message = deletedMsg,
            actionLabel = undoLabel,
            duration = SnackbarDuration.Short,
          )
          if (result == SnackbarResult.ActionPerformed) {
            expenseViewModel.restoreExpense(expense)
          }
        }
      },
    )
  }

  if (showBudgetDialog) {
    BudgetDialog(
      currentBudgetCents = uiState.budgetLimitCents,
      yearMonth = uiState.selectedYearMonth,
      onDismiss = { showBudgetDialog = false },
      onSave = { limitCents, applyGlobally ->
        expenseViewModel.saveBudget(limitCents, applyGlobally)
        showBudgetDialog = false
        scope.launch {
          snackbarHostState.showSnackbar(budgetSavedMsg)
        }
      },
    )
  }
}
