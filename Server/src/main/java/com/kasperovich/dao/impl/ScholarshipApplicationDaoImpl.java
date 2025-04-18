package com.kasperovich.dao.impl;

import com.kasperovich.dao.ScholarshipApplicationDao;
import com.kasperovich.database.HibernateUtil;
import com.kasperovich.entities.ScholarshipApplication;
import com.kasperovich.entities.User;
import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ScholarshipApplicationDao using Hibernate.
 */
public class ScholarshipApplicationDaoImpl implements ScholarshipApplicationDao {
    private static final Logger logger = LoggerUtil.getLogger(ScholarshipApplicationDaoImpl.class);

    /**
     * Saves a scholarship application.
     *
     * @param application the application to save
     * @return the saved application
     */
    @Override
    public ScholarshipApplication save(ScholarshipApplication application) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(application);
            transaction.commit();
            logger.info("Saved scholarship application with ID: {}", application.getId());
            return application;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving scholarship application", e);
            throw e;
        }
    }

    /**
     * Finds a scholarship application by its ID.
     *
     * @param id the ID of the application
     * @return an Optional containing the application if found, empty otherwise
     */
    @Override
    public Optional<ScholarshipApplication> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Use a query with join fetch to eagerly load related entities
            Query<ScholarshipApplication> query = session.createQuery(
                    "FROM ScholarshipApplication a " +
                    "LEFT JOIN FETCH a.applicant " +
                    "LEFT JOIN FETCH a.program " +
                    "LEFT JOIN FETCH a.period " +
                    "LEFT JOIN FETCH a.reviewer " +
                    "WHERE a.id = :id",
                    ScholarshipApplication.class);
            query.setParameter("id", id);
            
            ScholarshipApplication application = query.uniqueResult();
            return Optional.ofNullable(application);
        } catch (Exception e) {
            logger.error("Error finding scholarship application by ID: {}", id, e);
            return Optional.empty();
        }
    }

    /**
     * Finds all scholarship applications submitted by a specific user.
     *
     * @param user the user who submitted the applications
     * @return a list of applications submitted by the user
     */
    @Override
    public List<ScholarshipApplication> findByApplicant(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Use a query with join fetch to eagerly load related entities
            Query<ScholarshipApplication> query = session.createQuery(
                    "FROM ScholarshipApplication a " +
                    "LEFT JOIN FETCH a.applicant " +
                    "LEFT JOIN FETCH a.program " +
                    "LEFT JOIN FETCH a.period " +
                    "LEFT JOIN FETCH a.reviewer " +
                    "WHERE a.applicant = :applicant",
                    ScholarshipApplication.class);
            query.setParameter("applicant", user);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error finding scholarship applications by applicant: {}", user.getId(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Finds all scholarship applications for a specific program.
     *
     * @param programId the ID of the program
     * @return a list of applications for the program
     */
    @Override
    public List<ScholarshipApplication> findByProgramId(Long programId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Use a query with join fetch to eagerly load related entities
            Query<ScholarshipApplication> query = session.createQuery(
                    "FROM ScholarshipApplication a " +
                    "LEFT JOIN FETCH a.applicant " +
                    "LEFT JOIN FETCH a.program " +
                    "LEFT JOIN FETCH a.period " +
                    "LEFT JOIN FETCH a.reviewer " +
                    "WHERE a.program.id = :programId",
                    ScholarshipApplication.class);
            query.setParameter("programId", programId);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error finding scholarship applications by program ID: {}", programId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Finds all scholarship applications for a specific academic period.
     *
     * @param periodId the ID of the academic period
     * @return a list of applications for the period
     */
    @Override
    public List<ScholarshipApplication> findByPeriodId(Long periodId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Use a query with join fetch to eagerly load related entities
            Query<ScholarshipApplication> query = session.createQuery(
                    "FROM ScholarshipApplication a " +
                    "LEFT JOIN FETCH a.applicant " +
                    "LEFT JOIN FETCH a.program " +
                    "LEFT JOIN FETCH a.period " +
                    "LEFT JOIN FETCH a.reviewer " +
                    "WHERE a.period.id = :periodId",
                    ScholarshipApplication.class);
            query.setParameter("periodId", periodId);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error finding scholarship applications by period ID: {}", periodId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Finds all scholarship applications with a specific status.
     *
     * @param status the status to filter by
     * @return a list of applications with the given status
     */
    @Override
    public List<ScholarshipApplication> findByStatus(String status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Use a query with join fetch to eagerly load related entities
            Query<ScholarshipApplication> query = session.createQuery(
                    "FROM ScholarshipApplication a " +
                    "LEFT JOIN FETCH a.applicant " +
                    "LEFT JOIN FETCH a.program " +
                    "LEFT JOIN FETCH a.period " +
                    "LEFT JOIN FETCH a.reviewer " +
                    "WHERE a.status = :status",
                    ScholarshipApplication.class);
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error finding scholarship applications by status: {}", status, e);
            return new ArrayList<>();
        }
    }

    /**
     * Finds all scholarship applications.
     *
     * @return a list of all applications
     */
    @Override
    public List<ScholarshipApplication> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Use a query with join fetch to eagerly load related entities
            Query<ScholarshipApplication> query = session.createQuery(
                    "FROM ScholarshipApplication a " +
                    "LEFT JOIN FETCH a.applicant " +
                    "LEFT JOIN FETCH a.program " +
                    "LEFT JOIN FETCH a.period " +
                    "LEFT JOIN FETCH a.reviewer",
                    ScholarshipApplication.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error finding all scholarship applications", e);
            return new ArrayList<>();
        }
    }

    /**
     * Updates a scholarship application.
     *
     * @param application the application to update
     * @return the updated application
     */
    @Override
    public ScholarshipApplication update(ScholarshipApplication application) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(application);
            transaction.commit();
            logger.info("Updated scholarship application with ID: {}", application.getId());
            return application;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error updating scholarship application", e);
            throw e;
        }
    }

    /**
     * Deletes a scholarship application.
     *
     * @param application the application to delete
     */
    @Override
    public void delete(ScholarshipApplication application) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(application);
            transaction.commit();
            logger.info("Deleted scholarship application with ID: {}", application.getId());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting scholarship application", e);
            throw e;
        }
    }
}
