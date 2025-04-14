package com.kasperovich.dao.impl;

import com.kasperovich.dao.BaseDao;
import com.kasperovich.database.HibernateUtil;
import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;

/**
 * Base implementation of the BaseDao interface using Hibernate.
 *
 * @param <T> The entity type
 * @param <ID> The type of the entity's primary key
 */
public abstract class BaseDaoImpl<T, ID extends Serializable> implements BaseDao<T, ID> {
    
    protected final Logger logger;
    protected final Class<T> entityClass;
    
    /**
     * Constructor that initializes the entity class using reflection.
     */
    @SuppressWarnings("unchecked")
    public BaseDaoImpl() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
        this.logger = LoggerUtil.getLogger(getClass());
        logger.debug("Initialized DAO for entity class: {}", entityClass.getSimpleName());
    }
    
    @Override
    public T save(T entity) {
        logger.debug("Saving entity of type {}", entityClass.getSimpleName());
        Transaction transaction = null;
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(entity);
            transaction.commit();
            logger.debug("Entity saved successfully");
            return entity;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving entity", e);
            throw new RuntimeException("Error saving entity", e);
        }
    }
    
    @Override
    public Optional<T> findById(ID id) {
        logger.debug("Finding entity of type {} with ID: {}", entityClass.getSimpleName(), id);
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            T entity = session.get(entityClass, id);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            logger.error("Error finding entity by ID", e);
            throw new RuntimeException("Error finding entity by ID", e);
        }
    }
    
    @Override
    public List<T> findAll() {
        logger.debug("Finding all entities of type {}", entityClass.getSimpleName());
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM " + entityClass.getName(), entityClass).list();
        } catch (Exception e) {
            logger.error("Error finding all entities", e);
            throw new RuntimeException("Error finding all entities", e);
        }
    }
    
    @Override
    public void delete(T entity) {
        logger.debug("Deleting entity of type {}", entityClass.getSimpleName());
        Transaction transaction = null;
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(entity);
            transaction.commit();
            logger.debug("Entity deleted successfully");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting entity", e);
            throw new RuntimeException("Error deleting entity", e);
        }
    }
    
    @Override
    public boolean deleteById(ID id) {
        logger.debug("Deleting entity of type {} with ID: {}", entityClass.getSimpleName(), id);
        
        Optional<T> entityOpt = findById(id);
        if (entityOpt.isPresent()) {
            delete(entityOpt.get());
            return true;
        }
        return false;
    }
    
    @Override
    public boolean existsById(ID id) {
        logger.debug("Checking if entity of type {} exists with ID: {}", entityClass.getSimpleName(), id);
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT COUNT(e) FROM " + entityClass.getName() + " e WHERE e.id = :id", Long.class)
                    .setParameter("id", id)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("Error checking if entity exists by ID", e);
            throw new RuntimeException("Error checking if entity exists by ID", e);
        }
    }
    
    @Override
    public long count() {
        logger.debug("Counting entities of type {}", entityClass.getSimpleName());
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT COUNT(e) FROM " + entityClass.getName() + " e", Long.class)
                    .uniqueResult();
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.error("Error counting entities", e);
            throw new RuntimeException("Error counting entities", e);
        }
    }
}
