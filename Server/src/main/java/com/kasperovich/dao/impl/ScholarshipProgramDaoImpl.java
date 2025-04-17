package com.kasperovich.dao.impl;

import com.kasperovich.dao.ScholarshipProgramDao;
import com.kasperovich.database.HibernateUtil;
import com.kasperovich.entities.ScholarshipProgram;
import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of ScholarshipProgramDao using Hibernate.
 */
public class ScholarshipProgramDaoImpl implements ScholarshipProgramDao {
    private static final Logger logger = LoggerUtil.getLogger(ScholarshipProgramDaoImpl.class);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<ScholarshipProgram> findAll() {
        List<ScholarshipProgram> programs = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ScholarshipProgram> query = session.createQuery(
                "FROM ScholarshipProgram p JOIN FETCH p.createdBy", 
                ScholarshipProgram.class);
            programs = query.list();
            logger.debug("Found {} scholarship programs", programs.size());
        } catch (Exception e) {
            logger.error("Error finding all scholarship programs", e);
        }
        return programs;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<ScholarshipProgram> findAllActive() {
        List<ScholarshipProgram> programs = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ScholarshipProgram> query = session.createQuery(
                    "FROM ScholarshipProgram p JOIN FETCH p.createdBy WHERE p.active = true", 
                    ScholarshipProgram.class);
            programs = query.list();
            logger.debug("Found {} active scholarship programs", programs.size());
        } catch (Exception e) {
            logger.error("Error finding active scholarship programs", e);
        }
        return programs;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ScholarshipProgram findById(Long id) {
        ScholarshipProgram program = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ScholarshipProgram> query = session.createQuery(
                    "FROM ScholarshipProgram p JOIN FETCH p.createdBy WHERE p.id = :id",
                    ScholarshipProgram.class);
            query.setParameter("id", id);
            program = query.uniqueResult();
            
            if (program != null) {
                logger.debug("Found scholarship program with ID: {}", id);
            } else {
                logger.debug("No scholarship program found with ID: {}", id);
            }
        } catch (Exception e) {
            logger.error("Error finding scholarship program with ID: {}", id, e);
        }
        return program;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ScholarshipProgram save(ScholarshipProgram program) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(program);
            transaction.commit();
            logger.debug("Saved scholarship program with ID: {}", program.getId());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving scholarship program", e);
        }
        return program;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ScholarshipProgram update(ScholarshipProgram program) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(program);
            transaction.commit();
            logger.debug("Updated scholarship program with ID: {}", program.getId());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error updating scholarship program with ID: {}", program.getId(), e);
        }
        return program;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(ScholarshipProgram program) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(program);
            transaction.commit();
            logger.debug("Deleted scholarship program with ID: {}", program.getId());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting scholarship program with ID: {}", program.getId(), e);
        }
    }
}
