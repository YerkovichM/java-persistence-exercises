package ua.procamp.dao;

import ua.procamp.exception.AccountDaoException;
import ua.procamp.model.Account;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;

public class AccountDaoImpl implements AccountDao {
    private EntityManagerFactory emf;

    public AccountDaoImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public void save(Account account) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            entityManager.persist(account);
            entityManager.getTransaction().commit();
        }catch (Exception e){
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Exception during transaction", e);
        }finally {
            entityManager.close();
        }
    }

    @Override
    public Account findById(Long id) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        Account res = null;
        try {
             res = entityManager
                     .createQuery("select a from Account a where id = :id", Account.class)
                    .setParameter("id", id)
                    .getSingleResult();
             entityManager.getTransaction().commit();
        }catch (Exception e){
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Exception during transaction", e);
        }finally {
            entityManager.close();
        }
        return res;
    }

    @Override
    public Account findByEmail(String email) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        Account res = null;
        try {
            res = entityManager
                    .createQuery("select a from Account a where email = :email", Account.class)
                    .setParameter("email", email)
                    .getSingleResult();
            entityManager.getTransaction().commit();
        }catch (Exception e){
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Exception during transaction", e);
        }finally {
            entityManager.close();
        }
        return res;
    }

    @Override
    public List<Account> findAll() {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        List<Account> res;
        try {
            res = entityManager
                    .createQuery("select a from Account a", Account.class)
                    .getResultList();
            entityManager.getTransaction().commit();
        }catch (Exception e){
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Exception during transaction", e);
        }finally {
            entityManager.close();
        }
        return res;
    }

    @Override
    public void update(Account account) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            entityManager.merge(account);
            entityManager.getTransaction().commit();
        }catch (Exception e){
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Exception during transaction", e);
        }finally {
            entityManager.close();
        }
    }

    @Override
    public void remove(Account account) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Account merge = entityManager.merge(account);
            entityManager.remove(merge);
            entityManager.getTransaction().commit();
        }catch (Exception e){
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Exception during transaction", e);
        }finally {
            entityManager.close();
        }
    }
}

