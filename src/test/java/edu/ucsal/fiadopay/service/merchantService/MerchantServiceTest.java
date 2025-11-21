package edu.ucsal.fiadopay.service.merchantService;

import edu.ucsal.fiadopay.domain.merchant.*;
import edu.ucsal.fiadopay.domain.merchant.dto.MerchantCreate;
import edu.ucsal.fiadopay.domain.merchant.dto.MerchantRensponse;
import edu.ucsal.fiadopay.domain.user.User;
import edu.ucsal.fiadopay.repo.MerchantRepository;
import edu.ucsal.fiadopay.service.securityService.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

    @Mock
    MerchantRepository merchantRepository;

    @Mock
    SecurityService securityService;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    MerchantService service;

    User fakeUser;

    @BeforeEach
    void setup() {
        fakeUser = new User();
        fakeUser.setId(1L);
        fakeUser.setMerchant(null);
    }

    @Test
    void deveCriarMerchantComSucesso() {
        // arrange
        MerchantCreate dto = new MerchantCreate("Loja X", "https://webhook.site/12", 2.5);

        when(securityService.getAuthenticatedUserId()).thenReturn(fakeUser);
        when(merchantRepository.existsByName(dto.name())).thenReturn(false);
        when(merchantRepository.existsByWebhookUrl(dto.webhookUrl())).thenReturn(false);

        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        // act
        MerchantRensponse response = service.create(dto);

        // assert
        assertNotNull(response);
        assertEquals(dto.name(), response.name());
        verify(merchantRepository, times(1)).save(any(Merchant.class));
    }

    @Test
    void deveFalharQuandoNomeJaExiste() {
        MerchantCreate dto = new MerchantCreate("Loja X", "https://webhook.site/12", 2.5);

        when(securityService.getAuthenticatedUserId()).thenReturn(fakeUser);
        when(merchantRepository.existsByName(dto.name())).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.create(dto)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(merchantRepository, never()).save(any());
    }

    @Test
    void deveFalharQuandoWebhookJaExiste() {
        MerchantCreate dto = new MerchantCreate("Loja X", "https://webhook.site/12", 2.5);

        when(securityService.getAuthenticatedUserId()).thenReturn(fakeUser);
        when(merchantRepository.existsByName(dto.name())).thenReturn(false);
        when(merchantRepository.existsByWebhookUrl(dto.webhookUrl())).thenReturn(true);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.create(dto)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(merchantRepository, never()).save(any());
    }

    @Test
    void deveFalharQuandoUsuarioJaPossuiMerchant() {
        MerchantCreate dto = new MerchantCreate("Loja X", "https://webhook.site/12", 2.5);

        fakeUser.setMerchant(new Merchant()); // usuário já tem merchant
        when(securityService.getAuthenticatedUserId()).thenReturn(fakeUser);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.create(dto)
        );

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(merchantRepository, never()).save(any());
    }
}
