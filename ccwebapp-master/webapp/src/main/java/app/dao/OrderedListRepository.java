package app.dao;

import app.model.OrderedList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface OrderedListRepository extends JpaRepository<OrderedList, Integer> {
    @Query("select o from OrderedList o where o.recipie_id = ?1")
    List<OrderedList> findByRecipieId(String recipie_id);

    @Modifying
    @Transactional
    @Query("delete from OrderedList o where o.recipie_id = ?1")
    int deleteByRecipieId(String recipie_id);
}
