/*
 * Copyright (C) 2013-2014 Paweł Jojczyk
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 */

package pl.jojczykp.bookstore.controllers.books;

import com.google.protobuf.ByteString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;
import pl.jojczykp.bookstore.commands.books.DownloadBookCommand;
import pl.jojczykp.bookstore.repositories.BooksRepository;
import pl.jojczykp.bookstore.transfers.BookTO;

import static com.google.protobuf.ByteString.copyFrom;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static pl.jojczykp.bookstore.testutils.matchers.HasBeanProperty.hasBeanProperty;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("classpath:spring/controllers-test-context.xml")
public class DownloadBookControllerComponentTest {

	private static final String EXISTING_ID = "7";
	private static final String NOT_EXISTING_ID = "13";
	private static final String NOT_PARSABLE_ID = "someString";
	private static final String TITLE = "Some Book Title";
	private static final String CONTENT_TYPE = "content/type";
	private static final ByteString CONTENT = copyFrom(new byte[] {9, 8, 7, 6, 5, 4, 3, 2, 1});

	private MockMvc mvcMock;
	private ResultActions mvcMockPerformResult;
	@Autowired private WebApplicationContext wac;

	@Autowired private BooksRepository booksRepository;

	@Mock private BookTO bookTO;

	@Before
	public void setUp() {
		mvcMock = webAppContextSetup(wac)
				.alwaysDo(print())
				.build();

		MockitoAnnotations.initMocks(this);
		reset(booksRepository);
	}

	@Test
	public void shouldDownloadBook() throws Exception {
		DownloadBookCommand command = downloadBookCommandWith(EXISTING_ID);
		givenBookReadFromRepositoryWith(EXISTING_ID, TITLE, CONTENT_TYPE, CONTENT);

		whenControllerDownloadPerformedWithCommand(command);

		thenExpectStatusIsOk();
		thenExpectHeadersFor(TITLE, CONTENT_TYPE, CONTENT);
		thenExpectContent(CONTENT);
	}

	@Test
	public void shouldFailDownloadingNotExistingBook() throws Exception {
		DownloadBookCommand command = downloadBookCommandWith(NOT_EXISTING_ID);
		givenBookReadFromRepositoryWith(EXISTING_ID, TITLE, CONTENT_TYPE, CONTENT);

		whenControllerDownloadPerformedWithCommand(command);

		thenExpectStatusIsNotFound();
		thenExpectViewName("exception");
		thenExpectCorrectExceptionCommandFor(NOT_EXISTING_ID);
	}

	@Test
	public void shouldFailDownloadingBookForNotParsableId() throws Exception {
		DownloadBookCommand command = downloadBookCommandWith(NOT_PARSABLE_ID);

		whenControllerDownloadPerformedWithCommand(command);

		thenExpectStatusIsNotFound();
		thenExpectViewName("exception");
		thenExpectCorrectExceptionCommandFor(NOT_PARSABLE_ID);
	}

	private DownloadBookCommand downloadBookCommandWith(String id) {
		DownloadBookCommand command = new DownloadBookCommand();
		command.setId(id);

		return command;
	}

	private void givenBookReadFromRepositoryWith(String id, String title, String contentType, ByteString content) {
		given(booksRepository.find(parseInt(id))).willReturn(bookTO);
		given(bookTO.getTitle()).willReturn(title);
		given(bookTO.getContentType()).willReturn(contentType);
		given(bookTO.getContent()).willReturn(content);
	}

	private void whenControllerDownloadPerformedWithCommand(DownloadBookCommand command) throws Exception {
		mvcMockPerformResult = mvcMock.perform(get("/books/download")
				.flashAttr("downloadBookCommand", command));
	}

	private void thenExpectStatusIsOk() throws Exception {
		mvcMockPerformResult.andExpect(status().isOk());
	}

	private void thenExpectStatusIsNotFound() throws Exception {
		mvcMockPerformResult.andExpect(status().isNotFound());
	}

	private void thenExpectHeadersFor(String title, String contentType, ByteString content) throws Exception {
		mvcMockPerformResult
			.andExpect(header().string("Content-Type", is(equalTo(contentType))))
			.andExpect(header().string("Content-Length", is(equalTo(Integer.toString(content.size())))))
			.andExpect(header().string("Content-Disposition", is(equalTo("attachment; filename=\"" + title + "\""))));
	}

	private void thenExpectContent(ByteString content) throws Exception {
		mvcMockPerformResult.andExpect(content().bytes(content.toByteArray()));
	}

	private void thenExpectViewName(String viewName) throws Exception {
		mvcMockPerformResult
				.andExpect(view().name(viewName));
	}

	private void thenExpectCorrectExceptionCommandFor(String id) throws Exception {
		mvcMockPerformResult
				.andExpect(model().attribute("exceptionCommand",
						hasBeanProperty("stackTraceAsString", not(isEmptyOrNullString()))))
				.andExpect(model().attribute("exceptionCommand",
						hasBeanProperty("message",
								is(equalTo(format("Content of book with id '%s' not found.", id))))
				));
	}

}
