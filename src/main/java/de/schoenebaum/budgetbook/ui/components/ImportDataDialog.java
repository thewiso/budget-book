package de.schoenebaum.budgetbook.ui.components;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.shared.Registration;

import de.schoenebaum.budgetbook.services.ImportCSVService;
import de.schoenebaum.budgetbook.services.ImportCSVService.ImportResult;

public class ImportDataDialog extends Dialog {

	private static final long serialVersionUID = -1783724019650060092L;
	private static final Logger LOG = LoggerFactory.getLogger(ImportDataDialog.class);

	private final ImportCSVService importCSVService;

	private List<Runnable> changePersistedListeners = new LinkedList<>();

	private Button importButton;
	private MemoryBuffer buffer;
	private Upload upload;

	private VerticalLayout resultMessages;
	private Span greenResult;
	private Span yellowResult;
	private Span redResult;

	public ImportDataDialog(ImportCSVService importCSVService) {
		this.importCSVService = importCSVService;

		upload = new Upload();
		upload.addSucceededListener(this::onUploadSucceeded);
		upload.setAcceptedFileTypes(".csv");

		importButton = new Button("Import");
		importButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
		importButton.addClickListener(this::onImportButtonClicked);

		Button cancelButton = new Button("Close");
		cancelButton.addClickListener(event -> this.close());

		greenResult = new Span();
		greenResult.addClassName("text-green-bold");
		yellowResult = new Span();
		yellowResult.addClassName("text-yellow-bold");
		redResult = new Span();
		redResult.addClassName("text-red-bold");
		resultMessages = new VerticalLayout(greenResult, yellowResult, redResult);
		resultMessages.setVisible(false);

		HorizontalLayout uploadBar = new HorizontalLayout(upload, importButton);
		uploadBar.setAlignItems(Alignment.CENTER);

		add(uploadBar, resultMessages);
		getFooter().add(cancelButton);

		resetDialog();
	}

	@Override
	public void open() {
		resetDialog();
		super.open();
	}

	public Registration addChangePersistedListener(Runnable listener) {
		return Registration.addAndRemove(changePersistedListeners, listener);
	}

	protected void onUploadSucceeded(SucceededEvent event) {
		importButton.setEnabled(true);
	}

	protected void onImportButtonClicked(ClickEvent<?> event) {
		resetResultTexts();
		try {			
			ImportResult result = importCSVService.importCSV(buffer.getInputStream());
			if (result.getSuccesCount() > 0) {
				greenResult.setText(result.getSuccesCount() + " rows successfully imported");
			}
			if (result.getDuplicateCount() > 0) {
				yellowResult.setText(result.getDuplicateCount() + " rows were already imported");
			}
			if (result.getErrorCount() > 0) {
				redResult.setText(result.getErrorCount() + " rows could not be imported");
			}
			resetUpload();
			changePersistedListeners.forEach(Runnable::run);
		} catch (Exception e) {
			LOG.error("Could not import file {}", buffer.getFileName(), e);
			redResult.setText("Could not import file. Please try again or contact the administrator.");
		}
		resultMessages.setVisible(true);
	}

	protected void resetDialog() {
		resetUpload();
		resetResultTexts();
	}

	protected void resetUpload() {
		buffer = new MemoryBuffer();
		importButton.setEnabled(false);
		upload.clearFileList();
		upload.setReceiver(buffer);
	}

	protected void resetResultTexts() {
		resultMessages.setVisible(false);
		greenResult.setText("");
		yellowResult.setText("");
		redResult.setText("");
	}
}
