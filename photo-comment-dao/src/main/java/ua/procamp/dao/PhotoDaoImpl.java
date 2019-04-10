package ua.procamp.dao;

import ua.procamp.model.Photo;
import ua.procamp.model.PhotoComment;
import ua.procamp.util.EntityManagerUtil;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.List;

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
        EntityManagerUtil inTrans = new EntityManagerUtil(entityManagerFactory);
        inTrans.performWithinTx(em -> em.persist(photo));
    }

    @Override
    public Photo findById(long id) {
        EntityManagerUtil inTrans = new EntityManagerUtil(entityManagerFactory);
        return inTrans.performReturningWithinTx(em -> em.find(Photo.class, id));
    }

    @Override
    public List<Photo> findAll() {
        EntityManagerUtil inTrans = new EntityManagerUtil(entityManagerFactory);
        return inTrans.performReturningWithinTx(em -> em.createQuery("select p from Photo p", Photo.class)
                    .getResultList());
    }

    @Override
    public void remove(Photo photo) {
        EntityManagerUtil inTrans = new EntityManagerUtil(entityManagerFactory);
        inTrans.performWithinTx(em -> {
            Photo foundPhoto = em.merge(photo);
            em.remove(foundPhoto);
        });
    }

    @Override
    public void addComment(long photoId, String comment) {
        EntityManagerUtil inTrans = new EntityManagerUtil(entityManagerFactory);
        inTrans.performWithinTx(em -> {
            Photo photo = em.find(Photo.class, photoId);
            PhotoComment photoComment = new PhotoComment();
            photoComment.setPhoto(photo);
            photoComment.setText(comment);
            photoComment.setCreatedOn(LocalDateTime.now());
            em.persist(photoComment);
        });
    }
}
