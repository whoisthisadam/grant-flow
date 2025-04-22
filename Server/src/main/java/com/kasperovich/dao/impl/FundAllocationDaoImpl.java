package com.kasperovich.dao.impl;

import com.kasperovich.dao.FundAllocationDao;
import com.kasperovich.database.HibernateUtil;
import com.kasperovich.entities.FundAllocation;
import com.kasperovich.entities.AllocationStatus;
import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of FundAllocationDao using Hibernate.
 */
public class FundAllocationDaoImpl implements FundAllocationDao {
    private static final Logger logger = LoggerUtil.getLogger(FundAllocationDaoImpl.class);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<FundAllocation> findAll() {
        List<FundAllocation> allocations = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<FundAllocation> query = session.createQuery(
                "FROM FundAllocation a " +
                "JOIN FETCH a.budget " +
                "JOIN FETCH a.program " +
                "JOIN FETCH a.allocatedBy " +
                "ORDER BY a.allocationDate DESC", 
                FundAllocation.class);
            allocations = query.list();
            logger.debug("Found {} fund allocations", allocations.size());
        } catch (Exception e) {
            logger.error("Error finding all fund allocations", e);
        }
        return allocations;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<FundAllocation> findByBudgetId(Long budgetId) {
        List<FundAllocation> allocations = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<FundAllocation> query = session.createQuery(
                "FROM FundAllocation a " +
                "JOIN FETCH a.budget " +
                "JOIN FETCH a.program " +
                "JOIN FETCH a.allocatedBy " +
                "WHERE a.budget.id = :budgetId " +
                "ORDER BY a.allocationDate DESC", 
                FundAllocation.class);
            query.setParameter("budgetId", budgetId);
            allocations = query.list();
            logger.debug("Found {} fund allocations for budget ID: {}", allocations.size(), budgetId);
        } catch (Exception e) {
            logger.error("Error finding fund allocations by budget ID", e);
        }
        return allocations;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<FundAllocation> findByProgramId(Long programId) {
        List<FundAllocation> allocations = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<FundAllocation> query = session.createQuery(
                "FROM FundAllocation a " +
                "JOIN FETCH a.budget " +
                "JOIN FETCH a.program " +
                "JOIN FETCH a.allocatedBy " +
                "WHERE a.program.id = :programId " +
                "ORDER BY a.allocationDate DESC", 
                FundAllocation.class);
            query.setParameter("programId", programId);
            allocations = query.list();
            logger.debug("Found {} fund allocations for program ID: {}", allocations.size(), programId);
        } catch (Exception e) {
            logger.error("Error finding fund allocations by program ID", e);
        }
        return allocations;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<FundAllocation> findByStatus(AllocationStatus status) {
        List<FundAllocation> allocations = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<FundAllocation> query = session.createQuery(
                "FROM FundAllocation a " +
                "JOIN FETCH a.budget " +
                "JOIN FETCH a.program " +
                "JOIN FETCH a.allocatedBy " +
                "WHERE a.status = :status " +
                "ORDER BY a.allocationDate DESC", 
                FundAllocation.class);
            query.setParameter("status", status);
            allocations = query.list();
            logger.debug("Found {} fund allocations with status: {}", allocations.size(), status);
        } catch (Exception e) {
            logger.error("Error finding fund allocations by status", e);
        }
        return allocations;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<FundAllocation> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<FundAllocation> query = session.createQuery(
                "FROM FundAllocation a " +
                "JOIN FETCH a.budget " +
                "JOIN FETCH a.program " +
                "JOIN FETCH a.allocatedBy " +
                "WHERE a.id = :id", 
                FundAllocation.class);
            query.setParameter("id", id);
            FundAllocation allocation = query.uniqueResult();
            
            if (allocation != null) {
                logger.debug("Found fund allocation with ID: {}", id);
                return Optional.of(allocation);
            } else {
                logger.debug("No fund allocation found with ID: {}", id);
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Error finding fund allocation with ID: {}", id, e);
            return Optional.empty();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public FundAllocation save(FundAllocation allocation) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(allocation);
            transaction.commit();
            logger.debug("Saved fund allocation with ID: {}", allocation.getId());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving fund allocation", e);
        }
        return allocation;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public FundAllocation update(FundAllocation allocation) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(allocation);
            transaction.commit();
            logger.debug("Updated fund allocation with ID: {}", allocation.getId());
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error updating fund allocation with ID: {}", allocation.getId(), e);
        }
        return allocation;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(FundAllocation allocation) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(allocation);
            transaction.commit();
            logger.debug("Deleted fund allocation with ID: {}", allocation.getId());
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting fund allocation with ID: {}", allocation.getId(), e);
            return false;
        }
    }
}
