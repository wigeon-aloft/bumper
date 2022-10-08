import hashlib
import binascii
import os

# Returns hashed password.
def hash_password(password):
    salt = hashlib.sha256(os.urandom(60)).hexdigest().encode('ascii')
    pwdhash = hashlib.pbkdf2_hmac('sha512', password.encode('utf-8'),
                                    salt, 100000)
    pwdhash = binascii.hexlify(pwdhash)
    return (salt + pwdhash).decode('ascii')

# Takes stored password (hash stored in db) and provided password (plaintext) and verifies if they match.
def verify_password(stored_password, provided_password):
    salt = stored_password[:64]
    stored_password = stored_password[64:]
    pwdhash = hashlib.pbkdf2_hmac('sha512', provided_password.encode('utf-8'),
                                    salt.encode('ascii'),
                                    100000)
    pwdhash = binascii.hexlify(pwdhash).decode('ascii')
    return pwdhash == stored_password


def test_hashing(password):
    hashed_password = hash_password(password)
    return verify_password(hashed_password, password)

if __name__ == "__main__":
    test_password = "ThisIsATestPasswordNumberOne"
    test_res = test_hashing(test_password)
    print(test_res)
