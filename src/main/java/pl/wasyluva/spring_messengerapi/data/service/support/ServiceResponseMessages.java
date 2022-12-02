package pl.wasyluva.spring_messengerapi.data.service.support;

public interface ServiceResponseMessages {
    String ID_REQUIRED = "To żądanie wymaga podania identyfikatora.";
    String UNUSED_ID_REQUIRED = "To żądanie wymaga podania identyfikatora, który nie został wcześniej użyty.";
    String EXISTING_ID_REQUIRED = "To żądanie wymaga podania istniejącego identyfikatora.";
    String UNAUTHORIZED = "To żądanie wymaga większych uprawnień.";
    String OK = "Realizacja żądania przebiegła pomyślnie.";
    String TARGET_USER_DOES_NOT_EXIST = "Użytkownik o podanym identyfikatorze nie istnieje.";
    String CORRECT_RANGE_REQUIRED = "To żądanie wymaga prawidłowego zakresu wartości.";
    String CONFLICT = "To żądanie powoduje konflikt z aktualnym stanem obiektów.";
}
