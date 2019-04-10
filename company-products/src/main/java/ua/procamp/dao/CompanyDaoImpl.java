package ua.procamp.dao;

import ua.procamp.model.Company;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class CompanyDaoImpl implements CompanyDao {
    private EntityManagerFactory entityManagerFactory;

    public CompanyDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public Company findByIdFetchProducts(Long id) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try{
            Company company = entityManager
                    .createQuery("select c from Company c left join fetch c.products where c.id = :id", Company.class)
                    .setParameter("id", id)
                    .getSingleResult();
            entityManager.getTransaction().commit();
            return company;
        }catch (Exception ex){
            entityManager.getTransaction().getRollbackOnly();
            throw ex;
        }finally {
            entityManager.close();
        }
    }
}
