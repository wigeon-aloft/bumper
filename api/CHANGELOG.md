# Changelog
All notable changes to this project will be documented in this file.

Entries are ordered by descending version number.

## [0.0.4] - 2021-03-11

### Database Access Fixes
- Database connection and cursor objects are now created when the an API method is called and not just when the API starts (this prevents stale data being fetched).
- Added new method `get_db` for this purpose.

### Logout
- `logout` method now actually logs the user out, disabling all of their active tokens in the database.

### Other Fixes
- Removed token authentication from User POST method as this was preventing new users from being created.

## [0.0.3] - 2021-03-30

### Method in the Madness
- Reformatted `hamster.py` so method definitions are grouped below class definitions.

## [0.0.2] - 2021-03-30

### Revamped API Responses
- Created `response.json` file for storing API responses:
    - Moved existing responses to this file and categorised them by endpoint.
- Created `response` module containing the `ResponseHandler` class:
    - `ResponseHandler` parses the content of the `response.json` file and generates a 2-tuple containing a Dictionary for the JSON response and an int for the status code.
    - The API has been updated to utilise this new ResponseHandler.

### Care to Comment?
- Improved code comments.
- Removed obsolete response code.
- Consolidated `authenticate_token()` and `validate_request_token()` into one new method `validate_token()`.

## [0.0.1] - 2021-03-29

- Removed the `IMDbFilm` class as it has been superseded by the `Film` class.
- Updated `UserSearch.get()` so that the requesting user is not returned in the search results.