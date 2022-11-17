package de.schoenebaum.budgetbook.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.schoenebaum.budgetbook.db.entities.ExternalTransaction;
import iso.std.iso._20022.tech.xsd.camt_052_001.CashAccount16;
import iso.std.iso._20022.tech.xsd.camt_052_001.Document;
import iso.std.iso._20022.tech.xsd.camt_052_001.EntryTransaction2;
import iso.std.iso._20022.tech.xsd.camt_052_001.PartyIdentification32;
import iso.std.iso._20022.tech.xsd.camt_052_001.ReportEntry2;

/**
 * Class will perhaps be needed in the future, when all banks provide the
 * standardized XML reports The mapping provided by this class is really
 * primitive and should be over-worked when the time is ripe
 */
@Service
public class ImportXMLMapper {

	private static final Logger LOG = LoggerFactory.getLogger(ImportXMLMapper.class);

	public ExternalTransaction mapReportEntry(ReportEntry2 reportEntry) {
		// TODO ...
		EntryTransaction2 entryTransaction = getEntryTransaction(reportEntry);
		BigDecimal amount = reportEntry.getAmt()
			.getValue();

		PartyIdentification32 relatedParty;
		CashAccount16 relatedPartyAccount;
		switch (reportEntry.getCdtDbtInd()) {
		case CRDT:
			relatedParty = entryTransaction.getRltdPties()
				.getDbtr();
			relatedPartyAccount = entryTransaction.getRltdPties()
				.getDbtrAcct();
			break;
		case DBIT:
			relatedParty = entryTransaction.getRltdPties()
				.getCdtr();
			relatedPartyAccount = entryTransaction.getRltdPties()
				.getCdtrAcct();
			amount = amount.negate();
			break;
		default:
			throw new RuntimeException("Unexpected value for  Credit Debit Indicator " + reportEntry.getCdtDbtInd());
		}

		String relatedPartyName;
		String relatedPartyId = null;

		if (relatedParty != null && relatedParty.getNm() != null && relatedPartyAccount.getId()
			.getIBAN() != null) {
			relatedPartyName = relatedParty.getNm();
			relatedPartyId = relatedPartyAccount.getId()
				.getIBAN();
		} else {
			// TODO!
			relatedPartyName = "Unknown";
			LOG.warn("Cannot obtain related party information");
		}

		String externalId = entryTransaction.getRefs()
			.getPrtry()
			.getRef();
		String subject;
		if (entryTransaction.getRmtInf()
			.getUstrd()
			.size() > 0) {
			subject = entryTransaction.getRmtInf()
				.getUstrd()
				.get(0); // TODO
		} else {
			subject = reportEntry.getAddtlNtryInf();
		}

		ExternalTransaction retVal = new ExternalTransaction();

		retVal.setAmount(amount);
		retVal.setDate(mapXMLGregorianCalendear(reportEntry.getBookgDt()
			.getDt()));
		retVal.setExternalId(externalId);
		retVal.setSubject(subject);
		retVal.setRelatedPartyId(relatedPartyId);
		retVal.setRelatedPartyName(relatedPartyName);

		return retVal;
	}

	public List<ExternalTransaction> mapCamt52Document(Document document) {
		// TODO ..-
		return document.getBkToCstmrAcctRpt()
			.getRpt()
			.get(0)
			.getNtry()
			.stream()
			.map(this::mapReportEntry)
			.collect(Collectors.toList());

	}

	public LocalDate mapXMLGregorianCalendear(XMLGregorianCalendar xmlGregorianCalendar) {
		return LocalDate.of(xmlGregorianCalendar.getYear(), xmlGregorianCalendar.getMonth(),
				xmlGregorianCalendar.getDay());
	}

	public EntryTransaction2 getEntryTransaction(ReportEntry2 reportEntry) {
		// TODO ...
		return reportEntry.getNtryDtls()
			.get(0)
			.getTxDtls()
			.get(0);
	}

}
