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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.jojczykp.bookstore.controllers;

public abstract class SecurityConsts {

	static final String URL_PAGE_LOGIN = "/security/loginPage";
	static final String URL_PAGE_LOGOUT = "/security/logoutPage";

	static final String LOGIN_VIEW = "loginPage";

	static final String LOGOUT_ATTRIBUTE = "loggedOut";
}
