from flask import Flask
from flask import request
import werkzeug
werkzeug.cached_property = werkzeug.utils.cached_property
from flask_restplus import Api, Resource, reqparse
import random
import mysql.connector
import requests
import authentication as e
from datetime import datetime, timedelta
import string
from response import response
import traceback

# TODO: Figure out how to move the Resource classes into separate files.
# TODO: Implement config.json for global variables

# Create instance of ResponseHandler
r = response.ResponseHandler()

# Create Flask API components
app = Flask(__name__)
api = Api(app)

# TODO: move these queries inside their respective classes/methods
# Define SQL queries.
film_id_list_sql = "select film from film right join rating r using(film) left join film_genre fg using(film) left join genre g using(genre) where 1 "
film_imdb_id_list_sql = "select imdb_id from film right join rating r using(film) where 1 "
vote_insert_sql = "insert into vote (user, film, value) values (%s, %s, %s)"
relationship_insert_sql = "insert into relationship (requester, recipient, status) values (%s, %s, %s)"
usersearch_select_sql = "select * from user where username like %s and status = 'verified' and user != %s"
relationship_delete_sql = "update relationship set status = %s where requester = %s and recipient = %s"
relationship_get_sql = "select r.*, u1.username as 'recipient_username', u1.email as 'recipient_email', u2.email as 'requester_email', u2.username as 'requester_username' from relationship r left join user u1 on (r.recipient = u1.user) left join user u2 on (r.requester = u2.user) where (requester = %s or recipient = %s);"
genre_list_sql = "select value from genre"
rating_provider_list_sql = "select distinct(source) from rating"
min_max_release_year_runtime_sql = "select min(year), max(year), min(runtime), max(runtime) from film;"

class Vote(Resource):

    def post(self):
        token_check = validate_token(request)
        if token_check[0]['success']:
            """Add a user's positive/negative Vote to their list of Votes."""
            arg_keys = [i for i in request.args.keys()]
            film_id = 0
            user_id = 0
            value = -1
            res = {}

            for key in arg_keys:
                parameter = request.args.get(key)
                if key == "film":
                    film_id = parameter
                elif key == "user":
                    user_id = parameter
                elif key == "vote":
                    value = parameter

            # check if all vars are set
            if film_id == 0 or user_id == 0 or value == -1:
                return r.get("vote", "parameter_invalid")

            # Initialise database connection and cursor
            db, db_cursor = get_db()

            # TODO: POST method should check to see if vote already exists. If so, it should update existing record.

            db_cursor.execute(vote_insert_sql, (user_id, film_id, value,))
            db.commit()

            res = r.get("vote", "success")
            res[0]['user'] = user_id
            res[0]['film'] = film_id
            res[0]['vote'] = value

            # Close database connection and cursor
            teardown_db(db, db_cursor)
            return res
        else:
            return token_check

    # should update value of existing vote
    def update(self):
        """Changes the value of a user's Vote for a particular Film."""
        pass

    # Should remove existing entry for vote
    def delete(self):
        """Deletes a user's Vote entry for a particular Film."""
        pass

    def get(self):
        """Gets a list of all votes for a specific user."""
        pass

