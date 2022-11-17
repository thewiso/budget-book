package de.schoenebaum.budgetbook.services;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import de.schoenebaum.budgetbook.db.entities.Account;
import de.schoenebaum.budgetbook.db.entities.ExternalTransaction;
import de.schoenebaum.budgetbook.db.entities.InternalTransaction;
import de.schoenebaum.budgetbook.db.entities.InternalTransactionTemplate;
import de.schoenebaum.budgetbook.db.entities.Transaction;
import de.schoenebaum.budgetbook.db.repositories.ExternalTransactionRepository;
import de.schoenebaum.budgetbook.db.repositories.InternalTransactionRepository;
import de.schoenebaum.budgetbook.mapper.DbEntityMapper;
import de.schoenebaum.budgetbook.ui.model.GridTransaction;
import de.schoenebaum.budgetbook.ui.model.UIExternalTransaction;
import de.schoenebaum.budgetbook.ui.model.UIInternalTransaction;
import de.schoenebaum.budgetbook.ui.model.UITransaction;

@Service
@Transactional(readOnly = true)
public class TransactionService {

	private final ExternalTransactionRepository externalTransactionRepository;
	private final InternalTransactionRepository internalTransactionRepository;
	private final DbEntityMapper dbEntityMapper;
	private final EntityManager entityManager;
	private final PlatformTransactionManager transactionManager;

	@Autowired
	public TransactionService(ExternalTransactionRepository externalTransactionRepository,
			InternalTransactionRepository internalTransactionRepository, DbEntityMapper dbEntityMapper,
			EntityManager entityManager, PlatformTransactionManager transactionManager) {
		this.externalTransactionRepository = externalTransactionRepository;
		this.internalTransactionRepository = internalTransactionRepository;
		this.dbEntityMapper = dbEntityMapper;
		this.entityManager = entityManager;
		this.transactionManager = transactionManager;
	}

