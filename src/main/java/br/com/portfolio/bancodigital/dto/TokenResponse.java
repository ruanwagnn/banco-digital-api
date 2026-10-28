package br.com.portfolio.bancodigital.dto;

public record TokenResponse(String token, String tipo, long expiraEmSegundos) {
    public static TokenResponse bearer(String token, long expiraEmSegundos) {
        return new TokenResponse(token, "Bearer", expiraEmSegundos);
    }
}
