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

package pl.jojczykp.bookstore.consts;

public abstract class BooksConsts {

	public static final String URL_ACTION_CREATE = "/books/create";
	public static final String URL_ACTION_LIST = "/books/display";
	public static final String URL_ACTION_UPDATE = "/books/update";
	public static final String URL_ACTION_DELETE = "/books/delete";

	public static final String URL_ACTION_SORT = "/books/sort";
	public static final String URL_ACTION_GO_TO_PAGE = "/books/goToPage";
	public static final String URL_ACTION_SET_PAGE_SIZE = "/books/setPageSize";

	public static final String CREATE_BOOK_COMMAND = "createBookCommand";
	public static final String DISPLAY_BOOKS_COMMAND = "displayBooksCommand";
	public static final String UPDATE_BOOK_COMMAND = "updateBookCommand";
	public static final String DELETE_BOOKS_COMMAND = "deleteBooksCommand";
	public static final String CHANGE_PAGER_COMMAND = "changePagerCommand";

	public static final String DISPLAY_BOOKS_VIEW = "books";

	public static final String EXCEPTION_VIEW = "exception";
}