class RandomFilm(Resource):

    def get(self):
        """Get information for a random film."""
        # TODO: Add parameter to return films that user's friends have recently voted 'yes' on before returning random films
        # TODO: filter out films that have already been voted on
        token_check = validate_token(request)
        if token_check[0]['success']:
            # Initialise database stuff
            db, db_cursor = get_db()
            # set up params for sql query
            arg_keys = [i for i in request.args.keys()]
            sql_params = []
            request_sql = film_id_list_sql

            for key in arg_keys:
                parameter = request.args.get(key)
                if key == 'rating':
                    rating_list = parameter.split('],[')
                    # rating_list = parse_list_param(parameter)
                    # Append rating queries.
                    if len(rating_list) > 0 and rating_list[0] != '':
                        request_sql += "and ("
                        for x, rating in enumerate(rating_list):
                            rating_values = parse_list_param(rating)

                            # If the item is first in the list this statement avoids adding an 'or'.
                            if x == 0:
                                request_sql += f"(r.score {rating_values[2]} {rating_values[1]} and r.source = '{rating_values[0]}') "
                            else:
                                request_sql += f"or (r.score {rating_values[2]} {rating_values[1]} and r.source = '{rating_values[0]}') "

                            # If the item is last in the list, add a closing bracket.
                            if x == len(rating_list) - 1:
                                request_sql += ")"

                elif key == 'runtime':
                    request_sql += f"and runtime > {parameter} "
                    # sql_params.append(parameter)
                elif key == 'releaseYear':
                    year_list = parse_list_param(parameter)
                    request_sql += f"and (year between {year_list[0]} and {year_list[1]}) "
                elif key == 'genre':
                    genre_list = parse_list_param(parameter)
                    if len(genre_list) > 0 and genre_list[0] != '':
                        request_sql += "and g.value in ("
                        for x, genre in enumerate(genre_list):
                            request_sql += "'" + genre + "'"
                            if x != len(genre_list) - 1:
                                request_sql += ','
                        request_sql += ")"

            # get film id list
            if len(sql_params) > 0:
                db_cursor.execute(request_sql, tuple(sql_params))
            else:
                db_cursor.execute(request_sql)
            film_id_list = [int(film_id[0]) for film_id in db_cursor.fetchall()]

            # get random film id from list
            film_id = random.choice(film_id_list)

            # Close the database connection and cursor
            teardown_db(db, db_cursor)

            return get_film_info(film_id), 200
        else:
            return token_check

class Film(Resource):

    # TODO: this should query OMDb API to get hi-res poster URL, film description, starring actors, director, etc. as this class will be called when the user clicks for 'more info' in the app. 

    def get(self, id=0):

        token_check = validate_token(request)
        if token_check[0]['success']:
            """Fetch information for a specific Film."""
            # Initialise database connection and cursor
            db, db_cursor = get_db()

            # Get film ID list
            db_cursor.execute(film_id_list_sql)
            film_id_list =[int(film_id[0]) for film_id in db_cursor.fetchall()]

            if (id == 0):
                # Return error indicating that user need to specify a film id
                return r.get("film", "id_none")
            elif id in film_id_list:
                film_id = id
            else:
                return r.get("film", "not_found")

            # Close the database cursor and connection
            teardown_db(db, db_cursor)

            return get_film_info(film_id), 200
        else:
            return token_check

class User(Resource):

    def post(self):
        """Creates new user in database and logs them in."""
        arg_keys = [i for i in request.args.keys()]
        password = ""
        username = ""

        for key in arg_keys:
            parameter = request.args.get(key)
            if key == "username":
                username = parameter
            elif key == "password":
                password = parameter

        # Initialise database connection and cursor.
        db, db_cursor = get_db()

        # Check if username is already in DB.
        if username != "":
            # Get list of usernames matching the username provided in request.
            USERNAME_CHECK_QUERY = "select username from user where username = %s"
            db_cursor.execute(USERNAME_CHECK_QUERY, (username,))

            # Check if there were any users in the DB with the same username.
            if len(db_cursor.fetchall()) > 0:
                return r.get("user", "username_taken")
        else:
            return r.get("user", "username_none")

        # Check if the password parameter was set.
        if password == "":
            return r.get("user", "password_none")

        # Add new user to DB.
        ADD_USER_STATEMENT = "insert into user (username, password) values (%s, %s);"
        # print(e.hash_password(password))
        db_cursor.execute(ADD_USER_STATEMENT, (username, e.hash_password(password)))
        db.commit()

        # Get the new allocated user ID.
        SELECT_NEW_USER_ID = "select user, username, email from user where username = %s"
        db_cursor.execute(SELECT_NEW_USER_ID, (username,))
        rows = db_cursor.fetchall()[0]

        # Login the user by generating login token

        token = generate_token(rows[0])

        res = r.get("user", "create_success")
        res[0]["token"] = token
        res[0]["user"] = {
            "user_id": rows[0],
            "username": rows[1],
            "email": rows[2]
        }

        # Close database connection and cursor
        teardown_db(db, db_cursor)

        return res

