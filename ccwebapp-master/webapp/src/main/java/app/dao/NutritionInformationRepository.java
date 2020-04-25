package app.dao;

import app.model.NutritionInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface NutritionInformationRepository extends JpaRepository<NutritionInformation, Integer> {
    @Query("select n from NutritionInformation n where n.recipie_id = ?1")
    List<NutritionInformation> findByRecipieId(String id);

    @Modifying
    @Transactional
    @Query("delete from NutritionInformation n where n.recipie_id = ?1")
    int deleteByRecipieId(String recipie_id);
}