	@Transactional
	public void save(Transaction transaction) {
		if (transaction instanceof UITransaction) {
			transaction = ((UITransaction) transaction).getTransaction();
		}

		if (transaction instanceof ExternalTransaction) {
			externalTransactionRepository.save((ExternalTransaction) transaction);
		} else if (transaction instanceof InternalTransaction) {
			internalTransactionRepository.save((InternalTransaction) transaction);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Transactional
	public void save(InternalTransaction internalTransaction) {
		internalTransactionRepository.save(internalTransaction);
	}

	@Transactional
	public void save(ExternalTransaction externalTransaction) {
		externalTransactionRepository.save(externalTransaction);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void delete(Transaction transaction) {
		if (transaction instanceof UITransaction) {
			transaction = ((UITransaction) transaction).getTransaction();
		}

		if (transaction instanceof ExternalTransaction) {
			externalTransactionRepository.delete((ExternalTransaction) transaction);
		} else if (transaction instanceof InternalTransaction) {
			internalTransactionRepository.delete((InternalTransaction) transaction);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Transactional
	public void delete(InternalTransaction internalTransaction) {
		internalTransactionRepository.delete(internalTransaction);
	}

	@Transactional
	public void delete(ExternalTransaction externalTransaction) {
		externalTransactionRepository.delete(externalTransaction);
	}

	@SuppressWarnings("unchecked")
	public <T extends Transaction & GridTransaction> List<T> getTransactions(Account account) {
		return (List<T>) Stream.concat(externalTransactionRepository.findByAccount(account)
			.stream()
			.map(UIExternalTransaction::new),
				internalTransactionRepository.findByAccount(account)
					.stream()
					.map(t -> new UIInternalTransaction(t, account)))
			.toList();
	}

	public BigDecimal getSum(Account account) {
		return externalTransactionRepository.getSumByAccount(account)
			.orElse(BigDecimal.ZERO)
			.add(internalTransactionRepository.getSumBySourceAccount(account)
				.orElse(BigDecimal.ZERO)
				.negate())
			.add(internalTransactionRepository.getSumByTargetAccount(account)
				.orElse(BigDecimal.ZERO));
	}

	@Transactional
	public void fulfillTemplates(Set<InternalTransactionTemplate> templates) {
		templates.stream()
			.map(dbEntityMapper::mapInternalTransactionTemplate)
			.forEach(internalTransactionRepository::save);
	}

	@SuppressWarnings("unchecked")
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public Map<String, Map<LocalDate, BigDecimal>> getDailyBalances(LocalDate startDate, LocalDate endDate) {
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		TransactionStatus status = transactionManager.getTransaction(definition);

		try {
			Query setStartDate = entityManager.createNativeQuery("SET @StartDate = :startDate");
			setStartDate.setParameter("startDate", startDate);
			setStartDate.executeUpdate();

			Query setEndDate = entityManager.createNativeQuery("SET @EndDate = :endDate");
			setEndDate.setParameter("endDate", endDate);
			setEndDate.executeUpdate();

			Query query = entityManager.createNativeQuery(
					"""
							WITH RECURSIVE DAY_DATES(DAY_DATE) AS (
							    SELECT @StartDate
							    UNION ALL
							    SELECT FORMATDATETIME(DATEADD(day, 1, DAY_DATE), 'yyyy-MM-dd') FROM DAY_DATES WHERE DATEADD(day, 1, DAY_DATE) <= @EndDate
							),
							ACCOUNT_AND_DATE AS(
								SELECT ACCOUNT.ID AS ACCOUNT_ID, DAY_DATES.DAY_DATE AS DATE
								FROM DAY_DATES
								LEFT JOIN ACCOUNT
							)
							SELECT TRANSACTION_AMOUNT.ACCOUNT_ID AS ACCOUNT_ID, ACCOUNT.NAME AS ACCOUNT_NAME, TRANSACTION_AMOUNT.DATE AS DATE, COALESCE(SUM(TRANSACTION_AMOUNT.AMOUNT), 0) AS BALANCE
							FROM (
								SELECT ACCOUNT_AND_DATE.ACCOUNT_ID as ACCOUNT_ID, ACCOUNT_AND_DATE.DATE as DATE, it_source.AMOUNT * -1 as AMOUNT
								FROM ACCOUNT_AND_DATE
								LEFT JOIN INTERNAL_TRANSACTION it_source ON it_source.DATE<= ACCOUNT_AND_DATE.DATE AND it_source.SOURCE_ACCOUNT_ID = ACCOUNT_AND_DATE.ACCOUNT_ID
								UNION ALL
								SELECT ACCOUNT_AND_DATE.ACCOUNT_ID as ACCOUNT_ID, ACCOUNT_AND_DATE.DATE as DATE, it_target.AMOUNT as AMOUNT
								FROM ACCOUNT_AND_DATE
								LEFT JOIN INTERNAL_TRANSACTION it_target ON it_target.DATE<= ACCOUNT_AND_DATE.DATE AND it_target.TARGET_ACCOUNT_ID = ACCOUNT_AND_DATE.ACCOUNT_ID
								UNION ALL
								SELECT ACCOUNT_AND_DATE.ACCOUNT_ID as ACCOUNT_ID, ACCOUNT_AND_DATE.DATE as DATE, et.AMOUNT as AMOUNT
								FROM ACCOUNT_AND_DATE
								LEFT JOIN EXTERNAL_TRANSACTION et ON et.DATE<= ACCOUNT_AND_DATE.DATE AND et.ACCOUNT_ID = ACCOUNT_AND_DATE.ACCOUNT_ID
							) TRANSACTION_AMOUNT
							JOIN ACCOUNT ON ACCOUNT.ID = TRANSACTION_AMOUNT.ACCOUNT_ID
							GROUP BY TRANSACTION_AMOUNT.ACCOUNT_ID, TRANSACTION_AMOUNT.DATE
							ORDER BY TRANSACTION_AMOUNT.ACCOUNT_ID, TRANSACTION_AMOUNT.DATE;
										""",
					Tuple.class);

			return new TreeMap<>(((Stream<Tuple>) query.getResultStream())
				.collect(Collectors.groupingBy(tuple -> tuple.get("ACCOUNT_NAME", String.class),
						Collectors.toMap(tuple -> tuple.get("DATE", Date.class)
							.toLocalDate(), tuple -> tuple.get("BALANCE", BigDecimal.class), BigDecimal::add,
								TreeMap::new))));

		} finally {
			// Because h2 creates new views for CTEs and those make problems when restarting
			// the db, those should never be commited
			transactionManager.rollback(status);
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, BigDecimal> getExpensesByAccountNames() {
		Query query = entityManager.createNativeQuery("""

				SELECT a.NAME as ACCOUNT_NAME, COALESCE(SUM(expense.AMOUNT), 0) as EXPENSE
				FROM (
					SELECT it.SOURCE_ACCOUNT_ID AS ACCOUNT_ID, it.AMOUNT as AMOUNT
					FROM INTERNAL_TRANSACTION it
					UNION
					SELECT et.ACCOUNT_ID, et.AMOUNT * -1
					FROM EXTERNAL_TRANSACTION et
					WHERE et.AMOUNT < 0
				) expense
				RIGHT JOIN ACCOUNT a ON expense.ACCOUNT_ID = a.ID
				GROUP BY a.NAME
				ORDER BY EXPENSE DESC;
							""", Tuple.class);

		return ((Stream<Tuple>) query.getResultStream())
			.collect(Collectors.toMap(tuple -> tuple.get("ACCOUNT_NAME", String.class),
					tuple -> tuple.get("EXPENSE", BigDecimal.class), BigDecimal::add));
	}

}
