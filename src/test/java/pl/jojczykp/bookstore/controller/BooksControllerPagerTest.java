package pl.jojczykp.bookstore.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.validation.Errors;
import org.springframework.web.context.WebApplicationContext;
import pl.jojczykp.bookstore.command.BooksCommand;
import pl.jojczykp.bookstore.repository.BookRepository;
import pl.jojczykp.bookstore.utils.PageSorterColumn;
import pl.jojczykp.bookstore.utils.PageSorterDirection;
import pl.jojczykp.bookstore.validators.BooksSetPageSizeValidator;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static pl.jojczykp.bookstore.testutils.matchers.HasBeanProperty.hasBeanProperty;
import static pl.jojczykp.bookstore.testutils.matchers.MessagesControllerTestUtils.thenExpectErrorOnlyMessage;
import static pl.jojczykp.bookstore.testutils.matchers.MessagesControllerTestUtils.thenExpectInfoOnlyMessage;
import static pl.jojczykp.bookstore.utils.PageSorterColumn.BOOK_TITLE;
import static pl.jojczykp.bookstore.utils.PageSorterDirection.ASC;
import static pl.jojczykp.bookstore.utils.PageSorterDirection.DESC;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration({
		"file:src/main/webapp/WEB-INF/mvc-dispatcher-servlet.xml",
		"classpath:spring/config-test-context.xml",
		"classpath:spring/repository-mock-context.xml",
		"classpath:spring/book-command-validator-mock-context.xml",
		"classpath:spring/books-command-factory-mock-context.xml"
})
public class BooksControllerPagerTest {

	private static final String BOOKS_COMMAND = "booksCommand";

	private static final String URL_ACTION_SORT = "/books/sort";
	private static final String URL_ACTION_GO_TO_PAGE = "/books/goToPage";
	private static final String URL_ACTION_SET_PAGE_SIZE = "/books/setPageSize";

	private static final int PAGE_NUMBER = 2;
	private static final int PAGE_SIZE = 13;
	private static final int PAGES_COUNT = 4;
	private static final PageSorterColumn SORT_COLUMN = BOOK_TITLE;
	private static final PageSorterDirection SORT_DIRECTION = ASC;
	private static final String VALIDATOR_ERROR_MESSAGE = "Negative or zero page size is not allowed.";

	private MockMvc mvcMock;
	private ResultActions mvcMockPerformResult;
	@Autowired private BookRepository bookRepository;
	@Autowired private BooksSetPageSizeValidator booksSetPageSizeValidatorMock;
	@Autowired private WebApplicationContext wac;

	@Value("${view.books.defaultPageSize}") private int defaultPageSize;

	@Before
	public void setUp() {
		given(bookRepository.totalCount()).willReturn(PAGES_COUNT * PAGE_SIZE - 2);
		mvcMock = webAppContextSetup(wac).build();
		reset(booksSetPageSizeValidatorMock);
	}

	@Test
	public void shouldSort() throws Exception {
		final PageSorterColumn sortColumn = BOOK_TITLE;
		final PageSorterDirection sortDirection = DESC;
		final BooksCommand command = aSortBooksCommand(sortColumn, sortDirection);

		whenUrlActionPerformedWithCommand(URL_ACTION_SORT, command);

		assertThatSortedBy(sortColumn, sortDirection);
		thenExpectHttpRedirectWith(command);
	}

	@Test
	public void shouldSetPageSize() throws Exception {
		final int pageSize = 9;
		final BooksCommand command = aPageSizeBooksCommand(pageSize);

		whenUrlActionPerformedWithCommand(URL_ACTION_SET_PAGE_SIZE, command);

		thenExpectValidationInvoked();
		thenExpectInfoOnlyMessage(mvcMockPerformResult, "Page size changed.");
		thenExpectPageSizeSetTo(pageSize);
		thenExpectHttpRedirectWith(command);
	}

	@Test
	public void shouldSetPageSizeFailOnNotPositiveValue() throws Exception {
		final int pageSize = -1;
		final BooksCommand command = aPageSizeBooksCommand(pageSize);
		givenNegativeValidation();

		whenUrlActionPerformedWithCommand(URL_ACTION_SET_PAGE_SIZE, command);

		thenExpectValidationInvoked();
		thenExpectErrorOnlyMessage(mvcMockPerformResult, VALIDATOR_ERROR_MESSAGE);
		thenExpectPageSizeSetTo(defaultPageSize);
		thenExpectHttpRedirectWith(command);
	}

