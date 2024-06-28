# RegistrationForm

A simple registration form app

# Functionalities:
- register a new user - provide username, password, password confirmation and your email

- log-in - needs email and password

# Technology
Uses doobie for connecting to the postgres sql database. What's more, the app uses scala cats and cats effect to work with a random effect (it must be Sync however). Provides eager validation of the credentials when registering

# Future
This app can be further developed to work with more effects than it does now. Futhermore, considering its functional future, it can be futher developed to store more data on people as well be integrated into a bigger ecosystem.
