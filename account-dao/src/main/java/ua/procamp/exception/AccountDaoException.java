package ua.procamp.exception;

public class AccountDaoException extends RuntimeException{
    public AccountDaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
