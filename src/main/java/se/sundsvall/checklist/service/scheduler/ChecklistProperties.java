package se.sundsvall.checklist.service.scheduler;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("checklist")
record ChecklistProperties(List<String> managedMunicipalityIds) {
}
