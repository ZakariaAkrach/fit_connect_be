package it.zakaria.fitconnect.Service;

import it.zakaria.fitconnect.Controller.auth.AuthenticateRequest;
import it.zakaria.fitconnect.Controller.auth.AuthenticationResponse;
import it.zakaria.fitconnect.Controller.auth.RegisterRequest;
import it.zakaria.fitconnect.Entity.Role;
import it.zakaria.fitconnect.Entity.User;
import it.zakaria.fitconnect.Repository.UserRepository;
import it.zakaria.fitconnect.Utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return AuthenticationResponse.builder().messaggio("Un utente con questa email esiste già").status("KO").build();
        }

        if (!Utils.validatePassword(request.getPassword())) {
            return AuthenticationResponse.builder().messaggio("La password deve essere lunga almeno 8 caratteri e deve contenere almeno una lettera maiuscola, un numero e un simbolo").status("KO").build();
        }

        var user = User.builder()
                .nome(request.getNome())
                .cognome(request.getCognome())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder().token(jwtToken).messaggio("Registrazione avvenuta con successo").status("OK").build();
    }

    public AuthenticationResponse authenticate(AuthenticateRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            var user = userRepository.findByEmail(request.getEmail()).orElse(null);
            var jwtToken = jwtService.generateToken(user);
            return AuthenticationResponse.builder().token(jwtToken).messaggio("Login avvenuto con successo").status("OK").build();
        } catch (AuthenticationException ex) {
            return AuthenticationResponse.builder().messaggio("Credenziali non valide").status("KO").build();
        }
    }

}
