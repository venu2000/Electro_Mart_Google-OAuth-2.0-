package com.electromart.dto;

/** CSRF token for the SPA. The frontend echoes it in the X-XSRF-TOKEN header on unsafe requests. */
public record CsrfTokenResponse(String headerName, String token) {
}
