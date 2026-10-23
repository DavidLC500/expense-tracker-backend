package com.expensetracker.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Reglas de arquitectura hexagonal (ver sección "Arquitectura hexagonal" del README).
 *
 * <p>Solo se analizan las clases de producción ({@code DoNotIncludeTests}): estas reglas restringen
 * cómo se relacionan domain/application/infrastructure entre sí, no el código de test (que
 * legítimamente usa JUnit, ArchUnit, etc.).
 */
@AnalyzeClasses(
    packages = "com.expensetracker",
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  @ArchTest
  static final ArchRule domainShouldNotDependOnApplicationOrInfrastructure =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..application..", "..infrastructure..");

  @ArchTest
  static final ArchRule applicationShouldNotDependOnInfrastructure =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..infrastructure..");

  @ArchTest
  static final ArchRule domainAndApplicationShouldNotDependOnSpring =
      noClasses()
          .that()
          .resideInAnyPackage("..domain..", "..application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.springframework..");

  @ArchTest
  static final ArchRule domainAndApplicationShouldNotDependOnJpa =
      noClasses()
          .that()
          .resideInAnyPackage("..domain..", "..application..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("jakarta.persistence..");
}
