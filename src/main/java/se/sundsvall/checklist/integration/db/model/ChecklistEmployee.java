package se.sundsvall.checklist.integration.db.model;

/**
 * Projection holding the identity of an employee owning a checklist.
 *
 * Used when processing all checklists within a municipality, as loading the full {@link EmployeeChecklistEntity} graph
 * for every employee keeps a large amount of entities in memory for the duration of the processing.
 *
 * @param id        the id of the employee
 * @param firstName the first name of the employee
 * @param lastName  the last name of the employee
 * @param username  the username of the employee
 */
public record ChecklistEmployee(String id, String firstName, String lastName, String username) {
}
