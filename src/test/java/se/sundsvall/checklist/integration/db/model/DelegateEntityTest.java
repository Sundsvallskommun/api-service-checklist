package se.sundsvall.checklist.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

class DelegateEntityTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(DelegateEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters()));
	}
}
