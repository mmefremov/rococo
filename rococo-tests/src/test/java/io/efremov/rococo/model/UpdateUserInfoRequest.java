package io.efremov.rococo.model;

public record UpdateUserInfoRequest(
    String firstname,
    String lastname,
    String avatar
) {

}