class Login(Resource):

    def post(self):
        """Logs user in, generating new login token."""
        arg_keys = [i for i in request.args.keys()]
        password = ""
        username = ""

        for key in arg_keys:
            parameter = request.args.get(key)
            if key == "username":
                username = parameter
            elif key == "password":
                password = parameter

        if username == "":
            return r.get("authentication", "login_username_none")

        if password == "":
            return r.get("authentication", "login_password_none")

        # Initialise database connection
        db, db_cursor = get_db()

        # Get user's details from database.
        GET_USER_PASSWORD = "select user, password, username, email from user where username = %s"
        db_cursor.execute(GET_USER_PASSWORD, (username,))

        # If username already exists in db return fail response.
        rows = db_cursor.fetchall()
        if len(rows) == 0:
            return r.get("authentication", "login_credentials_invalid")

        # Verify user's provided password.
        if e.verify_password(rows[0][1], password):

            # Get new login token.
            token = generate_token(rows[0][0])

            # Add login token to response and return success.
            res = r.get("authentication", "login_success")
            res[0]['token'] = token
            res[0]['user'] = {
                "user_id": rows[0][0],
                "username": rows[0][2],
                "email": rows[0][3]
            }

            # Close database connection and cursor
            teardown_db(db, db_cursor)

            return res
        else:
            # TODO: A failed login should set any current tokens for the user to 'expired' (i.e. call 'logout' method, passing user id)
            return r.get("authentication", "login_credentials_invalid")

class UserSearch(Resource):

    def get(self):
        """Allows searching of other users with 'search' parameter."""
        token_check = validate_token(request)
        if token_check[0]['success']:
            # Initialise database connection and cursor.
            db, db_cursor = get_db()

            # Get the user ID of the current user using the token.
            # This allows the current user to be excluded from search results.
            current_user = get_userid_from_token(request.args.get("token"))

            arg_keys = [i for i in request.args.keys()]
            users = []
            search = ""

            for key in arg_keys:
                parameter = request.args.get(key)
                if key == "search":
                    search = parameter

            # Add wildcard characters (%) to the search string
            db_cursor.execute(usersearch_select_sql, ('%' + search + '%', current_user))
            users = db_cursor.fetchall()

            # Generate list of user dicts to be included in JSON response.
            user_dicts = []
            for user in users:
                user_dicts.append({
                    "user_id": user[0],
                    "username": user[1],
                    "email": user[2]
                    })

            res = r.get("usersearch", "success")
            res[0]["users"] = user_dicts

            # Close database connection and cursor
            teardown_db(db, db_cursor)

            return res
        else:
            return token_check

