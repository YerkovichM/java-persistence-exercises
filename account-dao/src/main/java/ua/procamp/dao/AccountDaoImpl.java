package ua.procamp.dao;

import org.hibernate.jpa.QueryHints;
import ua.procamp.exception.AccountDaoException;
import ua.procamp.model.Account;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class AccountDaoImpl implements AccountDao {
    private EntityManagerFactory emf;

    public AccountDaoImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void save(Account account) {
        doInTrans(entityManager -> entityManager.persist(account));
    }

    @Override
    public Account findById(Long id) {
        return doInTransWithReturnReadOnly(entityManager -> entityManager
                .createQuery("select a from Account a where id = :id", Account.class)
                .setParameter("id", id)
                .getSingleResult());
    }

    @Override
    public Account findByEmail(String email) {
        return doInTransWithReturnReadOnly(entityManager -> entityManager
                .createQuery("select a from Account a where email = :email", Account.class)
                .setParameter("email", email)
                .getSingleResult());
    }

    @Override
    public List<Account> findAll() {
        return doInTransWithReturnReadOnly(entityManager -> entityManager
                .createQuery("select a from Account a", Account.class)
                .getResultList());
    }

    @Override
    public void update(Account account) {
        doInTrans(entityManager -> entityManager.merge(account));
    }

    @Override
    public void remove(Account account) {
        doInTrans(entityManager -> {
            Account merge = entityManager.merge(account);
            entityManager.remove(merge);
        });
    }

    private void doInTrans(Consumer<EntityManager> operation) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            operation.accept(entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Exception during transaction", e);
        } finally {
            entityManager.close();
        }
    }

    private <T> T doInTransWithReturn(Function<EntityManager, T> operation) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            T result = operation.apply(entityManager);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Exception during transaction", e);
        } finally {
            entityManager.close();
        }
    }

    private <T> T doInTransWithReturnReadOnly(Function<EntityManager, T> operation) {
        return doInTransWithReturn(entityManager -> {
            entityManager.setProperty(QueryHints.HINT_READONLY, true);
            return operation.apply(entityManager);
        });
    }
}

