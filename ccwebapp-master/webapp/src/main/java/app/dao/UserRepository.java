package app.dao;

import app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("select u from User u where u.email_address like %?1")
    List<User> findByUsername(String email);
}