class Relationship(Resource):

    @api.doc(params={
        "user_id": "An int indicating the user ID for the user that relationships should be retrieved for.",
        "accepted_only": "A boolean indicating whether unconfirmed relationships should be returned."
    })
    def get(self):
        """Returns a list of relationships for the passed user ID."""
        token_check = validate_token(request)
        if token_check[0]['success']:
            # Initialise database connection and cursor
            db, db_cursor = get_db()

            arg_keys = [i for i in request.args.keys()]
            user_id = 0
            accepted_only = False

            # Get parameters
            for key in arg_keys:
                parameter = request.args.get(key)
                if key == "user_id":
                    user_id = parameter
                if key == "accepted_only":
                    accepted_only = parameter

            sql = relationship_get_sql

            if accepted_only:
                sql = sql + " and status = 'accepted'"

            db_cursor.execute(sql, (user_id, user_id))

            relationship_list = []
            relationship_records = db_cursor.fetchall()
            for relationship_record in relationship_records:
                relationship_list.append({
                    "requester": {
                        "user_id": relationship_record[1],
                        "username": relationship_record[8],
                        "email": relationship_record[7]
                    },
                    "recipient": {
                        "user_id": relationship_record[2],
                        "username": relationship_record[5],
                        "email": relationship_record[6]
                    },
                    "status": relationship_record[3]
                })

            res = r.get("relationship", "get_success")
            res[0]['relationship'] = relationship_list

            # Close database connection and cursor
            teardown_db(db, db_cursor)

            return res
        else:
            return token_check

    # Creates a new entry in the Relationship table (takes a combination of usernames or user_id)
    @api.doc(params={
        "requester": "int representing ID of relationship request requester. It should be valid foreign key of user(user).",
        "recipient": "int representing ID of relationship request recipient. It should be valid foreign key of user(user)."
    })
    def post(self):
        """Creates new Relationship between two users or updates an existing one."""

        token_check = validate_token(request)
        if token_check[0]['success']:
            # Initialise database connection and cursor
            db, db_cursor = get_db()

            arg_keys = [i for i in request.args.keys()]
            requester = -1
            recipient = -1

            # Get parameters
            for key in arg_keys:
                parameter = request.args.get(key)
                if key == "requester":
                    requester = int(parameter)
                if key == "recipient":
                    recipient = int(parameter)

            if requester < 0 or recipient < 0:
                return r.get("relationship", "user_invalid")

            # Check if relationship already exists in DB.
            db_cursor.execute("select * from relationship where requester = %s and recipient = %s", (requester, recipient))
            existing_relationships = db_cursor.fetchall()

            try:
                if len(existing_relationships) == 0:
                    # Create a new relationship.
                    db_cursor.execute(relationship_insert_sql, (requester, recipient, 'requested'))
                    db.commit()
                    # Return successful response containing relationship information.
                    res = r.get("relationship", "create_success")
                    res[0]['relationship']['requester'] = requester
                    res[0]['relationship']['recipient'] = recipient
                    res[0]['relationship']['status'] = "requested"
                    return res
                else:
                    return r.get("relationship", "already_exists")
            except mysql.connector.errors.IntegrityError:
                res = r.get("general", "500")
                res[0]['error'] = traceback.format_exc()
                return res
            finally:
                teardown_db(db, db_cursor)
        else:
            return token_check

    # REVIEW: this whole comment needs updating following method name change
    # TODO: update this method in line with it's new purpose
#     # Marks the relationship as either 'recanted' or 'rejected', depending on
#     # the value of the 'status' parameter.
#     #
#     # 'recanted' when the requester has cancelled the relationship request
#     # 'rejected' when the recipient has rejected the relationship request
#     def update(self):
#         """Updates status of relationship between two users."""

#         # TODO: update to use new token validation method and return error msg on fail
#         if validate_token(request):
#             arg_keys = [i for i in request.args.keys()]

#             # Initialise parameter variables.
#             requester = ""
#             recipient = ""
#             status = ""

#             # Get parameters from request.
#             for key in arg_keys:
#                 parameter = request.args.get(key)
#                 if key == "requester":
#                     requester = parameter
#                 if key == "recipient":
#                     recipient = parameter
#                 if key == "status":
#                     status = parameter

#             # Check that status parameter was provided and valid.
#             if status == "":
#                 return r.get("relationship", "update_success")
#             # FIXME: this needs to pull the available statuses from the DB and check if passed status is among them
#             elif status != 'recanted' and status != 'rejected':
#                 return r.get("relationship", "status_invalid")

#             # Execute SQL to update relationship status.
#             db_cursor.execute(relationship_delete_sql, (status, requester, recipient))
#             db.commit();

#             res = r.get("relationship", "update_success")
#             res[0]['relationship']['requester'] = requester
#             res[0]['relationship']['recipient'] = recipient
#             res[0]['relationship']['status'] = status
#             return res

class Logout(Resource):

    def post(self):
        """Logs user out, disabling existing login tokens."""
        if "user_id" in request.args.keys():
            user_id = request.args.get("user_id")
            return logout(user_id)
        else:
            return r.get("authentication", "logout_userid_none")

