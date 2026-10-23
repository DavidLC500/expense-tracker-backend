package com.expensetracker.infrastructure.adapter.in.web;

import com.expensetracker.application.port.in.CreateCategoryUseCase;
import com.expensetracker.application.port.in.ListCategoriesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints de categorías: {@code GET}/{@code POST /api/categories}. */
@Tag(name = "Categories", description = "Categorías a las que se asignan los movimientos.")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

  private final CreateCategoryUseCase createCategoryUseCase;
  private final ListCategoriesUseCase listCategoriesUseCase;

  public CategoryController(
      CreateCategoryUseCase createCategoryUseCase, ListCategoriesUseCase listCategoriesUseCase) {
    this.createCategoryUseCase = createCategoryUseCase;
    this.listCategoriesUseCase = listCategoriesUseCase;
  }

  @Operation(
      operationId = "listCategories",
      summary = "Listar categorías",
      description = "Ordenadas por nombre, sin distinguir mayúsculas ni tildes.")
  @ApiResponse(responseCode = "200", description = "Todas las categorías.")
  @GetMapping
  public List<CategoryResponse> list() {
    return listCategoriesUseCase.execute().stream().map(CategoryResponse::from).toList();
  }

  @Operation(
      operationId = "createCategory",
      summary = "Crear una categoría",
      description =
          "El nombre es único sin distinguir mayúsculas, tildes ni espacios de más: «Ocio» y"
              + " «ocio» son la misma. Lo garantiza la base de datos, también ante peticiones"
              + " simultáneas.")
  @ApiResponse(responseCode = "201", description = "Categoría creada.")
  @ApiResponse(responseCode = "400", description = "Nombre vacío o de más de 255 caracteres.")
  @ApiResponse(responseCode = "409", description = "Ya existe una categoría con ese nombre.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CategoryResponse create(@Valid @RequestBody CreateCategoryRequest request) {
    return CategoryResponse.from(createCategoryUseCase.execute(request.name()));
  }
}
