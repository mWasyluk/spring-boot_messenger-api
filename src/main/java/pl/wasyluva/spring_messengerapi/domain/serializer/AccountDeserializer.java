package pl.wasyluva.spring_messengerapi.domain.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import pl.wasyluva.spring_messengerapi.domain.userdetails.Account;
import pl.wasyluva.spring_messengerapi.domain.userdetails.UserAuthority;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Slf4j
public class AccountDeserializer extends StdDeserializer<Account> {
    public AccountDeserializer(){
        this(null);
    }

    public AccountDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Account deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        // TODO: Make sure if it is needed after updateAccount() method
        String id = node.has("id") ? node.get("id").asText() : null;
        String email = node.has("email") ? node.get("email").asText() : null;
        String password = node.has("password") ? node.get("password").asText() : null;
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        JsonNode authoritiesNode = node.has("authorities") ? node.findValue("authorities") : null;
        if (authoritiesNode != null)
            for (int i = 0; i < authoritiesNode.size(); i++){
                grantedAuthorities.add(Enum.valueOf(UserAuthority.class, authoritiesNode.get(i).asText()));
            }

        if (email == null || password == null) {
            log.debug("Provided email or password is null");
            return null;
        }
        Account account = new Account(email, password, grantedAuthorities);
        if (id != null) account.setId(UUID.fromString(id));
        return account;
    }
}