class Filter(Resource):

    def post(self):
        """Returns a list of available settings for a filter.
        Returns a list of ratings providers (IMDb, Rotten Tomatoes, etc.), genres, the maximum/minimum year of a film's release, and the maximum runtime. Takes no parameters.
        """
        token_check = validate_token(request)
        if token_check[0]['success']:
            # Wrap whole code in try-except to catch any weird errors.
            try:
                # Initialise database connection and cursor
                db, db_cursor = get_db()

                # Define the base Filter dict.
                filter = {
                    "rating" : [

                    ],
                    "genre": [

                    ],
                    "year": {
                        "max": 0,
                        "min": 0
                    },
                    "runtime": {
                        "max": 0,
                        "min": 0
                    }
                }

                # # Fetch filter information from the DB
                # Genres
                db_cursor.execute(genre_list_sql)
                filter["genre"] = [x[0] for x in db_cursor.fetchall()]

                # Rating information
                db_cursor.execute(rating_provider_list_sql)
                filter["rating"] = [y[0] for y in db_cursor.fetchall()]

                # Max/min release year and runtime
                # Output is ordered as follows: min(year), max(year), min(runtime), max(runtime)
                db_cursor.execute(min_max_release_year_runtime_sql)
                max_min_release_year_runtime = list(db_cursor.fetchall()[0])
                filter["year"]["min"] = max_min_release_year_runtime[0]
                filter["year"]["max"] = max_min_release_year_runtime[1]
                filter["runtime"]["min"] = max_min_release_year_runtime[2]
                filter["runtime"]["max"] = max_min_release_year_runtime[3]

                # Set the 'Filter' field in the response JSON.
                res = r.get("filter", "success")
                res[0]["filter"] = filter
                return res

            except Exception:
                res = r.get("general", "500")
                res[0]['error'] = traceback.format_exc()
                return res
            finally:
                teardown_db(db, db_cursor)
            
        else:
            return token_check

def get_db():
    """Returns database connection and cursor objects. 
    Should be run when any API method requiring database access is called.
    """
    conn = mysql.connector.connect(
        host="localhost",
        user="hamster",
        password="Untoward.Tomorrow.Ass883",
        database="hamster"
    )
    cursor = conn.cursor()
    return conn, cursor

def teardown_db(conn, cursor):
    cursor.close()
    conn.close()

def get_userid_from_token(token):
    # Initialise database connection and cursor
    db, db_cursor = get_db()

    db_cursor.execute("select user from login where token = %s and status = 'active'", (token,))
    results = db_cursor.fetchall()

    # Close database connection
    teardown_db(db, db_cursor)
    return results[0][0]

def generate_token(user):
    """Generates and returns login token."""

    # Creates random 64 character base64 string token and passes value to DB.
    token = ''.join(random.SystemRandom().choice(string.ascii_uppercase +
                        string.ascii_lowercase + string.digits) for _ in range(64)
                    )
    # Calculate expiration time (one week).
    expiry_date = (datetime.now() + timedelta(days=7)).strftime("%Y-%m-%d %H:%M:%S")

    ## Set all existing tokens for user to 'expired'.
    # Find active tokens for user.
    db, db_cursor = get_db()
    SELECT_USER_TOKENS = "update login set status = 'expired' where user = %s"
    db_cursor.execute(SELECT_USER_TOKENS, (user,))

    # Add the new active token to the login table in DB.
    INSERT_TOKEN_SQL = "insert into login (user, token, expiry_date, status) values(%s, %s, %s, %s)"
    db_cursor.execute(INSERT_TOKEN_SQL, (user, token, expiry_date, 'active'))
    db.commit()

    # Close the database cursor and connection
    teardown_db(db, db_cursor)

    return token

