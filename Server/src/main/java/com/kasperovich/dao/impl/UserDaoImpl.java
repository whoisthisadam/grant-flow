package com.kasperovich.dao.impl;

import com.kasperovich.dao.UserDao;
import com.kasperovich.database.HibernateUtil;
import com.kasperovich.entities.User;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementation of the UserDao interface using Hibernate.
 */
public class UserDaoImpl extends BaseDaoImpl<User, Long> implements UserDao {

    @Override
    public Optional<User> findByUsername(String username) {
        logger.debug("Finding user by username: {}", username);
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .uniqueResultOptional();
        } catch (Exception e) {
            logger.error("Error finding user by username", e);
            throw new RuntimeException("Error finding user by username", e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .uniqueResultOptional();
        } catch (Exception e) {
            logger.error("Error finding user by email", e);
            throw new RuntimeException("Error finding user by email", e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        logger.debug("Checking if user exists by username: {}", username);
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                    .setParameter("username", username)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("Error checking if user exists by username", e);
            throw new RuntimeException("Error checking if user exists by username", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        logger.debug("Checking if user exists by email: {}", email);
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                    .setParameter("email", email)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("Error checking if user exists by email", e);
            throw new RuntimeException("Error checking if user exists by email", e);
        }
    }

    @Override
    public void updateLastLogin(Long userId) {
        logger.debug("Updating last login time for user with ID: {}", userId);
        Transaction transaction = null;
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            int updatedCount = session.createQuery(
                    "UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :userId")
                    .setParameter("lastLogin", LocalDateTime.now())
                    .setParameter("userId", userId)
                    .executeUpdate();
            
            transaction.commit();
            
            if (updatedCount > 0) {
                logger.debug("Last login time updated successfully");
            } else {
                logger.warn("No user found with ID: {}", userId);
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error updating last login time", e);
            throw new RuntimeException("Error updating last login time", e);
        }
    }
}
