package pl.wasyluva.spring_messengerapi.data.service.support;

public interface ServiceResponseMessages {
    String ID_REQUIRED = "To żądanie wymaga podania identyfikatora.";
    String UNUSED_ID_REQUIRED = "To żądanie wymaga podania identyfikatora, który nie został wcześniej użyty.";
    String EXISTING_ID_REQUIRED = "To żądanie wymaga podania istniejącego identyfikatora.";
    String UNAUTHORIZED = "To żądanie wymaga większych uprawnień.";
//    String OK = "Realizacja żądania przebiegła pomyślnie.";
    String TARGET_USER_DOES_NOT_EXIST = "Użytkownik o podanym identyfikatorze nie istnieje.";
    String CORRECT_RANGE_REQUIRED = "To żądanie wymaga prawidłowego zakresu wartości.";
    String CONFLICT = "To żądanie powoduje konflikt z aktualnym stanem obiektów.";

    String EMAIL_ALREADY_IN_USE = "Ten adres e-mail jest już wykorzystywany";
    String PLAIN_PASSWORD_ERROR = "Hasło powinno zostać zaszyfrowane przed przesłaniem";
    String CONVERSATION_DOES_NOT_EXIST = "Taka konwersacja nie istnieje";
    String BAD_CONVERSATION_PARTICIPATORS = "Nie można stworzyć konwersacji z takimi użytkownikami";
    String CONVERSATION_CONFLICT = "Konwersacja już istnieje";
    String CANNOT_ADD_MESSAGE_TO_CONVERSATION = "Wiadomość nie może zostać przesłana";
    String OK = "Operacja udana";
    String ACCOUNT_PROFILE_ALREADY_EXISTS = "Konto ma już utworzony profil";
    String INCORRECT_OBJECT_PROVIDED = "Przesłany obiekt jest niepoprawny";
    String ID_NOT_FOUND = "Przesłany identyfikator nie został odnaleziony";
}