def validate_token(request):
    """Takes request and checks if token parameter exists and is valid."""
    if "token" in request.args.keys():
        token = request.args.get("token")

        # if token != None and token != "":
        if token not in [None, ""]:
            # Initialise database stuff
            db, db_cursor = get_db()

            # Checks provided token against DB to see if it is still valid.

            # Find all tokens with matching value in DB.
            SELECT_MATCHING_TOKEN = "select * from login where token = %s"
            db_cursor.execute(SELECT_MATCHING_TOKEN, (token,))
            tokens = db_cursor.fetchall()

            # If length of response is zero the token is not valid as it doesn't existing in the DB's login
            if len(tokens) != 0:
                # Check the status of the token, returning 'expired' response if it is not marked as active.
                if tokens[0][3] == 'active':
                    return r.get("authentication", "token_valid")
                else:
                    return r.get("authentication", "token_expired")
            else:
                return r.get("authentication", "token_invalid")
        else:
            return r.get("authentication", "token_invalid")
    else:
        return r.get("authentication", "token_none")

def get_film_info(film_id):

    if isinstance(film_id, int):
        film_info_sql = "select film, title, runtime, r.source, r.score, r.score_text, g.value, imdb_id, year from film " \
        "left join rating r using(film) " \
        "left join film_genre fg using(film) " \
        "left join genre g using(genre) " \
        "where film = %s "
    elif isinstance(film_id, str):
        film_info_sql = "select film, title, runtime, r.source, r.score, r.score_text, g.value, imdb_id, year from film " \
        "left join rating r using(film) " \
        "left join film_genre fg using(film) " \
        "left join genre g using(genre) " \
        "where imdb_id = %s "

    # Initialise database connection and cursor
    db, db_cursor = get_db()

    # get film info from db using said id
    db_cursor.execute(film_info_sql, (film_id,))
    film_info = db_cursor.fetchall()
    # print(film_info)

    # initialise variables for JSON response
    film_dict = {}
    film = {}
    genre = []
    rating = []
    # loop over film info - extract genre and rating info
    for x, row in enumerate(film_info):
        # on first loop add film info
        if x == 0:
            film = {
                "id": row[0],
                "title": row[1],
                "runtime": row[2],
                "imdb_id": row[7],
                "releaseYear": row[8]
            }

        # add rating if it doesn't exist already

        new_rating = {
        "source": row[3],
        "score": row[4],
        "score_text": row[5]
        }
        if new_rating not in rating and row[3] != None:
            # rating[row[3]] = new_rating
            rating.append(new_rating)

        # add genres to array
        if row[6] not in genre:
            genre.append(row[6])

    # finish film dict
    film["genre"] = genre
    film["rating"] = rating

    # add film to response dict
    film_dict = {
        "film": film,
        "success": True,
        "status_code": 200
    }

    print("Got random film {} - {}.".format(
        film_dict["film"]["id"],
        film_dict["film"]["title"]
    ))

    return film_dict

def logout(user_id):
    """Disable the tokens for the passed user ID. Returns response dict from ResponseHandler."""

    # TODO: actually implement logging out - disable active API tokens for passed user_id.
    # Initialise database connection and cursor.
    db, db_cursor = get_db()

    # Update 'active' tokens, setting them to 'expired'.
    db_cursor.execute("UPDATE login SET status = 'expired' WHERE user = %s AND status = 'active'", (user_id,))
    db.commit()

    # Close database connection and cursor.
    teardown_db(db, db_cursor)

    return r.get("authentication", "logout_success")

# Add entry to audit table indicating the API endpoint, HTTP method, time of request,
# error message (if needed) and status of the request (successful, failed, etc.)
#
# Takes request as an argument.
#
# Should be run on a caught exception.
def log(request):
    pass

def parse_list_param(parameter):
    return parameter.replace('[', '').replace(']', '').split(',')


# Add Resources to API instance.
api.add_resource(Film, "/film/<int:id>", methods=['GET'])
api.add_resource(Filter, "/filter", methods=['POST'])
api.add_resource(Login, "/login", methods=['POST'])
api.add_resource(Logout, "/logout", methods=['POST'])
api.add_resource(RandomFilm, "/film", methods=['GET'])
api.add_resource(Relationship, "/relationship", methods=['GET', 'POST', 'DELETE'])
api.add_resource(User, "/user", methods=['POST', 'GET'])
api.add_resource(UserSearch, "/usersearch", methods=['GET'])
api.add_resource(Vote, "/vote", methods=['POST'])

if __name__ == '__main__':
    app.run(debug=True)
