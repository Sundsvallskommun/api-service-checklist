package se.sundsvall.checklist.service;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.sundsvall.checklist.service.OrganizationTree.OrganizationLine;
import se.sundsvall.dept44.test.annotation.resource.Load;
import se.sundsvall.dept44.test.extension.ResourceLoaderExtension;

@ExtendWith(ResourceLoaderExtension.class)
class OrganizationTreeTest {

	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Test
	void testParseOrganizationString(@Load(value = "junit/full-org-tree.json", as = Load.ResourceType.STRING) String wantedOrgTree) {
		final var organizationString = "2|9271|Sundsvall Energi¤3|9246|SEAB Produktion Gemensamt¤4|9251|SEAB Enhet drift¤5|9279|SEAB Enhet drift¤6|9250|SEAB Enhet drift";
		OrganizationTree map = OrganizationTree.map(1, organizationString);

		assertJsonEquals(gson.toJson(map), wantedOrgTree);
	}

	@Test
	void testParseOrganizationStringWithoutLevel(@Load(value = "junit/partial-org-tree.json", as = Load.ResourceType.STRING) String wantedOrgTree) {
		// 9246 is missing value for level
		final var organizationString = "2|9271|Sundsvall Energi¤|9246|SEAB Produktion Gemensamt¤4|9251|SEAB Enhet drift¤5|9279|SEAB Enhet drift¤6|9250|SEAB Enhet drift";
		OrganizationTree map = OrganizationTree.map(1, organizationString);

		assertJsonEquals(gson.toJson(map), wantedOrgTree);
	}

	@Test
	void testSortOrderOnTree() {
		final var organizationString = "2|12|Level 2¤3|121|Level 3¤4|1212|Level 4¤5|12121|Level 5¤6|121212|Level 6¤7|1212121|Level 7¤8|12121212|Level 8¤9|121212121|Level 9¤10|1212121212|Level 10";
		OrganizationTree map = OrganizationTree.map(1, organizationString);

		assertThat(map.getTree().descendingMap().values().stream().map(OrganizationLine::getLevel).toList())
			.containsExactly(10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
	}
}
