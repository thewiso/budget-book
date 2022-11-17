package de.schoenebaum.budgetbook.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.schoenebaum.budgetbook.db.entities.InternalTransactionTemplate;
import de.schoenebaum.budgetbook.db.repositories.InternalTransactionTemplateRepository;

@Service
@Transactional(readOnly = true)
public class InternalTransactionTemplateService {

	private final InternalTransactionTemplateRepository internalTransactionTemplateRepository;

	@Autowired
	public InternalTransactionTemplateService(
			InternalTransactionTemplateRepository internalTransactionTemplateRepository) {
		this.internalTransactionTemplateRepository = internalTransactionTemplateRepository;
	}

	public List<InternalTransactionTemplate> findAll() {
		return internalTransactionTemplateRepository.findAll();
	}

	@Transactional
	public void save(InternalTransactionTemplate internalTransactionTemplate) {
		internalTransactionTemplateRepository.save(internalTransactionTemplate);
	}

	@Transactional
	public void delete(InternalTransactionTemplate internalTransactionTemplate) {
		internalTransactionTemplateRepository.delete(internalTransactionTemplate);
	}
}
