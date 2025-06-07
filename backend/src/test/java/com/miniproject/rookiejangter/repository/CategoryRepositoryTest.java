package com.miniproject.rookiejangter.repository;

import com.miniproject.rookiejangter.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Category testCategory;

    @BeforeEach
    public void setup() {
        testCategory = Category.builder()
                .categoryName("테스트")
                .build();
        entityManager.persist(testCategory);
        entityManager.flush();
    }

    @Test
    public void testFindByCategoryName() {
        Optional<Category> foundCategory = categoryRepository.findByCategoryName("테스트");
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getCategoryName()).isEqualTo("테스트");
    }

    @Test
    public void testSaveCategory() {
        Category newCategory = Category.builder()
                .categoryName("New Category")
                .build();
        Category savedCategory = categoryRepository.save(newCategory);
        assertThat(savedCategory.getCategoryId()).isNotNull();
        assertThat(savedCategory.getCategoryName()).isEqualTo("New Category");
    }

    @Test
    public void testDeleteCategory() {
        categoryRepository.delete(testCategory);
        Optional<Category> deletedCategory = categoryRepository.findByCategoryName("Test Category1");
        assertThat(deletedCategory).isEmpty();
    }
}