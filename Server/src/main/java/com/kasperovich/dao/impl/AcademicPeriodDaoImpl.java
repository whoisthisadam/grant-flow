package com.kasperovich.dao.impl;

import com.kasperovich.dao.AcademicPeriodDao;
import com.kasperovich.database.HibernateUtil;
import com.kasperovich.entities.AcademicPeriod;
import com.kasperovich.utils.LoggerUtil;
import jakarta.persistence.NoResultException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementation of the AcademicPeriodDao interface.
 */
public class AcademicPeriodDaoImpl extends BaseDaoImpl<AcademicPeriod, Long> implements AcademicPeriodDao {
    
    private static final Logger logger = LoggerUtil.getLogger(AcademicPeriodDaoImpl.class);
    
    public AcademicPeriodDaoImpl() {
        super();
    }
    
    @Override
    public AcademicPeriod findByName(String name) {
        logger.debug("Finding academic period by name: {}", name);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<AcademicPeriod> query = builder.createQuery(AcademicPeriod.class);
            Root<AcademicPeriod> root = query.from(AcademicPeriod.class);
            
            query.select(root)
                 .where(builder.equal(root.get("name"), name));
            
            return session.createQuery(query).getSingleResult();
        } catch (NoResultException e) {
            logger.debug("No academic period found with name: {}", name);
            return null;
        } catch (Exception e) {
            logger.error("Error finding academic period by name: {}", name, e);
            throw e;
        }
    }
    
    @Override
    public List<AcademicPeriod> findActiveAcademicPeriods() {
        logger.debug("Finding all active academic periods");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<AcademicPeriod> query = builder.createQuery(AcademicPeriod.class);
            Root<AcademicPeriod> root = query.from(AcademicPeriod.class);
            
            LocalDate today = LocalDate.now();
            
            query.select(root)
                 .where(builder.and(
                     builder.lessThanOrEqualTo(root.get("startDate"), today),
                     builder.greaterThanOrEqualTo(root.get("endDate"), today)
                 ));
            
            return session.createQuery(query).getResultList();
        } catch (Exception e) {
            logger.error("Error finding active academic periods", e);
            throw e;
        }
    }
    
    @Override
    public List<AcademicPeriod> findByDate(LocalDate date) {
        logger.debug("Finding academic periods that include date: {}", date);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<AcademicPeriod> query = builder.createQuery(AcademicPeriod.class);
            Root<AcademicPeriod> root = query.from(AcademicPeriod.class);
            
            query.select(root)
                 .where(builder.and(
                     builder.lessThanOrEqualTo(root.get("startDate"), date),
                     builder.greaterThanOrEqualTo(root.get("endDate"), date)
                 ));
            
            return session.createQuery(query).getResultList();
        } catch (Exception e) {
            logger.error("Error finding academic periods that include date: {}", date, e);
            throw e;
        }
    }
    
    @Override
    public List<AcademicPeriod> findByType(String type) {
        logger.debug("Finding academic periods of type: {}", type);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<AcademicPeriod> query = builder.createQuery(AcademicPeriod.class);
            Root<AcademicPeriod> root = query.from(AcademicPeriod.class);
            
            query.select(root)
                 .where(builder.equal(root.get("type"), type));
            
            return session.createQuery(query).getResultList();
        } catch (Exception e) {
            logger.error("Error finding academic periods of type: {}", type, e);
            throw e;
        }
    }
    
    @Override
    public List<AcademicPeriod> findByActive(boolean active) {
        logger.debug("Finding academic periods with active status: {}", active);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<AcademicPeriod> query = builder.createQuery(AcademicPeriod.class);
            Root<AcademicPeriod> root = query.from(AcademicPeriod.class);
            
            LocalDate today = LocalDate.now();
            
            if (active) {
                // If active=true, find periods where current date is between start and end dates
                query.select(root)
                     .where(builder.and(
                         builder.lessThanOrEqualTo(root.get("startDate"), today),
                         builder.greaterThanOrEqualTo(root.get("endDate"), today)
                     ));
            } else {
                // If active=false, find periods where current date is not between start and end dates
                query.select(root)
                     .where(builder.or(
                         builder.greaterThan(root.get("startDate"), today),
                         builder.lessThan(root.get("endDate"), today)
                     ));
            }
            
            return session.createQuery(query).getResultList();
        } catch (Exception e) {
            logger.error("Error finding academic periods with active status: {}", active, e);
            throw e;
        }
    }
}
