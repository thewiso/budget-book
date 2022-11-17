package de.schoenebaum.budgetbook.services;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.comment.CommentMatcher;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import de.schoenebaum.budgetbook.configuration.ImportProperties;
import de.schoenebaum.budgetbook.configuration.ImportProperties.TransactionAttribute;
import de.schoenebaum.budgetbook.db.entities.Account;
import de.schoenebaum.budgetbook.db.entities.ExternalTransaction;
import de.schoenebaum.budgetbook.db.repositories.ExternalTransactionRepository;
import de.schoenebaum.budgetbook.utils.CollectionUtils;
import de.schoenebaum.budgetbook.utils.CombinationCellProcessor;
import de.schoenebaum.budgetbook.utils.StringUtils;
import de.schoenebaum.budgetbook.utils.UnclosableInputStream;
import iso.std.iso._20022.tech.xsd.camt_052_001.Document;
import iso.std.iso._20022.tech.xsd.camt_052_001.ObjectFactory;
import lombok.Getter;

@Service
public class ImportCSVService {

	private static final Logger LOG = LoggerFactory.getLogger(ImportCSVService.class);

	private final ImportProperties importProperties;
	private final ExternalTransactionRepository externalTransactionRepository;
	private final AccountService accountService;

	private static final String UNIQUE_CONSTRAINT_VIOLATION = "23505";

	@Autowired
	public ImportCSVService(ImportProperties importProperties,
			ExternalTransactionRepository externalTransactionRepository, AccountService accountService) {
		this.importProperties = importProperties;
		this.externalTransactionRepository = externalTransactionRepository;
		this.accountService = accountService;
	}

	protected static class IntroductionCommentMatcher implements CommentMatcher {

		private boolean introductionEnded = false;

		@Override
		public boolean isComment(String line) {
			if (introductionEnded) {
				return false;
			} else if (StringUtils.atMostMatches(line, ';', 2)) {
				return true;
			} else {
				introductionEnded = true;
				return false;
			}
		}
	}

	@Getter
	public static class ImportResult {

		private int succesCount = 0;
		private int duplicateCount = 0;
		private int errorCount = 0;

	}

	public ImportResult importCSV(InputStream stream) throws IOException, RuntimeException {
		CharsetMatch charsetMatch = new CharsetDetector().setText(stream)
			.detect();
		LOG.info("Detected charset {} with confidence of {}%", charsetMatch.getName(), charsetMatch.getConfidence());

		CsvPreference preference = new CsvPreference.Builder('"', ';', "\n").ignoreEmptyLines(true)
			.skipComments(new IntroductionCommentMatcher())
			.build();

		CsvBeanReader beanReader = null;

		try {
			beanReader = new CsvBeanReader(charsetMatch.getReader(), preference);
			String[] header = beanReader.getHeader(true);

			CellProcessor[] cellProcessors = new CellProcessor[header.length];
			String[] nameMapping = new String[header.length];
			fillNameMappingAndCellProcessors(header, nameMapping, cellProcessors);

			DigestUtils digestUtils = new DigestUtils("SHA3-256");
			ImportResult result = new ImportResult();

			Account openIncome = accountService.findOpenIncomeAccount();
			Account openExpense = accountService.findOpenExpenseAccount();

			ExternalTransaction transaction;
			while ((transaction = beanReader.read(ExternalTransaction.class, nameMapping, cellProcessors)) != null) {
				try {
					if (transaction.getExternalId() == null) {
						transaction.setExternalId(digestUtils.digestAsHex(stringifyExternalTransactionForHashCode(transaction)));
					}

					if (transaction.getAmount()
						.compareTo(BigDecimal.ZERO) < 0) {
						transaction.setAccount(openExpense);
					} else {
						transaction.setAccount(openIncome);
					}

					externalTransactionRepository.save(transaction);
					result.succesCount++;
				} catch (Exception e) {
					SQLException sqlException = ExceptionUtils.throwableOfType(e, SQLException.class);
					if (sqlException != null && UNIQUE_CONSTRAINT_VIOLATION.equals(sqlException.getSQLState())) {
						LOG.info(
								"Encounted exception indicating a unique constraint violation, most likely a duplicate");
						result.duplicateCount++;
					} else {
						LOG.error("Could not persist new transaction {}", transaction, e);
						result.errorCount++;
					}
				}
			}

			return result;
		} finally {
			if (beanReader != null) {
				beanReader.close();
			}
		}
	}

	protected void fillNameMappingAndCellProcessors(final String[] header, String[] nameMapping,
			CellProcessor[] cellProcessors) {
		List<String> headerList = Arrays.asList(header);

		for (TransactionAttribute attr : TransactionAttribute.values()) {
			List<String> attrHeaders = importProperties.getTransactionAttributeToCsvHeader()
				.get(attr);

			if (attr.isCombination()) {
				List<Integer> headerIndices = CollectionUtils.getAllMatchingIndices(headerList, attrHeaders);
				if (headerIndices.size() >= 0) {
					nameMapping[headerIndices.get(0)] = attr.name();
					if (headerIndices.size() > 1) {
						cellProcessors[headerIndices.get(0)] = new CombinationCellProcessor<>(String.class,
								org.apache.commons.lang3.StringUtils::isEmpty, StringUtils::joinSpace,
								attr.getCellProcessor(importProperties),
								headerIndices.subList(1, headerIndices.size()));
					} else {
						cellProcessors[headerIndices.get(0)] = new CombinationCellProcessor<>(String.class,
								org.apache.commons.lang3.StringUtils::isEmpty, StringUtils::joinSpace,
								attr.getCellProcessor(importProperties));
					}

				} else if (attr.isMandatory()) {
					throw new IllegalArgumentException("Could not find one of " + String.join(",", attrHeaders)
							+ " in header: " + String.join(",", headerList));
				}

			} else {
				int headerIndex = CollectionUtils.getFirstMatchingIndex(headerList, attrHeaders);
				if (headerIndex >= 0) {
					nameMapping[headerIndex] = attr.name();
					cellProcessors[headerIndex] = attr.getCellProcessor(importProperties);
				} else if (attr.isMandatory()) {
					throw new IllegalArgumentException("Could not find one of " + String.join(",", attrHeaders)
							+ " in header: " + String.join(",", headerList));
				}
			}
		}
	}

	@SuppressWarnings({ "unused", "unchecked" })
	protected void extractXmlFromZipFile(InputStream inputStream) {
		// perhaps we will need this, in a brighter future...
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			ZipInputStream zis = new ZipInputStream(inputStream);
			ZipEntry zipEntry = zis.getNextEntry();
			while (zipEntry != null) {
				Document camt52Document = ((JAXBElement<Document>) jaxbUnmarshaller
					.unmarshal(new UnclosableInputStream(zis))).getValue();

				zipEntry = zis.getNextEntry();
			}
		} catch (Exception e) {
		}
	}
	
	protected static String stringifyExternalTransactionForHashCode(ExternalTransaction externalTransaction) {
		return new StringBuilder()
				.append(externalTransaction.getAmount().toString())
				.append(externalTransaction.getSubject())
				.append(externalTransaction.getDate().toString())
				.append(externalTransaction.getRelatedPartyName())
				.append(externalTransaction.getRelatedPartyId())
				.toString();
	}
}
