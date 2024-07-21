# First Phase Doc
The intent of this document is to expose and explain the details of the backend organization and implementation.

## Software organization and responsibilities

### Services
The Api services were designed with the intent of guaranteeing the operation's restrictions and managing the transactions with the database.

#### Integrity

With operation's restrictions, we mean to secure the application business logic and those, per service, could be:

* #### User Service:

    Validating the email format, by making sure its like
        
        email@domain.com
    Validating the security of the password