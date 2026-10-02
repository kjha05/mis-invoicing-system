package com.codeb.ims;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
	private ImsService imsService;

	@Autowired
	private ImsController imsController;

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
}
