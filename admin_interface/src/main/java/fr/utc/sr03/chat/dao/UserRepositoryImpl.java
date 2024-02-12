package fr.utc.sr03.chat.dao;

import fr.utc.sr03.chat.model.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public List<User> findAdminOnly() {
        Query query = entityManager.createQuery("SELECT u FROM User u WHERE u.isAdmin = :isAdmin", User.class).setParameter("isAdmin", true);

        return query.getResultList();
    }
}