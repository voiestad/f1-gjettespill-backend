package no.vebb.f1.domain;

import no.vebb.f1.util.domainPrimitive.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GuessService {

    private final CategoryRepository categoryRepository;

    public GuessService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryEntity::categoryName)
                .map(Category::new)
                .toList();
    }

    public boolean isValidCategory(String category) {
        return categoryRepository.existsById(category);
    }
}
