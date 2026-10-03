package com.codeb.ims;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.web.server.ResponseStatusException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ImsApplicationTests {

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private CompanyChainRepository companyChainRepository;

	@Autowired
	private BrandRepository brandRepository;

	@Autowired
	private ZoneRepository zoneRepository;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private SalesEstimateRepository salesEstimateRepository;

	@Autowired
	private ImsService imsService;

	@Autowired
	private ImsController imsController;

	@Autowired
	private InvoiceManagementService invoiceManagementService;

	@Autowired
	private InvoicePdfService invoicePdfService;

	@Autowired
	private InvoiceManagementController invoiceManagementController;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void brandFiltersAndZoneLinksGuardSoftDeletion() {
		String suffix = UUID.randomUUID().toString();
		CustomerGroup group = new CustomerGroup();
		group.setGroupName("Brand test group " + suffix);
		group.setIsActive(true);
		groupRepository.save(group);

		CompanyChain chain = new CompanyChain();
		chain.setGroup(group);
		chain.setChainName("Test company " + suffix.substring(0, 8));
		chain.setIsActive(true);
		companyChainRepository.save(chain);

		Brand brand = new Brand();
		brand.setBrandName("Test brand " + suffix.substring(0, 8));
		brand.setChain(chain);
		brand.setIsActive(true);
		brandRepository.save(brand);

		assertEquals(1, imsService.getActiveBrands(group.getGroupId(), chain.getChainId()).size());
		String page = imsController.brandsPage(group.getGroupId(), chain.getChainId(), null, null,
				new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-token"));
		assertTrue(page.contains("Sr. No."));
		assertTrue(page.contains(group.getGroupName()));
		assertTrue(page.contains(chain.getChainName()));
		assertTrue(page.contains(brand.getBrandName()));

		Zone zone = new Zone();
		zone.setZoneName("Test zone " + suffix.substring(0, 8));
		zone.setBrand(brand);
		zoneRepository.save(zone);
		Zone secondZone = new Zone();
		secondZone.setZoneName("Second test zone " + suffix.substring(0, 8));
		secondZone.setBrand(brand);
		zoneRepository.save(secondZone);

		assertFalse(imsService.deactivateBrand(brand.getBrandId()));
		assertTrue(brandRepository.findById(brand.getBrandId()).orElseThrow().getIsActive());

		zoneRepository.delete(zone);
		assertFalse(imsService.deactivateBrand(brand.getBrandId()));
		zoneRepository.delete(secondZone);
		assertTrue(imsService.deactivateBrand(brand.getBrandId()));
		assertFalse(brandRepository.findById(brand.getBrandId()).orElseThrow().getIsActive());
		assertTrue(imsService.getActiveBrands(null, null).stream()
				.noneMatch(activeBrand -> activeBrand.getBrandId().equals(brand.getBrandId())));
	}

	@Test
	@Transactional
	void zonesCanBeFilteredUpdatedAndSoftDeleted() {
		String suffix = UUID.randomUUID().toString();
		CustomerGroup group = new CustomerGroup();
		group.setGroupName("Zone test group " + suffix);
		group.setIsActive(true);
		groupRepository.save(group);

		CompanyChain chain = new CompanyChain();
		chain.setGroup(group);
		chain.setChainName("Zone company " + suffix.substring(0, 8));
		chain.setIsActive(true);
		companyChainRepository.save(chain);

		Brand brand = new Brand();
		brand.setBrandName("Zone brand " + suffix.substring(0, 8));
		brand.setChain(chain);
		brand.setIsActive(true);
		brandRepository.save(brand);

		imsService.createZone("Zone one", brand.getBrandId());
		Zone zone = imsService.getActiveZones(brand.getBrandId(), chain.getChainId(), group.getGroupId())
				.get(0);
		assertTrue(zone.getIsActive());
		assertTrue(zone.getCreatedAt() != null);
		assertTrue(zone.getUpdatedAt() != null);
		String page = imsController.zonesPage(brand.getBrandId(), chain.getChainId(), group.getGroupId(), null,
				new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-token"));
		assertTrue(page.contains("Total groups"));
		assertTrue(page.contains("Total companies / chains"));
		assertTrue(page.contains("Total brands"));
		assertTrue(page.contains("Total zones"));
		assertTrue(page.contains("Filter zones"));
		assertTrue(page.contains("Zone one"));
		assertTrue(page.contains(group.getGroupName()));

		imsService.updateZone(zone.getZoneId(), "Renamed zone", brand.getBrandId());
		assertEquals("Renamed zone", zoneRepository.findById(zone.getZoneId()).orElseThrow().getZoneName());
		assertTrue(imsService.getActiveZones(brand.getBrandId(), null, null).stream()
				.anyMatch(activeZone -> activeZone.getZoneId().equals(zone.getZoneId())));

		imsService.deactivateZone(zone.getZoneId());
		assertFalse(zoneRepository.findById(zone.getZoneId()).orElseThrow().getIsActive());
		assertTrue(imsService.getActiveZones(null, null, null).stream()
				.noneMatch(activeZone -> activeZone.getZoneId().equals(zone.getZoneId())));
	}

	@Test
	@Transactional
	void salesEstimateCapturesClientHierarchyAndCalculatesTotal() {
		String suffix = UUID.randomUUID().toString();
		CustomerGroup group = new CustomerGroup();
		group.setGroupName("Estimate group " + suffix.substring(0, 8));
		group.setIsActive(true);
		groupRepository.save(group);

		CompanyChain chain = new CompanyChain();
		chain.setGroup(group);
		chain.setChainName("Estimate company " + suffix.substring(0, 8));
		chain.setIsActive(true);
		companyChainRepository.save(chain);

		Brand brand = new Brand();
		brand.setBrandName("Estimate brand " + suffix.substring(0, 8));
		brand.setChain(chain);
		brand.setIsActive(true);
		brandRepository.save(brand);

		Zone zone = new Zone();
		zone.setZoneName("Estimate zone");
		zone.setBrand(brand);
		zone.setIsActive(true);
		zoneRepository.save(zone);

		Client client = new Client();
		client.setName("Estimate client");
		client.setEmail("estimate@example.com");
		clientRepository.save(client);

		SalesEstimate estimate = imsService.createSalesEstimate(client.getId(), chain.getChainId(),
				zone.getZoneId(), "Installation service", 3, new BigDecimal("125.50"),
				LocalDate.of(2026, 12, 15), "Deliver to site");
		SalesEstimate saved = salesEstimateRepository.findAllForDashboard().stream()
				.filter(item -> item.getEstimatedId().equals(estimate.getEstimatedId()))
				.findFirst().orElseThrow();

		assertEquals("Estimate client", saved.getClient().getName());
		assertEquals(chain.getChainId(), saved.getChain().getChainId());
		assertEquals(group.getGroupName(), saved.getGroupName());
		assertEquals(brand.getBrandName(), saved.getBrandName());
		assertEquals(zone.getZoneName(), saved.getZoneName());
		assertEquals(new BigDecimal("376.50"), saved.getTotalCost());
		assertEquals(LocalDate.of(2026, 12, 15), saved.getDeliveryDate());
		assertTrue(saved.getCreatedAt() != null);
		assertTrue(saved.getUpdatedAt() != null);
		String estimatesPage = imsController.salesEstimatesPage(null,
				new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-token"));
		assertTrue(estimatesPage.contains("Installation service"));
		assertTrue(estimatesPage.contains("Generate"));

		CompanyChain otherChain = new CompanyChain();
		otherChain.setGroup(group);
		otherChain.setChainName("Other estimate company " + suffix.substring(0, 8));
		otherChain.setIsActive(true);
		companyChainRepository.save(otherChain);
		assertThrows(ResponseStatusException.class, () -> imsService.createSalesEstimate(client.getId(),
				otherChain.getChainId(), zone.getZoneId(), "Invalid hierarchy", 1,
				BigDecimal.ONE, LocalDate.of(2026, 12, 15), "Must be rejected"));
	}

	@Test
	@Transactional
	void invoicesCopyEstimateDataAndSupportSearchPaymentPdfAndDeletion() throws IOException {
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		CustomerGroup group = new CustomerGroup();
		group.setGroupName("Invoice group " + suffix);
		group.setIsActive(true);
		groupRepository.save(group);

		CompanyChain chain = new CompanyChain();
		chain.setGroup(group);
		chain.setChainName("Invoice company " + suffix);
		chain.setIsActive(true);
		companyChainRepository.save(chain);

		Brand brand = new Brand();
		brand.setBrandName("Invoice brand " + suffix);
		brand.setChain(chain);
		brand.setIsActive(true);
		brandRepository.save(brand);

		Zone zone = new Zone();
		zone.setZoneName("Invoice zone " + suffix);
		zone.setBrand(brand);
		zone.setIsActive(true);
		zoneRepository.save(zone);

		Client client = new Client();
		client.setName("Invoice client " + suffix);
		client.setCompany("Invoice customer company " + suffix);
		client.setEmail("invoice-" + suffix + "@example.com");
		clientRepository.save(client);

		SalesEstimate estimate = imsService.createSalesEstimate(client.getId(), chain.getChainId(),
				zone.getZoneId(), "Invoice service " + suffix, 2, new BigDecimal("75.25"),
				LocalDate.of(2026, 12, 20), "Invoice delivery " + suffix);

		Invoice draft = invoiceManagementService.createDraft(estimate.getEstimatedId());
		assertTrue(draft.getInvoiceNo() >= 1000 && draft.getInvoiceNo() <= 9999);
		Invoice anotherDraft = invoiceManagementService.createDraft(estimate.getEstimatedId());
		assertNotEquals(draft.getInvoiceNo(), anotherDraft.getInvoiceNo());
		invoiceManagementService.deleteInvoice(anotherDraft.getId());
		assertEquals(estimate.getEstimatedId(), draft.getSalesEstimate().getEstimatedId());
		assertEquals(chain.getChainId(), draft.getChain().getChainId());
		assertEquals("Invoice service " + suffix, draft.getServiceDetails());
		assertEquals(new BigDecimal("150.50"), draft.getAmountPayable());
		assertEquals(new BigDecimal("150.50"), draft.getBalance());
		assertEquals(null, draft.getDateOfPayment());
		assertEquals(1, invoiceManagementService.findInvoices(draft.getInvoiceNo().toString()).size());
		assertEquals(1, invoiceManagementService.findInvoices(chain.getChainId().toString()).size());
		assertEquals(1, invoiceManagementService.findInvoices("Invoice customer company " + suffix).size());
		assertTrue(invoiceManagementController.reviewInvoice(draft.getId(),
				new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-token")).contains("Record payment"));

		Invoice paid = invoiceManagementService.recordPayment(draft.getId(), "paid-" + suffix + "@example.com");
		assertEquals(LocalDate.now(), paid.getDateOfPayment());
		assertEquals(BigDecimal.ZERO.setScale(2), paid.getBalance());
		byte[] pdf = invoicePdfService.createPdf(paid);
		assertTrue(new String(pdf, java.nio.charset.StandardCharsets.ISO_8859_1).startsWith("%PDF-"));
		try (PDDocument document = PDDocument.load(pdf)) {
			assertTrue(new PDFTextStripper().getText(document).contains("Invoice number: " + draft.getInvoiceNo()));
		}

		invoiceManagementService.updateEmail(draft.getId(), "updated-" + suffix + "@example.com");
		assertEquals("updated-" + suffix + "@example.com",
				invoiceManagementService.findInvoice(draft.getId()).getEmailId());
		invoiceManagementService.deleteInvoice(draft.getId());
		assertTrue(invoiceManagementService.findInvoices(draft.getInvoiceNo().toString()).isEmpty());
	}
}
