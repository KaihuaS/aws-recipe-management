package app.dao;

import app.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Integer> {
    @Query("select i from Image i where i.recipeId = ?1")
    List<Image> findByRecipieId(String id);

    @Query("select i from Image i where i.id = ?1")
    Image findById(String id);

    @Modifying
    @Transactional
    @Query("delete from Image i where i.recipeId = ?1")
    int deleteByRecipieId(String recipie_id);
}
