import json
from datetime import datetime

class ResponseHandler():

    def __init__(self):
        print("Response handler started.")
        self.response_json_filepath = "response/response.json"

        # Load the JSON file containing all of the API responses.
        self.response_json = self.load_response_json()
        print("API responses imported from '{}'.".format(self.response_json_filepath))
        
        # Initialise the last refresh time with the current datetime.
        self.last_refresh = datetime.now()
        # This value indicates the number of seconds that need to have passed since the last refresh to trigger a new one.
        self.refresh_delay = 300

    def load_response_json(self):
        """Loads the JSON file containing all possible API responses (response.json)."""

        with open(self.response_json_filepath, "r") as response_json_file:
            data = json.load(response_json_file)
            return data['response']

    def get(self, category, tag, status_code=True, refresh=True):
        """Retrieves the requested response from response.json, indexing by category->tag."""
        
        # Update the currently available JSON responses from response.json if the refresh delay has elapsed.
        if refresh and (datetime.now() - self.last_refresh).total_seconds() > self.refresh_delay:
            self.response_json = self.load_response_json()
            self.last_refresh = datetime.now()

        # Retreive the relevant response.
        res = self.response_json[category][tag]
        if status_code:
            return res, res['status_code']
        else:
            return res