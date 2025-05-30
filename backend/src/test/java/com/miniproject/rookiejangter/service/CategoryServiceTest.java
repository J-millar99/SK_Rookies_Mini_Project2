package com.miniproject.rookiejangter.service;

import com.miniproject.rookiejangter.controller.dto.CategoryDTO;
import com.miniproject.rookiejangter.entity.Category;
import com.miniproject.rookiejangter.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("카테고리 생성 성공 테스트")
    void createCategorySuccessTest() {
        // Given
        String categoryName = "의류";
        Category savedCategory = Category.builder()
                .categoryId(1)
                .categoryName(categoryName)
                .build();
        when(categoryRepository.findByCategoryName(categoryName)).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // When
        CategoryDTO.Response response = categoryService.createCategory(categoryName);

        // Then
        assertThat(response.getCategoryId()).isEqualTo(1);
        assertThat(response.getCategoryName()).isEqualTo(categoryName);
        verify(categoryRepository, times(1)).findByCategoryName(categoryName);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 생성 실패 테스트 (이미 존재하는 이름)")
    void createCategoryFailAlreadyExistsTest() {
        // Given
        String categoryName = "전자제품";
        when(categoryRepository.findByCategoryName(categoryName)).thenReturn(Optional.of(new Category()));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(categoryName));
        verify(categoryRepository, times(1)).findByCategoryName(categoryName);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("ID로 카테고리 조회 성공 테스트")
    void getCategoryByIdSuccessTest() {
        // Given
        Integer categoryId = 1;
        String categoryName = "도서";
        Category foundCategory = Category.builder()
                .categoryId(categoryId)
                .categoryName(categoryName)
                .build();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(foundCategory));

        // When
        CategoryDTO.Response response = categoryService.getCategoryById(categoryId);

        // Then
        assertThat(response.getCategoryId()).isEqualTo(categoryId);
        assertThat(response.getCategoryName()).isEqualTo(categoryName);
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    @DisplayName("ID로 카테고리 조회 실패 테스트 (EntityNotFoundException)")
    void getCategoryByIdNotFoundTest() {
        // Given
        Integer invalidCategoryId = 999;
        when(categoryRepository.findById(invalidCategoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> categoryService.getCategoryById(invalidCategoryId));
        verify(categoryRepository, times(1)).findById(invalidCategoryId);
    }

    @Test
    @DisplayName("이름으로 카테고리 조회 성공 테스트")
    void getCategoryByNameSuccessTest() {
        // Given
        String categoryName = "식품";
        Category foundCategory = Category.builder()
                .categoryId(2)
                .categoryName(categoryName)
                .build();
        when(categoryRepository.findByCategoryName(categoryName)).thenReturn(Optional.of(foundCategory));

        // When
        CategoryDTO.Response response = categoryService.getCategoryByName(categoryName);

        // Then
        assertThat(response.getCategoryName()).isEqualTo(categoryName);
        assertThat(response.getCategoryId()).isEqualTo(2);
        verify(categoryRepository, times(1)).findByCategoryName(categoryName);
    }

    @Test
    @DisplayName("이름으로 카테고리 조회 실패 테스트 (EntityNotFoundException)")
    void getCategoryByNameNotFoundTest() {
        // Given
        String invalidCategoryName = "가구";
        when(categoryRepository.findByCategoryName(invalidCategoryName)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> categoryService.getCategoryByName(invalidCategoryName));
        verify(categoryRepository, times(1)).findByCategoryName(invalidCategoryName);
    }

    @Test
    @DisplayName("모든 카테고리 조회 테스트")
    void getAllCategoriesTest() {
        // Given
        List<Category> categories = Arrays.asList(
                Category.builder().categoryId(1).categoryName("패션").build(),
                Category.builder().categoryId(2).categoryName("취미").build()
        );
        when(categoryRepository.findAll()).thenReturn(categories);

        // When
        List<CategoryDTO.Response> responses = categoryService.getAllCategories();

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getCategoryName()).isEqualTo("패션");
        assertThat(responses.get(1).getCategoryName()).isEqualTo("취미");
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("이름으로 카테고리 검색 테스트")
    void searchCategoriesByNameTest() {
        // Given
        String searchKeyword = "전";
        List<Category> searchResults = Arrays.asList(
                Category.builder().categoryId(1).categoryName("전자제품").build(),
                Category.builder().categoryId(3).categoryName("전기").build()
        );
        when(categoryRepository.findByCategoryNameContainingIgnoreCase(searchKeyword)).thenReturn(searchResults);

        // When
        List<CategoryDTO.Response> responses = categoryService.searchCategoriesByName(searchKeyword);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getCategoryName()).isEqualTo("전자제품");
        assertThat(responses.get(1).getCategoryName()).isEqualTo("전기");
        verify(categoryRepository, times(1)).findByCategoryNameContainingIgnoreCase(searchKeyword);
    }

    @Test
    @DisplayName("카테고리 수정 성공 테스트")
    void updateCategorySuccessTest() {
        // Given
        Integer categoryId = 1;
        String existingName = "가전";
        String newName = "생활가전";
        Category existingCategory = Category.builder().categoryId(categoryId).categoryName(existingName).build();
        Category updatedCategory = Category.builder().categoryId(categoryId).categoryName(newName).build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.findByCategoryName(newName)).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

        // When
        CategoryDTO.Response response = categoryService.updateCategory(categoryId, newName);

        // Then
        assertThat(response.getCategoryId()).isEqualTo(categoryId);
        assertThat(response.getCategoryName()).isEqualTo(newName);
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).findByCategoryName(newName);
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 수정 실패 테스트 (ID Not Found)")
    void updateCategoryNotFoundTest() {
        // Given
        Integer invalidCategoryId = 999;
        String newName = "주방용품";
        when(categoryRepository.findById(invalidCategoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> categoryService.updateCategory(invalidCategoryId, newName));
        verify(categoryRepository, times(1)).findById(invalidCategoryId);
        verify(categoryRepository, never()).findByCategoryName(anyString());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 수정 실패 테스트 (새 이름 이미 존재)")
    void updateCategoryFailNameExistsTest() {
        // Given
        Integer categoryId = 1;
        String existingName = "의류";
        String newName = "잡화";
        Category existingCategory = Category.builder().categoryId(categoryId).categoryName(existingName).build();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.findByCategoryName(newName)).thenReturn(Optional.of(new Category()));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> categoryService.updateCategory(categoryId, newName));
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).findByCategoryName(newName);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 삭제 성공 테스트")
    void deleteCategorySuccessTest() {
        // Given
        Integer categoryId = 1;
        Category categoryToDelete = Category.builder().categoryId(categoryId).categoryName("신발").build();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryToDelete));
        doNothing().when(categoryRepository).delete(categoryToDelete);

        // When
        categoryService.deleteCategory(categoryId);

        // Then
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).delete(categoryToDelete);
    }

    @Test
    @DisplayName("카테고리 삭제 실패 테스트 (ID Not Found)")
    void deleteCategoryNotFoundTest() {
        // Given
        Integer invalidCategoryId = 999;
        when(categoryRepository.findById(invalidCategoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> categoryService.deleteCategory(invalidCategoryId));
        verify(categoryRepository, times(1)).findById(invalidCategoryId);
        verify(categoryRepository, never()).delete(any());
    }
}