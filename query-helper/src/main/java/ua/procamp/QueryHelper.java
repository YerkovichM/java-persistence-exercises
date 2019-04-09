package ua.procamp;

import org.hibernate.annotations.QueryHints;
import ua.procamp.exception.QueryHelperException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.function.Function;

/**
 * {@link QueryHelper} provides a util method that allows to perform read operations in the scope of transaction
 */
public class QueryHelper {
    private EntityManagerFactory entityManagerFactory;

    public QueryHelper(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * Receives a {@link Function<EntityManager, T>}, creates {@link EntityManager} instance, starts transaction,
     * performs received function and commits the transaction, in case of exception in rollbacks the transaction and
     * throws a {@link QueryHelperException} with the following message: "Error performing query. Transaction is rolled back"
     * <p>
     * The purpose of this method is to perform read operations using {@link EntityManager}, so it uses read only mode
     * by default.
     *
     * @param entityManagerConsumer query logic encapsulated as function that receives entity manager and returns result
     * @param <T>                   generic type that allows to specify single entity class of some collection
     * @return query result specified by type T
     */
    public <T> T readWithinTx(Function<EntityManager, T> entityManagerConsumer) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try{
            entityManager.setProperty(QueryHints.READ_ONLY, true);
            return entityManagerConsumer.apply(entityManager);
        }catch (Exception e){
            entityManager.getTransaction().rollback();
            throw new QueryHelperException("Error performing query. Transaction is rolled back" ,e);
        }finally {
            entityManager.close();
        }
    }
}