	@Test
	public void shouldGoToPage() throws Exception {
		final int pageNumber = 3;
		final BooksCommand command = aPageNumberBooksCommand(pageNumber);

		whenUrlActionPerformedWithCommand(URL_ACTION_GO_TO_PAGE, command);

		assertThatScrolledToPage(pageNumber);
		thenExpectHttpRedirectWith(command);
	}

	private void whenUrlActionPerformedWithCommand(String action, BooksCommand bookCommand) throws Exception {
		mvcMockPerformResult = mvcMock.perform(post(action)
				.flashAttr(BOOKS_COMMAND, bookCommand));
	}

	private BooksCommand aPageNumberBooksCommand(int pageNumber) {
		return aBooksCommand(pageNumber, PAGE_SIZE, PAGES_COUNT, SORT_COLUMN, SORT_DIRECTION);
	}

	private BooksCommand aSortBooksCommand(PageSorterColumn sortColumn, PageSorterDirection sortDirection) {
		return aBooksCommand(PAGE_NUMBER, PAGE_SIZE, PAGES_COUNT, sortColumn, sortDirection);
	}

	private BooksCommand aPageSizeBooksCommand(int pageSize) {
		return aBooksCommand(PAGE_NUMBER, pageSize, PAGES_COUNT, SORT_COLUMN, SORT_DIRECTION);
	}

	private BooksCommand aBooksCommand(int pageNumber, int pageSize, int pagesCount,
									   PageSorterColumn sortColumn, PageSorterDirection sortDirection) {
		BooksCommand command = new BooksCommand();
		command.getPager().setPageNumber(pageNumber);
		command.getPager().setPageSize(pageSize);
		command.getPager().setPagesCount(pagesCount);
		command.getPager().getSorter().setColumn(sortColumn);
		command.getPager().getSorter().setDirection(sortDirection);

		return command;
	}

	private void givenNegativeValidation() {
		doAnswer(validationError())
				.when(booksSetPageSizeValidatorMock).validate(anyObject(), any(Errors.class));
	}

	private Answer<Void> validationError() {
		return new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) {
				Errors errors = (Errors) invocation.getArguments()[1];
				errors.rejectValue("pager.pageSize", "pager.pageSize.notPositive", VALIDATOR_ERROR_MESSAGE);
				return null;
			}
		};
	}

	private void thenExpectValidationInvoked() {
		verify(booksSetPageSizeValidatorMock).validate(anyObject(), any(Errors.class));
	}

	private void assertThatScrolledToPage(int pageNumber) throws Exception {
		mvcMockPerformResult
				.andExpect(status().isFound())
				.andExpect(flash().attribute(BOOKS_COMMAND,
						hasBeanProperty("pager.pageNumber", equalTo(pageNumber))))
				.andExpect(flash().attribute(BOOKS_COMMAND,
						hasBeanProperty("pager.pageSize", equalTo(PAGE_SIZE))))
				.andExpect(flash().attribute(BOOKS_COMMAND,
						hasBeanProperty("pager.pagesCount", equalTo(PAGES_COUNT))));
	}

	private void thenExpectPageSizeSetTo(int pageSize) throws Exception {
		mvcMockPerformResult
				.andExpect(status().isFound())
				.andExpect(flash().attribute(BOOKS_COMMAND,
						hasBeanProperty("pager.pageSize", equalTo(pageSize))));
	}

	private void assertThatSortedBy(PageSorterColumn sortColumn, PageSorterDirection sortDirection) throws Exception {
		mvcMockPerformResult
				.andExpect(status().isFound())
				.andExpect(flash().attribute(BOOKS_COMMAND,
						hasBeanProperty("pager.sorter.column", equalTo(sortColumn))))
				.andExpect(flash().attribute(BOOKS_COMMAND,
						hasBeanProperty("pager.sorter.direction", equalTo(sortDirection))));
	}

	private void thenExpectHttpRedirectWith(BooksCommand command) throws Exception {
		mvcMockPerformResult
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/books/read"))
				.andExpect(flash().attribute("booksCommand", sameInstance(command)));
	}

}
