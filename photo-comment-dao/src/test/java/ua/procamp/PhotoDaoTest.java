package ua.procamp;

import ua.procamp.dao.PhotoDao;
import ua.procamp.dao.PhotoDaoImpl;
import ua.procamp.model.Photo;
import ua.procamp.util.EntityManagerUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

import static ua.procamp.util.PhotoTestDataGenerator.createListOfRandomPhotos;
import static ua.procamp.util.PhotoTestDataGenerator.createRandomPhoto;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PhotoDaoTest {
    private EntityManagerUtil emUtil;
    private PhotoDao photoDao;
    private EntityManagerFactory entityManagerFactory;

    @BeforeEach
    public void setup() {
        entityManagerFactory = Persistence.createEntityManagerFactory("PhotoComments");
        emUtil = new EntityManagerUtil(entityManagerFactory);
        photoDao = new PhotoDaoImpl(entityManagerFactory);
    }

    @AfterEach
    public void destroy() {
        entityManagerFactory.close();
    }

    @Test
    public void testSavePhoto() {
        Photo photo = createRandomPhoto();

        photoDao.save(photo);

        Photo fountPhoto = emUtil.performReturningWithinTx(entityManager -> entityManager.find(Photo.class, photo.getId()));
        assertThat(fountPhoto, equalTo(photo));
    }

    @Test
    public void testFindPhotoById() {
        Photo photo = createRandomPhoto();
        emUtil.performWithinTx(entityManager -> entityManager.persist(photo));

        Photo foundPhoto = photoDao.findById(photo.getId());

        assertThat(foundPhoto, equalTo(photo));
    }

    @Test
    public void testFindAllPhotos() {
        List<Photo> listOfRandomPhotos = createListOfRandomPhotos(5);
        emUtil.performWithinTx(entityManager -> listOfRandomPhotos.forEach(entityManager::persist));

        List<Photo> foundPhotos = photoDao.findAll();

        assertThat(foundPhotos, containsInAnyOrder(listOfRandomPhotos.toArray()));
    }

    @Test
    public void testRemovePhoto() {
        Photo photo = createRandomPhoto();
        emUtil.performWithinTx(entityManager -> entityManager.persist(photo));

        photoDao.remove(photo);

        Photo removedPhoto = emUtil.performReturningWithinTx(entityManager -> entityManager.find(Photo.class, photo.getId()));
        assertThat(removedPhoto, nullValue());
    }

    @Test
    public void testAddPhotoComment() {
        Photo photo = createRandomPhoto();
        emUtil.performWithinTx(entityManager -> entityManager.persist(photo));

        photoDao.addComment(photo.getId(), "Nice picture!");

        emUtil.performWithinTx(entityManager -> {
            Photo managedPhoto = entityManager.find(Photo.class, photo.getId());
            assertThat(managedPhoto.getComments(),
                    hasItem(hasProperty("text", equalTo("Nice picture!"))));
        });
    }
}
