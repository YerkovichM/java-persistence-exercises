package ua.procamp.dao;

import org.hibernate.jpa.QueryHints;
import ua.procamp.model.Photo;
import ua.procamp.model.PhotoComment;
import ua.procamp.util.EntityManagerUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Please note that you should not use auto-commit mode for your implementation.
 */
public class PhotoDaoImpl implements PhotoDao {
    private EntityManagerFactory entityManagerFactory;

    public PhotoDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void save(Photo photo) {
        doInTrans(em -> em.persist(photo));
    }

    @Override
    public Photo findById(long id) {
        return doInTransWithReturnReadOnly(em -> em.find(Photo.class, id));
    }

    @Override
    public List<Photo> findAll() {
        return doInTransWithReturnReadOnly(em -> em.createQuery("select p from Photo p", Photo.class)
                    .getResultList());
    }

    @Override
    public void remove(Photo photo) {
        doInTrans(em -> {
            Photo foundPhoto = em.merge(photo);
            em.remove(foundPhoto);
        });
    }

    @Override
    public void addComment(long photoId, String comment) {
        EntityManagerUtil inTrans = new EntityManagerUtil(entityManagerFactory);
        doInTrans(em -> {
            Photo photo = em.getReference(Photo.class, photoId);
            PhotoComment photoComment = new PhotoComment();
            photoComment.setPhoto(photo);
            photoComment.setText(comment);
            photoComment.setCreatedOn(LocalDateTime.now());
            em.persist(photoComment);
        });
    }

    private void doInTrans(Consumer<EntityManager> operation){
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            operation.accept(entityManager);
            entityManager.getTransaction().commit();
        }catch (Exception e){
            entityManager.getTransaction().rollback();
            throw e;
        }finally {
            entityManager.close();
        }
    }

    private <T> T doInTransWithReturn(Function<EntityManager, T> operation){
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
            try {
                T result = operation.apply(entityManager);
                entityManager.getTransaction().commit();
                return result;
            }catch (Exception e){
                entityManager.getTransaction().rollback();
                throw e;
            }finally {
                entityManager.close();
            }
    }

    private <T> T doInTransWithReturnReadOnly(Function<EntityManager, T> operation){
        return doInTransWithReturn(entityManager -> {
           entityManager.setProperty(QueryHints.HINT_READONLY, true);
           return operation.apply(entityManager);
        });
    }
}
