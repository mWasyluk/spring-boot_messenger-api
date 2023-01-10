package pl.wasyluva.spring_messengerapi.data.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.wasyluva.spring_messengerapi.data.repository.AccountRepository;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponse;
import pl.wasyluva.spring_messengerapi.data.service.support.ServiceResponseMessages;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    AccountRepository accountRepository;
    AccountService accountService;

    final String testEmail = "test@gmail.com";
    final String testPassword = "$2a$12$psbR2EBlOAXlmrlMCpmSj.Wg/28HjOqRrgsHE1Ud0WTEwiJr5AVZu";
    final String bcryptPrefix = "{bcrypt}";
    Account testAccount;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository);
        testAccount = new Account(testEmail, testPassword);
    }

    @Nested
    @DisplayName("Test if the loadUserByUsername() method")
    class LoadUserByUsername {
        @Test
        @DisplayName("loads the Account by its email")
        void loadUserByUsername() {
            when(accountRepository.findByEmail(testEmail))
                    .thenReturn(Optional.of(testAccount));

            Account account = accountService.loadUserByUsername(testEmail);

            assertThat(account.getUsername()).isEqualTo(testEmail);
            assertThat(account.getPassword()).isEqualTo(testPassword);
        }

        @Test
        @DisplayName("throws an Exception if the Account does not exist")
        public void throwsExceptionIdNotFound() {
            when(accountRepository.findByEmail(any())).thenReturn(Optional.empty());

            Throwable exception = catchThrowable(() -> accountService.loadUserByUsername(testEmail));

            assertThat(exception)
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining(testEmail);
        }
    }

    @Nested
    @DisplayName("Test if the createUserAccount() method")
    class CreateUserAccount {

        @Test
        @DisplayName("resets account's ID if the same ID is already in use")
        void resetsAccountSIdIfTheSameIdIsAlreadyInUse() {
            UUID sId = UUID.randomUUID();
            Account sAccount = new Account();
            sAccount.setId(sId);
            sAccount.setEmail("stest@gmail.com");
            sAccount.setPassword(testPassword);
            when(accountRepository.findById(any())).thenReturn(Optional.of(testAccount)).thenReturn(Optional.empty());
            Account.AccountRegistrationForm form = new Account.AccountRegistrationForm(sAccount.getEmail(), sAccount.getPassword());

            ServiceResponse<?> account = accountService.createAccount(form);

            ArgumentCaptor<UUID> firstArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
            verify(accountRepository, times(2)).findById(firstArgumentCaptor.capture());
            List<UUID> allValues = firstArgumentCaptor.getAllValues();
            assertThat(allValues.get(0)).isNotEqualTo(allValues.get(1));
        }

        @Test
        @DisplayName("throws NullPointerException when the password is null")
        void returnsNullWhenThePasswordIsNull() {
            Account.AccountRegistrationForm form = new Account.AccountRegistrationForm();
            form.setEmail(testEmail);

            Throwable throwable = catchThrowable(() -> accountService.createAccount(form));

            assertThat(throwable).isExactlyInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("throws NullPointerException when the email is null")
        void returnsNullWhenTheEmailIsNull() {
            Account.AccountRegistrationForm form = new Account.AccountRegistrationForm();
            form.setPassword(testPassword);

            Throwable throwable = catchThrowable(() -> accountService.createAccount(form));

            assertThat(throwable).isExactlyInstanceOf(NullPointerException.class);
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("returns null when the email already exists")
        void returnsNullWhenAccountEmailAlreadyInUse() {
            Account existing = new Account("test", "test");
            Account.AccountRegistrationForm form = new Account.AccountRegistrationForm("test", "test");
            when(accountRepository.findByEmail(any())).thenReturn(Optional.of(existing));

            Object body = accountService.createAccount(form).getBody();

            assertThat(body).isEqualTo(ServiceResponseMessages.EMAIL_ALREADY_IN_USE);
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("returns null when the password is not encoded with bcrypt")
        void returnsNullWhenThePasswordIsNotEncodedWithBcrypt() {
            when(accountRepository.findById(any())).thenReturn(Optional.empty());
            when(accountRepository.findByEmail(any())).thenReturn(Optional.empty());

            Object body = accountService.createAccount(new Account.AccountRegistrationForm(testEmail, "somePlainPassword")).getBody();

            assertThat(body).isEqualTo(ServiceResponseMessages.PLAIN_PASSWORD_ERROR);
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("returns Account with a password prefixed with {bcrypt} when the password does not contain it")
        void returnsAccountWithPasswordPrefixedWithBcrypt() {
            when(accountRepository.findById(any())).thenReturn(Optional.empty());
            when(accountRepository.findByEmail(any())).thenReturn(Optional.empty());
            when(accountRepository.save(any())).thenReturn(new Account());

            Object body = accountService.createAccount(new Account.AccountRegistrationForm(testEmail, testPassword)).getBody();

            assertThat(body).isInstanceOf(Account.class);
            ArgumentCaptor<Account> argumentCaptor = ArgumentCaptor.forClass(Account.class);
            verify(accountRepository).save(argumentCaptor.capture());
            assertThat(argumentCaptor.getValue().getPassword()).startsWith(bcryptPrefix);
        }

        @Test
        @DisplayName("returns an Account when it is persistent")
        void returnsAccountWhenIdAndEmailDoNotCollide() {
            when(accountRepository.findById(any())).thenReturn(Optional.empty());
            when(accountRepository.findByEmail(any())).thenReturn(Optional.empty());
            when(accountRepository.save(any())).thenReturn(testAccount);

            Object body = accountService.createAccount(new Account.AccountRegistrationForm(testEmail, testPassword)).getBody();

            assertThat(body).isInstanceOf(Account.class);
            verify(accountRepository).save(any());
        }
    }

    @Nested
    @DisplayName("Test if the deleteAccount() method")
    class DeleteAccountTest {
        @Test
        @DisplayName("returns incorrect_id when the requesting user's account does not exist")
        void returnsIncorrectIdWhenTheRequestingUserSAccountDoesNotExist() {
            when(accountRepository.findById(any())).thenReturn(Optional.empty());

            ServiceResponse<?> serviceResponse = accountService.deleteAccount(testAccount.getId());

            assertThat(serviceResponse).isEqualTo(ServiceResponse.INCORRECT_ID);
        }

        @Test
        @DisplayName("returns incorrect_id when the requesting user's account ID is not a valid UUID")
        void returnsIncorrectIdWhenTheRequestingUserSAccountIdIsNotAValidUuid() {
            String incorrectUuid = "123d2-d123d-d3d2dd-hjj3dd-3v";

            ServiceResponse<?> serviceResponse = accountService.deleteAccount(incorrectUuid);

            assertThat(serviceResponse).isEqualTo(ServiceResponse.INCORRECT_ID);
            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("returns OK when the requesting user's account ID is a valid and existing UUID as String")
        void returnsOkWhenTheRequestingUserSAccountIdIsAValidAndExistingUuidAsString() {
            when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));

            ServiceResponse<?> serviceResponse = accountService.deleteAccount(testAccount.getId().toString());

            assertThat(serviceResponse).isEqualTo(ServiceResponse.OK);
        }
    }
}