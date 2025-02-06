package se.sundsvall.checklist.service;

import static org.apache.commons.lang3.math.NumberUtils.isCreatable;

import java.util.TreeMap;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representation of an organization in the municipality.
 * Also contains a mapper for creating the OrganizationTree from the org-string in Employee.
 */
@Getter
@Setter
public class OrganizationTree {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationTree.class);

	private TreeMap<Integer, OrganizationLine> tree = new TreeMap<>();

	public void addOrg(OrganizationLine org) {
		tree.put(org.getLevel(), org);
	}

	@Getter
	@Setter
	@Builder(setterPrefix = "with")
	public static class OrganizationLine {
		private int level; // Duplicated but nice to have.
		private String orgId;
		private String orgName;
	}

	/**
	 * <pre>
	 * Maps the following (example) organizational string from Employee:
	 * "2|28|Kommunstyrelsekontoret¤3|440|KS KSK Avdelningar¤4|2835|KS Avd Digital Transformation¤5|2834|KS Avd Digital Transformation¤6|2836|KS Avd Digital Transformation"
	 * The first number is the level, the second is the orgId and the third is the orgName.
	 * </pre>
	 *
	 * @param  organizationString The org-string to parse
	 * @return                    An {@link OrganizationTree} object
	 */
	public static OrganizationTree map(String organizationString) {

		LOGGER.info("Trying to parse into an organization tree: {}", organizationString);

		final var organizationTree = new OrganizationTree();

		final var split = organizationString.split("¤");

		for (final String line : split) {
			final var split1 = line.split("\\|");
			final var level = split1[0];
			final var orgId = split1[1];
			final var orgName = split1[2];

			if (isCreatable(level)) {
				organizationTree.addOrg(OrganizationTree.OrganizationLine.builder()
					.withLevel(Integer.parseInt(level))
					.withOrgId(orgId)
					.withOrgName(orgName)
					.build());
			}
		}

		return organizationTree;
	}
}
