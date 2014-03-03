package pl.jojczykp.bookstore.controller;

import org.hibernate.ObjectNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;
import pl.jojczykp.bookstore.command.BookCommand;
import pl.jojczykp.bookstore.command.BooksCommand;
import pl.jojczykp.bookstore.repository.BooksRepository;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static pl.jojczykp.bookstore.testutils.controller.MessagesControllerTestUtils.thenExpectInfoOnlyFlashMessages;
import static pl.jojczykp.bookstore.testutils.controller.MessagesControllerTestUtils.thenExpectWarnOnlyFlashMessages;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("classpath:spring/controllers-test-context.xml")
public class BooksControllerDeleteTest {

	private static final int EXISTING_ID_1 = 3;
	private static final int EXISTING_ID_2 = 7;
	private static final int NOT_EXISTING_ID = 98;

	private MockMvc mvcMock;
	private ResultActions mvcMockPerformResult;
	@Autowired private BooksRepository booksRepository;
	@Autowired private WebApplicationContext wac;

	@Captor private ArgumentCaptor<Integer> idOfBookToRemove;

	@Before
	public void setUp() {
		mvcMock = webAppContextSetup(wac).build();
		MockitoAnnotations.initMocks(this);
		givenExceptionOnDeletingNotExistingId();
	}

	@Test
	public void shouldDeleteExistingBooks() throws Exception {
		final BooksCommand command = aCommandToRemoveByIds(EXISTING_ID_1, EXISTING_ID_2);

		whenControllerDeletePerformedWithCommand(command);

		thenExpectDeleteInvokedOnRepository(EXISTING_ID_1, EXISTING_ID_2);
		thenExpectInfoOnlyFlashMessages(mvcMockPerformResult, "Object deleted.", "Object deleted.");
		thenExpectHttpRedirectWith(command);
	}

	@Test
	public void shouldFailOnDeletingNotExistingBook() throws Exception {
		final BooksCommand command = aCommandToRemoveByIds(NOT_EXISTING_ID);

		whenControllerDeletePerformedWithCommand(command);

		thenExpectDeleteInvokedOnRepository(NOT_EXISTING_ID);
		thenExpectWarnOnlyFlashMessages(mvcMockPerformResult, "Object already deleted.");
		thenExpectHttpRedirectWith(command);
	}

	private void givenExceptionOnDeletingNotExistingId() {
		reset(booksRepository);
		doThrow(ObjectNotFoundException.class).when(booksRepository).delete(NOT_EXISTING_ID);
	}

	private BooksCommand aCommandToRemoveByIds(int... ids) {
		BooksCommand command = new BooksCommand();
		for (int id : ids) {
			command.getBooks().add(aCommandToDeleteBookWithId(id));
		}

		return command;
	}

	private BookCommand aCommandToDeleteBookWithId(int id) {
		BookCommand bookCommand = new BookCommand();
		bookCommand.setChecked(true);
		bookCommand.setId(id);

		return bookCommand;
	}

	private void whenControllerDeletePerformedWithCommand(BooksCommand command) throws Exception {
		mvcMockPerformResult = mvcMock.perform(post("/books/delete")
				.flashAttr("booksCommand", command));
	}

	private void thenExpectDeleteInvokedOnRepository(Integer... ids) {
		verify(booksRepository, times(ids.length)).delete(idOfBookToRemove.capture());
		assertThat(idOfBookToRemove.getAllValues(), hasItems(ids));
		verifyNoMoreInteractions(booksRepository);
	}

	private void thenExpectHttpRedirectWith(BooksCommand command) throws Exception {
		mvcMockPerformResult
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/books/read"))
				.andExpect(flash().attribute("booksCommand", sameInstance(command)));
	}

}
