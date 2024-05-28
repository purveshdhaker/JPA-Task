package com.griddynamics.jpahibernate;

import com.griddynamics.jpahibernate.entity.Child;
import com.griddynamics.jpahibernate.entity.Parent;
import com.griddynamics.jpahibernate.repository.ChildRepository;
import com.griddynamics.jpahibernate.repository.ParentRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class JpaEntityLifecycleTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private ChildRepository childRepository;

    @BeforeEach
    public void setup() {
        parentRepository.deleteAll();
        childRepository.deleteAll();
        // Reset the ID generation strategy for H2 database
        entityManager.createNativeQuery("ALTER TABLE PARENT ALTER COLUMN ID RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE CHILD ALTER COLUMN ID RESTART WITH 1").executeUpdate();
    }


    //Number3
    @Test
    public void testSaveParentWithoutId() {
        Parent parent = Parent.builder().name("Parent 1").build();

        // Using repository.save()
        Parent savedParentRepo = parentRepository.save(parent);
        entityManager.flush(); // Ensure changes are flushed to the database
        assertNotNull(savedParentRepo.getId());
        System.out.println("Repository.save() generated ID: " + savedParentRepo.getId());

        // Using entityManager.persist()
        Parent parentToPersist = Parent.builder().name("Parent 2").build();
        entityManager.persist(parentToPersist);
        entityManager.flush();
        assertNotNull(parentToPersist.getId());
        System.out.println("EntityManager.persist() generated ID: " + parentToPersist.getId());

        // Using entityManager.merge()
        Parent parentToMerge = Parent.builder().name("Parent 3").build();
        Parent mergedParent = entityManager.merge(parentToMerge);
        entityManager.flush();
        assertNotNull(mergedParent.getId());
        System.out.println("EntityManager.merge() generated ID: " + mergedParent.getId());
    }


    //Number4
    @Test
    public void testSaveParentWithInitializedId() {
        // Using repository.save()
        Parent parent = Parent.builder().id(1L).name("Parent 1").build();
        Parent savedParentRepo = parentRepository.save(parent);
        assertEquals(1L, savedParentRepo.getId());
        System.out.println("Repository.save() with initialized ID: " + savedParentRepo.getId());

        // Using entityManager.persist()
        Parent parentToPersist = Parent.builder().name("Parent 2").build();
        entityManager.persist(parentToPersist);
        entityManager.flush();
        assertNotNull(parentToPersist.getId());
        System.out.println("EntityManager.persist() with initialized ID: " + parentToPersist.getId());

        // Using entityManager.merge()
        Parent parentToMerge = Parent.builder().id(3L).name("Parent 3").build();
        Parent mergedParent = entityManager.merge(parentToMerge);
        entityManager.flush();
        assertEquals(3L, mergedParent.getId());
        System.out.println("EntityManager.merge() with initialized ID: " + mergedParent.getId());
    }


    //Number5
    //to be completed
    @Test
    public void testSaveParentWithDuplicateId() {
    }



    //Number6
    @Test
    public void testSaveParentWithChildren() {
        Parent parent = Parent.builder().name("Parent")
                .children(List.of(
                        Child.builder().name("Child 1").build(),
                        Child.builder().name("Child 2").build()
                ))
                .build();

        // Using repository.save()
        Parent savedParentRepo = parentRepository.save(parent);
        entityManager.flush(); // Ensure changes are flushed to the database
        assertNotNull(savedParentRepo.getId());
        assertEquals(2, savedParentRepo.getChildren().size());
        System.out.println("Repository.save() Parent ID: " + savedParentRepo.getId());

        // Using entityManager.persist()
        Parent parentToPersist = Parent.builder().name("Parent 2").build();
        Child child3 = Child.builder().name("Child 3").parent(parentToPersist).build();
        parentToPersist.setChildren(List.of(child3));

        entityManager.persist(parentToPersist);
        entityManager.flush();
        assertNotNull(parentToPersist.getId());
        assertEquals(1, parentToPersist.getChildren().size());
        System.out.println("EntityManager.persist() Parent ID: " + parentToPersist.getId());

        // Using entityManager.merge()
        Parent parentToMerge = Parent.builder().name("Parent 3").build();
        Child child4 = Child.builder().name("Child 4").parent(parentToMerge).build();
        parentToMerge.setChildren(List.of(child4));

        Parent mergedParent = entityManager.merge(parentToMerge);
        entityManager.flush();
        assertNotNull(mergedParent.getId());
        assertEquals(1, mergedParent.getChildren().size());
        System.out.println("EntityManager.merge() Parent ID: " + mergedParent.getId());
    }


    //Number7
    @Test
    public void testSaveParentWithExistingChildren() {
        // First, save the child separately
        Child existingChild = Child.builder().name("Existing Child").build();
        entityManager.persist(existingChild);
        entityManager.flush();
        assertNotNull(existingChild.getId());

        // Using repository.save()
        Parent parent1 = Parent.builder().name("Parent 1").children(List.of(existingChild)).build();
        existingChild.setParent(parent1);
        Parent savedParentRepo = parentRepository.save(parent1);
        entityManager.flush(); // Ensure changes are flushed to the database
        assertNotNull(savedParentRepo.getId());
        assertNotNull(savedParentRepo.getChildren().get(0).getId());
        System.out.println("Repository.save() Parent ID: " + savedParentRepo.getId() + ", Child ID: " + savedParentRepo.getChildren().get(0).getId());

        // Using entityManager.persist()
        Parent parent2 = Parent.builder().name("Parent 2").children(List.of(existingChild)).build();
        existingChild.setParent(parent2);
        try {
            entityManager.persist(parent2);
            entityManager.flush();
        } catch (Exception e) {
            System.out.println("EntityManager.persist() with existing child: " + e.getMessage());
        }

        // Using entityManager.merge()
        Parent parent3 = Parent.builder().name("Parent 3").children(List.of(existingChild)).build();
        existingChild.setParent(parent3);
        Parent mergedParent = entityManager.merge(parent3);
        entityManager.flush();
        assertNotNull(mergedParent.getId());
        assertNotNull(mergedParent.getChildren().get(0).getId());
        System.out.println("EntityManager.merge() Parent ID: " + mergedParent.getId() + ", Child ID: " + mergedParent.getChildren().get(0).getId());
    }


    //Number8
    @Test
    public void testSaveChildWithoutParent() {
        Child child = Child.builder().name("Orphan Child").build();

        // Using repository.save()
        Child savedChildRepo = childRepository.save(child);
        entityManager.flush(); // Ensuring changes are flushed to the database
        assertNotNull(savedChildRepo.getId());
        System.out.println("Repository.save() Child ID: " + savedChildRepo.getId());

        // Using entityManager.persist()
        Child childToPersist = Child.builder().name("Orphan Child 2").build();
        entityManager.persist(childToPersist);
        entityManager.flush();
        assertNotNull(childToPersist.getId());
        System.out.println("EntityManager.persist() Child ID: " + childToPersist.getId());

        // Using entityManager.merge()
        Child childToMerge = Child.builder().name("Orphan Child 3").build();
        Child mergedChild = entityManager.merge(childToMerge);
        entityManager.flush();
        assertNotNull(mergedChild.getId());
        System.out.println("EntityManager.merge() Child ID: " + mergedChild.getId());
    }

    @Test
    public void testSaveChildWithDetachedParent() {
        // First, save the parent and then detach it
        Parent parent = Parent.builder().name("Parent").build();
        entityManager.persist(parent);
        entityManager.flush();
        assertNotNull(parent.getId());
        entityManager.detach(parent);

        // Using repository.save()
        Child child1 = Child.builder().name("Child 1").parent(parent).build();
        Child savedChildRepo = childRepository.save(child1);
        entityManager.flush(); // Ensure changes are flushed to the database
        assertNotNull(savedChildRepo.getId());
        assertNotNull(savedChildRepo.getParent().getId());
        System.out.println("Repository.save() Child ID: " + savedChildRepo.getId() + ", Parent ID: " + savedChildRepo.getParent().getId());

        // Using entityManager.persist()
        Child child2 = Child.builder().name("Child 2").parent(parent).build();
        try {
            entityManager.persist(child2);
            entityManager.flush();
        } catch (Exception e) {
            System.out.println("EntityManager.persist() with detached parent: " + e.getMessage());
        }

        // Using entityManager.merge()
        Child child3 = Child.builder().name("Child 3").parent(parent).build();
        Child mergedChild = entityManager.merge(child3);
        entityManager.flush();
        assertNotNull(mergedChild.getId());
        assertNotNull(mergedChild.getParent().getId());
        System.out.println("EntityManager.merge() Child ID: " + mergedChild.getId() + ", Parent ID: " + mergedChild.getParent().getId());
    }

    //Number11
    @Test
    public void testFetchAndModifyParent() {
        Parent parent = Parent.builder().name("Parent").build();
        entityManager.persist(parent);
        entityManager.flush();
        entityManager.detach(parent);

        Parent fetchedParent = parentRepository.findById(parent.getId()).orElse(null);
        assertNotNull(fetchedParent);
        fetchedParent.setName("Modified Parent");

        entityManager.flush(); // Flushing changes
        Parent updatedParent = entityManager.find(Parent.class, parent.getId());
        assertEquals("Modified Parent", updatedParent.getName());
    }


    //Number12
    @Test
    @Transactional
    public void testTransactionFetchAndModifyParent() {
        Parent parent = Parent.builder().name("Parent").build();
        entityManager.persist(parent);
        entityManager.flush();
        entityManager.detach(parent);

        Parent fetchedParent = parentRepository.findById(parent.getId()).orElse(null);
        assertNotNull(fetchedParent);
        fetchedParent.setName("Modified Parent");

        // Need not to flush or commit transaction explicitly, Spring will handle it
        Parent updatedParent = parentRepository.findById(parent.getId()).orElse(null);
        assertEquals("Modified Parent", updatedParent.getName());
    }
}
