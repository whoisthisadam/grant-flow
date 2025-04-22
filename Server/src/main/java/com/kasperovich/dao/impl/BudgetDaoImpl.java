package com.kasperovich.dao.impl;

import com.kasperovich.dao.BudgetDao;
import com.kasperovich.database.HibernateUtil;
import com.kasperovich.entities.Budget;
import com.kasperovich.entities.BudgetStatus;
import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of BudgetDao using Hibernate.
 */
public class BudgetDaoImpl implements BudgetDao {
    private static final Logger logger = LoggerUtil.getLogger(BudgetDaoImpl.class);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Budget> findAll() {
        List<Budget> budgets = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Budget> query = session.createQuery(
                "FROM Budget b JOIN FETCH b.createdBy ORDER BY b.fiscalYear DESC, b.startDate DESC", 
                Budget.class);
            budgets = query.list();
            logger.debug("Found {} budgets", budgets.size());
        } catch (Exception e) {
            logger.error("Error finding all budgets", e);
        }
        return budgets;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Budget> findByStatus(BudgetStatus status) {
        List<Budget> budgets = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Budget> query = session.createQuery(
                "FROM Budget b JOIN FETCH b.createdBy WHERE b.status = :status ORDER BY b.fiscalYear DESC, b.startDate DESC", 
                Budget.class);
            query.setParameter("status", status);
            budgets = query.list();
            logger.debug("Found {} budgets with status {}", budgets.size(), status);
        } catch (Exception e) {
            logger.error("Error finding budgets by status", e);
        }
        return budgets;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Budget> findActiveBudget() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Budget> query = session.createQuery(
                "FROM Budget b JOIN FETCH b.createdBy " +
                "WHERE b.status = :status " +
                "AND b.startDate <= :currentDate " +
                "AND b.endDate >= :currentDate", 
                Budget.class);
            query.setParameter("status", BudgetStatus.ACTIVE);
            query.setParameter("currentDate", LocalDate.now());
            Budget budget = query.uniqueResult();
            
            if (budget != null) {
                logger.debug("Found active budget with ID: {}", budget.getId());
                return Optional.of(budget);
            } else {
                logger.debug("No active budget found");
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Error finding active budget", e);
            return Optional.empty();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Budget> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Budget> query = session.createQuery(
                "FROM Budget b JOIN FETCH b.createdBy WHERE b.id = :id", 
                Budget.class);
            query.setParameter("id", id);
            Budget budget = query.uniqueResult();
            
            if (budget != null) {
                logger.debug("Found budget with ID: {}", id);
                return Optional.of(budget);
            } else {
                logger.debug("No budget found with ID: {}", id);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Error finding budget with ID: {}", id, e);
            return Optional.empty();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Budget save(Budget budget) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(budget);
            transaction.commit();
            logger.debug("Saved budget with ID: {}", budget.getId());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving budget", e);
        }
        return budget;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Budget update(Budget budget) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(budget);
            transaction.commit();
            logger.debug("Updated budget with ID: {}", budget.getId());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error updating budget with ID: {}", budget.getId(), e);
        }
        return budget;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(Budget budget) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(budget);
            transaction.commit();
            logger.debug("Deleted budget with ID: {}", budget.getId());
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting budget with ID: {}", budget.getId(), e);
            return false;
        }
    }
}
