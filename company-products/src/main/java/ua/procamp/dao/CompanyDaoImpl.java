package ua.procamp.dao;

import ua.procamp.model.Company;

import javax.persistence.EntityManagerFactory;

public class CompanyDaoImpl implements CompanyDao {
    private EntityManagerFactory entityManagerFactory;

    public CompanyDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public Company findByIdFetchProducts(Long id) {
        throw new UnsupportedOperationException("I'm still not implemented!");
    }
}
