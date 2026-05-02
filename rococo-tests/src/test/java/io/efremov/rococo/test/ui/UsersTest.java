package io.efremov.rococo.test.ui;

import static io.efremov.rococo.config.Constants.DEFAULT_PASSWORD;
import static io.efremov.rococo.config.Constants.UI_TAG;

import io.efremov.rococo.jupiter.annotation.AnyUser;
import io.efremov.rococo.jupiter.annotation.Authentication;
import io.efremov.rococo.jupiter.annotation.meta.WebTest;
import io.efremov.rococo.model.UserInfoResponse;
import io.efremov.rococo.page.MainPage;
import io.efremov.rococo.provider.UserProvider;
import io.efremov.rococo.util.RandomDataUtils;
import io.qameta.allure.Epic;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag(UI_TAG)
@Epic("UI")
@Story("Users")
@WebTest
class UsersTest {

  @Test
  @DisplayName("Visit guest")
  void visitGuestTest() {
    MainPage.open()
        .verifyUserIsNotLoggedIn()
        .goToPaintings()
        .verifyEditingIsNotAvailable()
        .goToArtists()
        .verifyEditingIsNotAvailable()
        .goToMuseums()
        .verifyEditingIsNotAvailable();
  }

  @Test
  @DisplayName("Register user")
  void registerUserTest() {
    String newUsername = RandomDataUtils.randomUsername();
    MainPage.open()
        .clickLoginButton()
        .clickRegisterButton()
        .fillRegisterPage(newUsername, DEFAULT_PASSWORD)
        .submit()
        .verifySuccessfullyRegistration()
        .clickEnterButton()
        .verifyPageLoaded();
  }

  @Test
  @AnyUser
  @DisplayName("Login user")
  void loginUserTest(UserInfoResponse userInfo) {
    MainPage.open()
        .clickLoginButton()
        .verifyPageLoaded()
        .fillLoginPage(userInfo.username(), DEFAULT_PASSWORD)
        .submit()
        .verifyUserIsLoggedIn();
  }

  @Test
  @Authentication
  @DisplayName("Update user")
  void updateUserTest() {
    var updatedUser = UserProvider.getUpdateUserRequest();
    MainPage.open()
        .verifyUserIsLoggedIn()
        .updateUser(updatedUser)
        .verifyUserInfoIsUpdated();
  }

  @Test
  @Authentication
  @DisplayName("Logout user")
  void logoutUserTest() {
    MainPage.open()
        .verifyUserIsLoggedIn()
        .logout()
        .verifyPageLoaded()
        .verifyUserIsNotLoggedIn();
  }
}

