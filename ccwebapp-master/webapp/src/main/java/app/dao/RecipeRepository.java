package app.dao;

import app.model.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
    @Query("select r from Recipe r where r.id = ?1")
    Recipe findById(String id);

    @Query("select r from Recipe r where r.author_id = ?1")
    List<Recipe> findByUserId(String id);

    @Query("select r from Recipe r order by r.created_ts desc ")
    List<Recipe> getLatest();

}
