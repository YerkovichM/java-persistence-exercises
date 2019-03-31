package ua.procamp.dao;

import ua.procamp.model.Company;

public interface CompanyDao {
    /**
     * Retrieves a {@link Company} with all its products by company id
     *
     * @param id company id
     * @return company with all its products
     */
    Company findByIdFetchProducts(Long id);
}
