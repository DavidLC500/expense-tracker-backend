package com.expensetracker.infrastructure.config;

import com.expensetracker.application.port.in.CreateCategoryUseCase;
import com.expensetracker.application.port.in.CreateRuleUseCase;
import com.expensetracker.application.port.in.DeleteBudgetUseCase;
import com.expensetracker.application.port.in.DeleteRuleUseCase;
import com.expensetracker.application.port.in.GetBudgetStatusUseCase;
import com.expensetracker.application.port.in.GetMonthlyReportUseCase;
import com.expensetracker.application.port.in.ImportTransactionsFromCsvUseCase;
import com.expensetracker.application.port.in.ImportTransactionsUseCase;
import com.expensetracker.application.port.in.ListCategoriesUseCase;
import com.expensetracker.application.port.in.ListRulesUseCase;
import com.expensetracker.application.port.in.ListTransactionMonthsUseCase;
import com.expensetracker.application.port.in.ListTransactionsUseCase;
import com.expensetracker.application.port.in.RecategorizeTransactionsUseCase;
import com.expensetracker.application.port.in.ResetTransactionCategoryUseCase;
import com.expensetracker.application.port.in.SetBudgetsUseCase;
import com.expensetracker.application.port.in.UpdateTransactionCategoryUseCase;
import com.expensetracker.application.port.out.BudgetRepository;
import com.expensetracker.application.port.out.CategoryRepository;
import com.expensetracker.application.port.out.CsvTransactionParser;
import com.expensetracker.application.port.out.RuleRepository;
import com.expensetracker.application.port.out.TransactionRepository;
import com.expensetracker.application.usecase.CreateCategoryService;
import com.expensetracker.application.usecase.CreateRuleService;
import com.expensetracker.application.usecase.DeleteBudgetService;
import com.expensetracker.application.usecase.DeleteRuleService;
import com.expensetracker.application.usecase.GetBudgetStatusService;
import com.expensetracker.application.usecase.GetMonthlyReportService;
import com.expensetracker.application.usecase.ImportTransactionsFromCsvService;
import com.expensetracker.application.usecase.ImportTransactionsService;
import com.expensetracker.application.usecase.ListCategoriesService;
import com.expensetracker.application.usecase.ListRulesService;
import com.expensetracker.application.usecase.ListTransactionMonthsService;
import com.expensetracker.application.usecase.ListTransactionsService;
import com.expensetracker.application.usecase.RecategorizeTransactionsService;
import com.expensetracker.application.usecase.ResetTransactionCategoryService;
import com.expensetracker.application.usecase.SetBudgetsService;
import com.expensetracker.application.usecase.UpdateTransactionCategoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cablea los casos de uso como beans de Spring. Las clases de {@code application.usecase} son Java
 * puro (sin anotaciones de Spring, por regla de arquitectura), así que el registro como
 * {@code @Bean} vive aquí, en infraestructura, en vez de anotar directamente esas clases.
 */
@Configuration
public class UseCaseConfig {

  @Bean
  public CreateCategoryUseCase createCategoryUseCase(CategoryRepository categoryRepository) {
    return new CreateCategoryService(categoryRepository);
  }

  @Bean
  public ListCategoriesUseCase listCategoriesUseCase(CategoryRepository categoryRepository) {
    return new ListCategoriesService(categoryRepository);
  }

  @Bean
  public CreateRuleUseCase createRuleUseCase(
      RuleRepository ruleRepository, CategoryRepository categoryRepository) {
    return new CreateRuleService(ruleRepository, categoryRepository);
  }

  @Bean
  public ListRulesUseCase listRulesUseCase(RuleRepository ruleRepository) {
    return new ListRulesService(ruleRepository);
  }

  @Bean
  public DeleteRuleUseCase deleteRuleUseCase(RuleRepository ruleRepository) {
    return new DeleteRuleService(ruleRepository);
  }

  @Bean
  public ImportTransactionsUseCase importTransactionsUseCase(
      TransactionRepository transactionRepository, RuleRepository ruleRepository) {
    return new ImportTransactionsService(transactionRepository, ruleRepository);
  }

  @Bean
  public ImportTransactionsFromCsvUseCase importTransactionsFromCsvUseCase(
      CsvTransactionParser csvTransactionParser,
      ImportTransactionsUseCase importTransactionsUseCase) {
    return new ImportTransactionsFromCsvService(csvTransactionParser, importTransactionsUseCase);
  }

  @Bean
  public RecategorizeTransactionsUseCase recategorizeTransactionsUseCase(
      TransactionRepository transactionRepository, RuleRepository ruleRepository) {
    return new RecategorizeTransactionsService(transactionRepository, ruleRepository);
  }

  @Bean
  public UpdateTransactionCategoryUseCase updateTransactionCategoryUseCase(
      TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
    return new UpdateTransactionCategoryService(transactionRepository, categoryRepository);
  }

  @Bean
  public ResetTransactionCategoryUseCase resetTransactionCategoryUseCase(
      TransactionRepository transactionRepository, RuleRepository ruleRepository) {
    return new ResetTransactionCategoryService(transactionRepository, ruleRepository);
  }

  @Bean
  public ListTransactionsUseCase listTransactionsUseCase(
      TransactionRepository transactionRepository) {
    return new ListTransactionsService(transactionRepository);
  }

  @Bean
  public ListTransactionMonthsUseCase listTransactionMonthsUseCase(
      TransactionRepository transactionRepository) {
    return new ListTransactionMonthsService(transactionRepository);
  }

  @Bean
  public SetBudgetsUseCase setBudgetsUseCase(
      BudgetRepository budgetRepository, CategoryRepository categoryRepository) {
    return new SetBudgetsService(budgetRepository, categoryRepository);
  }

  @Bean
  public DeleteBudgetUseCase deleteBudgetUseCase(BudgetRepository budgetRepository) {
    return new DeleteBudgetService(budgetRepository);
  }

  @Bean
  public GetBudgetStatusUseCase getBudgetStatusUseCase(
      BudgetRepository budgetRepository, TransactionRepository transactionRepository) {
    return new GetBudgetStatusService(budgetRepository, transactionRepository);
  }

  @Bean
  public GetMonthlyReportUseCase getMonthlyReportUseCase(
      TransactionRepository transactionRepository) {
    return new GetMonthlyReportService(transactionRepository);
  }
}
