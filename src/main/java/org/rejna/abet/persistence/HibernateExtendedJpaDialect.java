package org.rejna.abet.persistence;

import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.hibernate.Session;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.transaction.TransactionDefinition;

public class HibernateExtendedJpaDialect extends HibernateJpaDialect {
	private static final long serialVersionUID = -5852316229619516232L;

	 @Override
	    public Object beginTransaction(EntityManager entityManager,
	            TransactionDefinition definition) throws PersistenceException,
	            SQLException /*, TransactionException */ {

	        Session session = (Session) entityManager.getDelegate();
	        DataSourceUtils.prepareConnectionForTransaction(session.connection(), definition);

	        entityManager.getTransaction().begin();

	        return prepareTransaction(entityManager, definition.isReadOnly(), definition.getName());
	    }

}
